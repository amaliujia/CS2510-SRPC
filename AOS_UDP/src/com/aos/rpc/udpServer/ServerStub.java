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
import com.aos.rpc.dataMarshalling.TCPMapperRequestUnmarshaller;
import com.aos.rpc.dataMarshalling.TCPRequestUnmarshaller;
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

	private TCPRequestUnmarshaller clientTcpUnmarshaller;
	private TCPMapperRequestUnmarshaller mapperUnmarshaller;
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
		clientTcpUnmarshaller = null;
		mapperUnmarshaller = null;
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
		res.setTag(stateKey);
		addResultToStateTable(res);

		double[] result = null;

		long procedure = clientTcpUnmarshaller.getProcedureNumber();
		switch((int)procedure)
		{
		//for the min procedure
		case 1:			
			//only one double (the minimum)
			result = new double[1];
			//call the procedure
			result[0] = program.min(desegmentation.getVector1());
			printoutProcessingEnd(1);
			//form the reply 
			segmentation = new ServerSegmentation(result, null, clientTcpUnmarshaller.getTransactionID());
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
			printoutProcessingEnd(2);
			//form the reply 
			segmentation = new ServerSegmentation(result, null, clientTcpUnmarshaller.getTransactionID());
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
			printoutProcessingEnd(3);
			//form the reply 
			segmentation = new ServerSegmentation(result, null, clientTcpUnmarshaller.getTransactionID());
			//fill the result structure for the status
			res.setElements1_r(1);
			res.setElements1_c(result.length);
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult(result);
			break;
		case 4:
			//form the matrix from the two vectors (two 1-dim vectors) from the request
			matRes.setVectorMatrix(desegmentation.getVector1(),(int) clientTcpUnmarshaller.getNumberOfElements1_r(), (int)clientTcpUnmarshaller.getNumberOfElements1_c());
			double[][] mat1 = matRes.getMatrixFromVector();
			matRes.setVectorMatrix(desegmentation.getVector2(),(int) clientTcpUnmarshaller.getNumberOfElements2_r(), (int)clientTcpUnmarshaller.getNumberOfElements2_c());
			double[][] mat2 = matRes.getMatrixFromVector();
			//check for the dimensions
			if(clientTcpUnmarshaller.getNumberOfElements1_c() != clientTcpUnmarshaller.getNumberOfElements2_r())
				throw new Exception("the dimensions doesn't match");
			//now call the procedure
			double[][] matResult = program.multiply(mat1, mat2);
			printoutProcessingEnd(4);
			matRes.setMatrix(matResult);
			//get the vector from the result matrix
			result = matRes.getVectorFromMatrix();
			//segment it to pass it to the 
			segmentation = new ServerSegmentation(result, null, clientTcpUnmarshaller.getTransactionID());
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

//		printoutRequestRecieved();
		if(doesRequestHasState())
		{
			if(resultExists())
			{
				if(isRequestCompleted())
				{
					//do nothing
				}
				else
				{
					//resend the result by filling the marshaller and give it to the communicator
					RequestStatus res = getResultFromStateTable();
					segmentation = new ServerSegmentation(res.getResult(), null, clientTcpUnmarshaller.getTransactionID());
				}
			}
			else
			{
				removeResultFromStateTable();
				processNewClientRequest();
			}
		}
		else
			processNewClientRequest();
	}

	private void clientRequestRecieved() throws IOException
	{
		//this will inspect the unmarshaller module to check if the stream has been unmarshaled correctly
		if(clientTcpUnmarshaller.isRequestReady())
		{
			//this will not result in CRC error since we are using TCP here, but good to have for future change
			if(!clientTcpUnmarshaller.isCRCError())
			{
				//check if I have the requested procedure
				if(isCallSupported(clientTcpUnmarshaller.getProgramNumber(), clientTcpUnmarshaller.getProgramVersion(), 
						clientTcpUnmarshaller.getProcedureNumber()))
				{
					try
					{
						//fill the udp handler with the recieved request information from TCP and then
						// continue fetching the parameters through the UDP handler
						udpHandler.setTransactionID(clientTcpUnmarshaller.getTransactionID());//set the transactionID
						udpHandler.setTcpConnection(socket);
						udpHandler.setNumberOfPacketsToReceive(clientTcpUnmarshaller.getNumOfPackets());
						udpHandler.recieveUdpParametersPackets();
						handleClientRequest();
					}
					catch(Exception e)
					{
						//form an error response
						segmentation = new ServerSegmentation(null, null, clientTcpUnmarshaller.getTransactionID());
					}
				}
				else
					//form an error response
					segmentation = new ServerSegmentation(null, null, clientTcpUnmarshaller.getTransactionID());
			}
			else
				//form an error response
				segmentation = new ServerSegmentation(null, null, clientTcpUnmarshaller.getTransactionID());
		}
		segmentation.processingSegmentation();
		udpHandler.setNumberOfPacketsToSend(segmentation.getUDPMarshallers().length);
		udpHandler.setUDPMarshallers(segmentation.getUDPMarshallers());
		udpHandler.sendUdpResultsPackets();
	}



	private void mapperRequestRecieved() throws IOException
	{
		//this will inspect the unmarshaller module to check if the stream has been unmarshaled correctly
		if(mapperUnmarshaller.isRequestReady())
		{
			//this will not result in CRC error since we are using TCP here, but good to have for future change
			if(!mapperUnmarshaller.isCRCError())
			{
				try
				{
					//check if the request is  re-register request from the port mapper
					if (mapperUnmarshaller.getRequestType() == 2)
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
				clientTcpUnmarshaller = new TCPRequestUnmarshaller();
				clientTcpUnmarshaller.setStream(request);
				clientMarshaller = new UDPMarshaller();
			}
			else//the request is coming from a mapper for a re-register, hence, prepare to receive
			{
				mapperUnmarshaller = new TCPMapperRequestUnmarshaller();
				mapperUnmarshaller.setStream(request);
				mapperMarshaller = new TCPMapperReplyMarshaller();
			}


			//handle the request from the client
			if(clientTcpUnmarshaller != null)
			{
				//push the communication handling to this module and further handle it
				udpHandler = new UDPServerStreamHandler(socket);
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
				String.valueOf(clientTcpUnmarshaller.getTransactionID()) + "," +
				String.valueOf(clientTcpUnmarshaller.getProcedureNumber()) + 
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

		return result;	
	}

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

	//remove the structure to the state table
	private void removeResultFromStateTable()
	{
		boolean flag = true;
		LinkedList<RequestStatus> temp = stateKeeper.get(stateKey);

		if(temp != null)
		{
			int length = temp.size();
			for(int i = 0; i < length && flag; i++)
			{
				RequestStatus tempResult = temp.get(i);
				if(tempResult.getTag().equals(stateKey))
				{
					temp.remove(i);
					flag = false;
				}
			}
		}
	}


	//reconstruct the parameters to their original form of a one dimentional vector(s)
	private void desegmentRequest()
	{
		desegmentation.setUnmarshallers(udpHandler.getRecievedParametersPackets());
		desegmentation.setNumberOfElements1_r(clientTcpUnmarshaller.getNumberOfElements1_r());
		desegmentation.setNumberOfElements1_c(clientTcpUnmarshaller.getNumberOfElements1_c());
		desegmentation.setNumberOfElements2_r(clientTcpUnmarshaller.getNumberOfElements2_r());
		desegmentation.setNumberOfElements2_c(clientTcpUnmarshaller.getNumberOfElements2_c());
		desegmentation.constructParameters();
	}
	
	//print out for the reception of the request
//	private void printoutRequestRecieved()
//	{
//		System.out.println("=====================================");
//		System.out.println("Request recieved from: " + udpHandler.getClientAddress() + " with Transaction ID: " + clientTcpUnmarshaller.getTransactionID());
//		System.out.println("=====================================");
//
//	}
	
	
	//print out of the end of processing
	private void printoutProcessingEnd(int procNum)
	{
		switch(procNum)
		{
		case 1:
			System.out.println("- Done processing min() for: " + udpHandler.getClientAddress() +
					" with Transaction ID: " + clientTcpUnmarshaller.getTransactionID());
			break;
		case 2:
			System.out.println("- Done processing max() for: " + udpHandler.getClientAddress() +
					" with Transaction ID: " + clientTcpUnmarshaller.getTransactionID());
			break;
		case 3:
			System.out.println("- Done processing sort() for: " + udpHandler.getClientAddress() +
					" with Transaction ID: " + clientTcpUnmarshaller.getTransactionID());
			break;
		case 4:
			System.out.println("- Done processing multiply() for: " + udpHandler.getClientAddress() +
					" with Transaction ID: " + clientTcpUnmarshaller.getTransactionID());
			break;
			default:
				break;
		}	}


}
