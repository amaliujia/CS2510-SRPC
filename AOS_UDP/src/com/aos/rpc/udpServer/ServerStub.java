package com.aos.rpc.udpServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;

import com.aos.rpc.dataMarshalling.TCPMapperReplyMarshaller;
import com.aos.rpc.dataMarshalling.TCPMapperRequestDemarshaller;
import com.aos.rpc.dataMarshalling.TCPReplyMarshaller;
import com.aos.rpc.dataMarshalling.TCPRequestDemarshaller;
import com.aos.rpc.dataMarshalling.UDPMarshaller;
import com.aos.rpc.helperClasses.MatrixResolver;
import com.aos.rpc.helperClasses.ParametersConstructor;
import com.aos.rpc.helperClasses.RequestStatus;
import com.aos.rpc.helperClasses.ServerSegmentation;

/*
 * - get the TCP socket
 * - end the communication
 * - establish a udp
 * - check the errors (CRC, out of order, ...)
 * - agree with the client on everything
 * - define a UDP socket
 * - send the port to the client
 * - wait to accept it
 * - start from there the demarshaling
 * - execute
 * - return results
 */

public class ServerStub extends Thread
{
	private ConcurrentHashMap<String, RequestStatus> stateKeeper;
	private ProgramLibrary program;
	private Socket socket;
	private TCPRequestDemarshaller clientTcpDemarshaller;
	private TCPMapperRequestDemarshaller mapperDemarshaller;
	private TCPMapperReplyMarshaller mapperMarshaller;

	private UDPServerStreamHandler udpHandler;
	private UDPMarshaller clientMarshaller;

	private ParametersConstructor parameters;
	private ServerSegmentation segmentation;

	public ServerStub(ConcurrentHashMap<String, RequestStatus> stateKeeper, Socket socket) throws IOException
	{
		this.stateKeeper = stateKeeper;
		this.socket = socket;
		program = new ProgramLibrary();
		clientTcpDemarshaller = null;
		mapperDemarshaller = null;
		clientMarshaller = null;
		mapperMarshaller = null;
		udpHandler = null;
		parameters = new ParametersConstructor();
		segmentation = null;
	}
	
	private void showTheRecievedParameters()
	{
		double[] param1 = parameters.getVector1();
		double[] param2 = parameters.getVector2();
		for(int i = 0; i < param1.length; i++)
			System.out.println(param1[i]);
		
		for(int i = 0; i < param1.length; i++)
			System.out.println(param2[i]);
	}

	private void processNewRequest(String key) throws Exception
	{
		MatrixResolver matRes = new MatrixResolver();
		RequestStatus res = new RequestStatus();
		stateKeeper.put(key, res);
		
		parameters.setDemarshallers(udpHandler.getRecievedParametersPackets());
		parameters.setNumberOfElements1_r(clientTcpDemarshaller.getNumberOfElements1_r());
		parameters.setNumberOfElements1_c(clientTcpDemarshaller.getNumberOfElements1_c());
		parameters.setNumberOfElements2_r(clientTcpDemarshaller.getNumberOfElements2_r());
		parameters.setNumberOfElements2_c(clientTcpDemarshaller.getNumberOfElements2_c());
		parameters.constructParameters();
		
		//showTheRecievedParameters();
		
		double[] result1 = null;
		double[] result2 = null;

		long procedure = clientTcpDemarshaller.getProcedureNumber();
		switch((int)procedure)
		{
		case 1:			
			result1 = new double[1];
			result1[0] = program.min(parameters.getVector1());
			segmentation = new ServerSegmentation(result1, null, clientTcpDemarshaller.getTransactionID());
			res.setElements1_r(1);
			res.setElements1_c(1);;
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult1(result1);
			break;
		case 2:
			result1 = new double[1];
			result1[0] = program.max(parameters.getVector1());
			segmentation = new ServerSegmentation(result1, null, clientTcpDemarshaller.getTransactionID());
			res.setElements1_r(1);
			res.setElements1_c(1);;
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult1(result1);
			break;
		case 3:
			result1 = new double[parameters.getVector1().length];
			result1 = program.sort(parameters.getVector1());
			segmentation = new ServerSegmentation(result1, null, clientTcpDemarshaller.getTransactionID());
			res.setElements1_r(1);
			res.setElements1_c(result1.length);
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult1(result1);
			break;
		case 4:
			matRes.setVectorMatrix(parameters.getVector1(),(int) clientTcpDemarshaller.getNumberOfElements1_r(), (int)clientTcpDemarshaller.getNumberOfElements1_c());
			double[][] mat1 = matRes.getMatrixFromVector();
			matRes.setVectorMatrix(parameters.getVector2(),(int) clientTcpDemarshaller.getNumberOfElements2_r(), (int)clientTcpDemarshaller.getNumberOfElements2_c());
			double[][] mat2 = matRes.getMatrixFromVector();
			double[][] matResult = program.multiply(mat1, mat2);
			matRes.setMatrix(matResult);
			result1 = matRes.getVectorFromMatrix();
			segmentation = new ServerSegmentation(result1, null, clientTcpDemarshaller.getTransactionID());
			res.setElements1_r(1);
			res.setElements1_c(result1.length);
			res.setElements2_r(0);
			res.setElements2_c(0);
			res.setResult1(result1);
			break;
		default:
			//something
			break;
		}
		res.setHasResult(true);
		stateKeeper.put(key, res);
	}

	private void handleClientRequest() throws Exception
	{
		String key = getKey(clientTcpDemarshaller.getTransactionID());

		if(transactionIDExists(key))
		{
			if(resultExists(key))
			{
				if(isRequestCompleted(key))
				{
					//drop the request by sending to the client that it is done
					System.out.println("This shouldn't happen, and i should drop the request here");
				}
				else
				{
					//resend the result by filling the marshaller and give it to the communicator
					RequestStatus res = stateKeeper.get(key);
					segmentation = new ServerSegmentation(res.getResult1(), null, clientTcpDemarshaller.getTransactionID());
				}
			}
			else
			{
				stateKeeper.remove(key);
				processNewRequest(key);
			}
		}
		else
			processNewRequest(key);
	}

	private void clientRequestRecieved() throws IOException
	{
		if(clientTcpDemarshaller.isRequestReady())
		{
			if(!clientTcpDemarshaller.isCRCError())
			{
				if(isCallSupported(clientTcpDemarshaller.getProgramNumber(), clientTcpDemarshaller.getProgramVersion(), 
						clientTcpDemarshaller.getProcedureNumber()))
				{
					try
					{
						udpHandler.setTrID(clientTcpDemarshaller.getTransactionID());
						udpHandler.setTcpConnection(socket);
						udpHandler.setNumberOfPacketsToReceive(clientTcpDemarshaller.getNumOfPackets());
						udpHandler.recieveUdpParametersPackets();
						handleClientRequest();
					}
					catch(Exception e)
					{
						//here system error
						segmentation = new ServerSegmentation(null, null, clientTcpDemarshaller.getTransactionID());
					}
				}
				else
					segmentation = new ServerSegmentation(null, null, clientTcpDemarshaller.getTransactionID());
			}
			else
				segmentation = new ServerSegmentation(null, null, clientTcpDemarshaller.getTransactionID());
		}
		segmentation.processingSegmentation();
		udpHandler.setNumberOfPacketsToSend(segmentation.getUDPMarshallers().length);
		udpHandler.setUDPMarshallers(segmentation.getUDPMarshallers());
		udpHandler.sendUdpResultsPackets();
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

			if(requestType == 1)
			{
				clientTcpDemarshaller = new TCPRequestDemarshaller();
				clientTcpDemarshaller.setStream(request);
				clientMarshaller = new UDPMarshaller();
			}
			else
			{
				mapperDemarshaller = new TCPMapperRequestDemarshaller();
				mapperDemarshaller.setStream(request);
				mapperMarshaller = new TCPMapperReplyMarshaller();
			}


			if(clientTcpDemarshaller != null)
			{
				udpHandler = new UDPServerStreamHandler(socket, stateKeeper);
				clientRequestRecieved();
				//update state table
				if(clientMarshaller.getType() != 0)
				{
					RequestStatus res = stateKeeper.get(getKey(clientTcpDemarshaller.getTransactionID()));
					res.setCompleted(true);
					stateKeeper.put(getKey(clientTcpDemarshaller.getTransactionID()), res);
				}
			}
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

	private boolean isCallSupported(long prgNum, long progVer, long prcNum)
	{
		boolean result = false;

		if (program.isProgramNumberSupported(prgNum) && program.isProgramVersionSupported(progVer) 
				&& program.isProcedureSupported(prcNum))
			result = true;

		return result;
	}

	private String getKey(long trID)
	{
		String result = null;

		result = String.valueOf(socket.getInetAddress().getHostAddress()) + "," + 
				String.valueOf(socket.getPort()) + "," + String.valueOf(trID);

		return result;
	}

	private boolean transactionIDExists(String key)
	{
		boolean result = false;

		if(stateKeeper.containsKey(key))
			result = true;

		return result;
	}

	private boolean resultExists(String key)
	{
		boolean result = false;
		RequestStatus value = stateKeeper.get(key);

		if(stateKeeper.get(key) != null)
			result = value.isHasResult();

		return result;
	}

	private boolean isRequestCompleted(String key)
	{
		boolean result = false;
		RequestStatus value = stateKeeper.get(key);

		if(stateKeeper.get(key) != null)
			result = value.isCompleted();

		return result;
	}
	
	private void handleMapperRequest()
	{	
        if(!mapperDemarshaller.isCRCError())
        {
            if (mapperDemarshaller.getRequestType() == 2)
            {
            	mapperMarshaller.setReplyType((short)2);
                mapperMarshaller.setResult((short)1);
            }
        }
	}

	private int clientOrMapperRequest(byte[] stream)
	{
		int result = -1;

		ByteBuffer buffer = ByteBuffer.wrap(stream);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.rewind();
		result = buffer.getShort();

		return result;
	}

	private void mapperRequestRecieved() throws IOException
	{
		if(mapperDemarshaller.isRequestReady())
		{
			if(!mapperDemarshaller.isCRCError())
			{
				try
				{
					mapperMarshaller.setType((short)2);
					handleMapperRequest();
				}
				catch(Exception e)
				{
					//here system error
					mapperMarshaller.setType((short)0);
				}
			}
			else
				mapperMarshaller.setType((short)0);
		}
		else
			mapperMarshaller.setType((short)0);
		
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		mapperMarshaller.formStream();

		//sending reply
		out.writeInt(mapperMarshaller.getStream().length);
		out.write(mapperMarshaller.getStream());
		out.flush();
		socket.close();

	}

}
