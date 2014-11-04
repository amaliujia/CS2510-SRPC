package com.aos.rpc.udpServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.aos.rpc.dataMarshalling.TCPMapperReplyMarshaller;
import com.aos.rpc.dataMarshalling.TCPMapperRequestDemarshaller;
import com.aos.rpc.dataMarshalling.TCPRequestDemarshaller;
import com.aos.rpc.dataMarshalling.UDPMarshaller;
import com.aos.rpc.helperClasses.MatrixResolver;
import com.aos.rpc.helperClasses.ServerDesegmentation;
import com.aos.rpc.helperClasses.RequestStatus;
import com.aos.rpc.helperClasses.ServerSegmentation;

public class ServerStub extends Thread
{
	private ConcurrentHashMap<String, LinkedList<RequestStatus>> stateKeeper;
	private ProgramLibrary program;

	//the TCP socket connection gotten from the RPCServerRuntime
	private Socket socket;

	private TCPRequestDemarshaller clientTcpDemarshaller;
	private TCPMapperRequestDemarshaller mapperDemarshaller;
	private TCPMapperReplyMarshaller mapperMarshaller;
	private UDPMarshaller clientMarshaller;

	//handles the communication between the client the server using UDP
	private UDPServerStreamHandler udpHandler;

	private ServerDesegmentation desegmentation;
	private ServerSegmentation segmentation;
	
	//the key to access the state table
	private String stateKey;

	public ServerStub(ConcurrentHashMap<String, LinkedList<RequestStatus>> stateKeeper, Socket socket) throws IOException
	{
		this.stateKeeper = stateKeeper;
		this.socket = socket;
		program = new ProgramLibrary();
		clientTcpDemarshaller = null;
		mapperDemarshaller = null;
		clientMarshaller = null;
		mapperMarshaller = null;
		udpHandler = null;
		desegmentation = new ServerDesegmentation();
		segmentation = null;
		stateKey = "";
	}

	
	
	private void processNewClientRequest() throws Exception
	{
		MatrixResolver matRes = new MatrixResolver();
		//crete a result structure with no results and put it in the state table
		RequestStatus res = new RequestStatus();
		addResultToStateTable(res);

		double[] result = null;

		long procedure = clientTcpDemarshaller.getProcedureNumber();
		switch((int)procedure)
		{
		//for the min procedure
		case 1:			
			//only one double (the minimum)
			result = new double[1];
			//call the procedure
			result[0] = program.min(desegmentation.getVector1());
			//form the reply 
			segmentation = new ServerSegmentation(result, null, clientTcpDemarshaller.getTransactionID());
			//fill the result structure for the status
			res.setElements1_r(1);
			res.setElements1_c(1);
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult(result);
			break;
		case 2:
			//only one double (the maximum)
			result = new double[1];
			//call the procedure
			result[0] = program.max(desegmentation.getVector1());
			//form the reply 
			segmentation = new ServerSegmentation(result, null, clientTcpDemarshaller.getTransactionID());
			//fill the result structure for the status
			res.setElements1_r(1);
			res.setElements1_c(1);;
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult(result);
			break;
		case 3:
			//sorted vector
			result = new double[desegmentation.getVector1().length];
			//call the procedure
			result = program.sort(desegmentation.getVector1());
			//form the reply 
			segmentation = new ServerSegmentation(result, null, clientTcpDemarshaller.getTransactionID());
			//fill the result structure for the status
			res.setElements1_r(1);
			res.setElements1_c(result.length);
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult(result);
			break;
		case 4:
			//form the matrix from the two vectors (two 1-dim vectors) from the request
			matRes.setVectorMatrix(desegmentation.getVector1(),(int) clientTcpDemarshaller.getNumberOfElements1_r(), (int)clientTcpDemarshaller.getNumberOfElements1_c());
			double[][] mat1 = matRes.getMatrixFromVector();
			matRes.setVectorMatrix(desegmentation.getVector2(),(int) clientTcpDemarshaller.getNumberOfElements2_r(), (int)clientTcpDemarshaller.getNumberOfElements2_c());
			double[][] mat2 = matRes.getMatrixFromVector();
			//now call the procedure
			double[][] matResult = program.multiply(mat1, mat2);
			matRes.setMatrix(matResult);
			//get the vector from the result matrix
			result = matRes.getVectorFromMatrix();
			//segment it to pass it to the 
			segmentation = new ServerSegmentation(result, null, clientTcpDemarshaller.getTransactionID());
			res.setElements1_r(1);
			res.setElements1_c(result.length);
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult(result);
			break;
		default:
			//something
			break;
		}
		//set the status as "result is calculated but not sent yet"
		res.setHasResult(true);
		addResultToStateTable(res);
	}

	private void handleClientRequest() throws Exception
	{
		desegmentRequest();
		constructStateKey();		

		if(doesRequestHasState())
		{
			if(resultExists())
			{
				if(isRequestCompleted())
				{
					//drop the request by sending to the client that it is done
					System.out.println("This shouldn't happen, and i should drop the request here");
				}
				else
				{
					//resend the result by filling the marshaller and give it to the communicator
					RequestStatus res = getResultFromStateTable();
					segmentation = new ServerSegmentation(res.getResult(), null, clientTcpDemarshaller.getTransactionID());
				}
			}
			else
			{
				stateKeeper.remove(stateKey);
				processNewClientRequest();
			}
		}
		else
			processNewClientRequest();
	}

	private void clientRequestRecieved() throws IOException
	{
		//this will inspect the demarshaller module to check if the stream has been demarshaled correctly
		if(clientTcpDemarshaller.isRequestReady())
		{
			//this will not result in CRC error since we are using TCP here, but good to have for future change
			if(!clientTcpDemarshaller.isCRCError())
			{
				//check if I have the requested procedure
				if(isCallSupported(clientTcpDemarshaller.getProgramNumber(), clientTcpDemarshaller.getProgramVersion(), 
						clientTcpDemarshaller.getProcedureNumber()))
				{
					try
					{
						//fill the udp handler with the recieved request information from TCP and then
						// continue fetching the parameters through the UDP handler
						udpHandler.setTransactionID(clientTcpDemarshaller.getTransactionID());//set the transactionID
						udpHandler.setTcpConnection(socket);
						udpHandler.setNumberOfPacketsToReceive(clientTcpDemarshaller.getNumOfPackets());
						udpHandler.recieveUdpParametersPackets();
						handleClientRequest();
					}
					catch(Exception e)
					{
						//form an error response
						segmentation = new ServerSegmentation(null, null, clientTcpDemarshaller.getTransactionID());
					}
				}
				else
					//form an error response
					segmentation = new ServerSegmentation(null, null, clientTcpDemarshaller.getTransactionID());
			}
			else
				//form an error response
				segmentation = new ServerSegmentation(null, null, clientTcpDemarshaller.getTransactionID());
		}
		segmentation.processingSegmentation();
		udpHandler.setNumberOfPacketsToSend(segmentation.getUDPMarshallers().length);
		udpHandler.setUDPMarshallers(segmentation.getUDPMarshallers());
		udpHandler.sendUdpResultsPackets();
	}

	
	
	private void mapperRequestRecieved() throws IOException
	{
		//this will inspect the demarshaller module to check if the stream has been demarshaled correctly
		if(mapperDemarshaller.isRequestReady())
		{
			//this will not result in CRC error since we are using TCP here, but good to have for future change
			if(!mapperDemarshaller.isCRCError())
			{
				try
				{
					//check if the request is  re-register request from the port mapper
					if (mapperDemarshaller.getRequestType() == 2)
					{
						mapperMarshaller.setType((short)2);
						mapperMarshaller.setReplyType((short)2);
						//fill the result with "yes I'm alive"
						mapperMarshaller.setResult((short)1);
					}
				}
				catch(Exception e)
				{
					//set an error here
					mapperMarshaller.setType((short)0);
				}
			}
			else					
				//set an error here
				mapperMarshaller.setType((short)0);
		}
		else
			//set an error here
			mapperMarshaller.setType((short)0);

		//reply to the mapper with the result here and close the socket
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		mapperMarshaller.formStream();
		out.writeInt(mapperMarshaller.getStream().length);
		out.write(mapperMarshaller.getStream());
		out.flush();
		socket.close();

	}

	
	
	public void run()
	{
		int requestType = -1;
		try
		{
			DataInputStream in;
			in = new DataInputStream(socket.getInputStream());

			int requestSize = in.readInt();
			byte[] request = new byte[requestSize];
			for (int i = 0; i < requestSize; i++)
				request[i] = (byte) in.read();

			requestType = clientOrMapperRequest(request);

			//1: the rquest is coming from a client, hence, prepare to recieve
			if(requestType == 1)
			{
				clientTcpDemarshaller = new TCPRequestDemarshaller();
				clientTcpDemarshaller.setStream(request);
				clientMarshaller = new UDPMarshaller();
			}
			else//the request is coming from a mapper for a re-register, hence, prepare to receive
			{
				mapperDemarshaller = new TCPMapperRequestDemarshaller();
				mapperDemarshaller.setStream(request);
				mapperMarshaller = new TCPMapperReplyMarshaller();
			}


			//handle the request from the client
			if(clientTcpDemarshaller != null)
			{
				//push the communication handling to this module and further handle it
				udpHandler = new UDPServerStreamHandler(socket, stateKeeper);
				clientRequestRecieved();

				//if the response has no errors, update state table by stating that the request is fully served
				if(clientMarshaller.getType() != 0)
				{
					RequestStatus res = getResultFromStateTable();
					res.setCompleted(true);
					addResultToStateTable(res);
				}
			}
			//handle the refresh request (i.e. re-register) from the port mapper
			else
				mapperRequestRecieved();
		}
		catch(SocketTimeoutException s)
		{
			System.out.println("Socket timedout!");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	//does the server support the program number, version, and procedure version
	private boolean isCallSupported(long prgNum, long progVer, long prcNum)
	{
		boolean result = false;

		if (program.isProgramNumberSupported(prgNum) && program.isProgramVersionSupported(progVer) 
				&& program.isProcedureSupported(prcNum))
			result = true;

		return result;
	}

	//form the key for the state table from the IP address and the transactionID
	private void constructStateKey()
	{
		double[] vector1 = desegmentation.getVector1();
		double[] vector2 = desegmentation.getVector2();
		double parameterFirst, parameterLast, parameterMiddle;

		//only one vector (non matrix parameter)
		if(vector2 == null)
		{
			parameterFirst = vector1[0];
			parameterLast = vector1[vector1.length-1];
			parameterMiddle = vector1[vector1.length/2];
		}
		else
		{
			parameterFirst = vector1[0];
			parameterLast = vector2[vector2.length-1];

			int middlePosition = ((vector1.length + vector2.length) / 2);
			if(middlePosition > vector1.length)
			{
				middlePosition = vector2.length - middlePosition;
				parameterMiddle = vector2[middlePosition - 1];
			}
			else
				parameterMiddle = vector1[middlePosition - 1];
		}

		stateKey = String.valueOf(socket.getInetAddress().getHostAddress()) + "," + 
				String.valueOf(clientTcpDemarshaller.getTransactionID()) + "," +
				String.valueOf(clientTcpDemarshaller.getProcedureNumber()) + 
				String.valueOf(parameterFirst) + String.valueOf(parameterLast) +
				String.valueOf(parameterMiddle);
	}

	//check if the Request has been seen before
	private boolean doesRequestHasState()
	{
		boolean result = false;
		RequestStatus temp = getResultFromStateTable();

		if(temp != null)
			result = true;

		return result;	}

	//checks in the status table if the request is recieved
	private boolean resultExists()
	{
		boolean result = false;
		RequestStatus temp = getResultFromStateTable();

		if(temp != null)
			result = temp.isHasResult();

		return result;	}

	//means result calculated and sent and recieved by the client
	private boolean isRequestCompleted()
	{
		boolean result = false;
		RequestStatus temp = getResultFromStateTable();

		if(temp != null)
			result = temp.isCompleted();

		return result;
	}

	//get the first "short type" value of the stream and determine the type of the request
	private int clientOrMapperRequest(byte[] stream)
	{
		int result = -1;

		ByteBuffer buffer = ByteBuffer.wrap(stream);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.rewind();
		result = buffer.getShort();

		return result;
	}

	//returns the RequestResult from the state table or null if it doesn't exist
	private RequestStatus getResultFromStateTable()
	{
		RequestStatus result = null;
		LinkedList<RequestStatus> temp = stateKeeper.get(stateKey);

		if(temp != null)
		{
			int length = temp.size();
			for(int i = 0; i < length; i++)
			{
				RequestStatus tempResult = temp.get(i);
				if(tempResult.getTag().equals(stateKey))
					result = tempResult;
			}
		}

		return result;
	}

	//add the structure to the state table
	private void addResultToStateTable(RequestStatus result)
	{
		LinkedList<RequestStatus> temp = stateKeeper.get(stateKey);

		if(temp == null)
		{
			temp = new LinkedList<RequestStatus>();
			temp.add(result);
			stateKeeper.put(stateKey, temp);
		}
		else
		{
			temp.add(result);
		}
	}
	
	//reconstruct the parameters to their original form of a one dimentional vector(s)
	private void desegmentRequest()
	{
		desegmentation.setDemarshallers(udpHandler.getRecievedParametersPackets());
		desegmentation.setNumberOfElements1_r(clientTcpDemarshaller.getNumberOfElements1_r());
		desegmentation.setNumberOfElements1_c(clientTcpDemarshaller.getNumberOfElements1_c());
		desegmentation.setNumberOfElements2_r(clientTcpDemarshaller.getNumberOfElements2_r());
		desegmentation.setNumberOfElements2_c(clientTcpDemarshaller.getNumberOfElements2_c());
		desegmentation.constructParameters();
	}
}
