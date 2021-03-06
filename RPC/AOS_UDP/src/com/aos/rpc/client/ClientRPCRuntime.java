package com.aos.rpc.client;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aos.rpc.dataMarshalling.TCPMapperReplyUnmarshaller;
import com.aos.rpc.dataMarshalling.UDPUnmarshaller;
import com.aos.rpc.dataMarshalling.UDPMarshaller;


public class ClientRPCRuntime 
{
	private int serverTCPPort;
	private String serverIPString;
	private int mapperPort;
	private String mapperIPString, path;

	private int serverUDPPort = 0;

	public ClientRPCRuntime() throws Exception
	{
		this.path = "address";
	}

	private void fillMapperIp() throws Exception
	{
		
		String line = "";
		File file = new File(path);
		//change it to the afs path

		// if file doesnt exists, then create it
		if (!file.exists())
			throw new Exception("file openning problem");
		FileReader fr = new FileReader(file.getAbsoluteFile());
		BufferedReader br = new BufferedReader(fr);
		line = br.readLine();
		br.close();
		if(line == null)
			throw new Exception("file is empty");

		String[] ipandport = line.split(" ");
		mapperIPString = ipandport[0];
		mapperPort = Integer.valueOf(ipandport[1]);
		
	}


	public UDPUnmarshaller[] dataToServer (UDPMarshaller[] udpMarshallers, int elementsToReceive, long tranID)
			throws IOException
	{
		if (serverIPString != "0.0.0.0" && serverUDPPort != 0)
		{
			UDPClientStreamHandler udpClientStreamHandler = new UDPClientStreamHandler(serverUDPPort, serverIPString, udpMarshallers, elementsToReceive, tranID);
			udpClientStreamHandler.sendUdpParametersPackets();
			udpClientStreamHandler.recieveUdpResultsPackets();
			return  udpClientStreamHandler.getRecievedResultPackets();
		}
		else 
		{
			return null;
		}

	}

	public void requestMethodToServer (ClientStub clientStub, byte[] requestToServer) throws UnknownHostException, IOException
	{
		if (serverIPString != "0.0.0.0")	
		{
			try 
			{
			//	System.out.println("Connecting to Server on its port " + serverTCPPort);

				Socket client = new Socket(serverIPString, serverTCPPort);
			//	System.out.println("Just connected to " + client.getRemoteSocketAddress());


				//-------------------------------------------------
				//send to the Server
				//-------------------------------------------------
				DataOutputStream out = new DataOutputStream(client.getOutputStream());

				out.writeInt(requestToServer.length);
				out.write(requestToServer);
				out.flush();


				//-------------------------------------------------
				//receive from the Server
				//-------------------------------------------------
				DataInputStream in = new DataInputStream(client.getInputStream());

				serverUDPPort = in.readInt();
			//	System.out.println("Port is: " + serverUDPPort);
				client.close();
			}
			catch (IOException e)
			{
				//e.printStackTrace();
				System.out.println("Error:  1. Server is busy.  OR  2.TCP Connection between server and client failed. ");
				serverUDPPort = 0;
			}
		}
	}

	// check the port Mapper for the relative IP and port number
	public void requestMethodToPortMapper (byte[] requestToPortMapper)
	{
		try
		{
			fillMapperIp();
		//	System.out.println("Connecting to PortMapper on its port " + mapperPort);
			Socket client = new Socket(mapperIPString, mapperPort);
		//	System.out.println("Just connected to "+ client.getRemoteSocketAddress());

			//preparing the channel to send request
			DataOutputStream out = new DataOutputStream(client.getOutputStream());

			//sending the request message in channel
			out.writeInt(requestToPortMapper.length);
			//			out.writeUTF("Hello!");
			out.write(requestToPortMapper);
			//actual sending
			out.flush();


			//preparing the channel to receive the reply
			DataInputStream in = new DataInputStream(client.getInputStream());

			//getting the reply message (blocking for reply)
			int replySize = in.readInt();
			byte[] response = new byte[replySize];
			for (int i = 0; i < replySize; i++)
				response[i] = (byte) in.read();
			unmarshalReplyFromPortMapper (response);
			client.close();
		}
		catch(Exception e)
		{
		//	e.printStackTrace();
			serverIPString = "0.0.0.0";
			serverTCPPort = 0;
			System.out.println("Error: Communication with Name Server failed.");

		}
	}

	private void unmarshalReplyFromPortMapper (byte[] response)
	{
		TCPMapperReplyUnmarshaller replyUnmarshaller = new TCPMapperReplyUnmarshaller();
		replyUnmarshaller.setStream(response);
		if (replyUnmarshaller.getType() == 2) 
		{
			int ip1 = replyUnmarshaller.getIp1();
			int ip2 = replyUnmarshaller.getIp2();
			int ip3 = replyUnmarshaller.getIp3();
			int ip4 = replyUnmarshaller.getIp4();

			serverIPString = ip1 + "." +ip2 + "." + ip3 + "." + ip4 ;
			serverTCPPort = replyUnmarshaller.getPort();

	//		System.out.println("The corresponding IP and port acquired from the PortMapper is   " + serverIPString + ":" +serverTCPPort);
		}
		else if (replyUnmarshaller.getType() == 0)
		{
			System.out.println("Error: (In Client Side) PortMapper being busy");
			serverIPString = "0.0.0.0";
			serverTCPPort = 0;
		}
		else 
		{
			System.out.println("Error: (In Client Side) Wrong message format from PortMapper");
			serverIPString = "0.0.0.0";
			serverTCPPort = 0;

		}
	}

}
