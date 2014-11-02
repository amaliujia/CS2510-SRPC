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

import com.aos.rpc.dataMarshalling.TCPMapperReplyDemarshaller;
import com.aos.rpc.dataMarshalling.UDPDemarshaller;
import com.aos.rpc.dataMarshalling.UDPMarshaller;


public class ClientRPCRuntime 
{
	private int serverTCPPort;
	private String serverIPString;
	private int mapperPort;
	private String mapperIPString;

    private int serverUDPPort;
	
	public ClientRPCRuntime(String path) throws Exception
	{
		fillMapperIp(path);
	}

	private void fillMapperIp(String path) throws Exception
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
		String[] ipandport = line.split(" ");
		mapperIPString = ipandport[0];
		mapperPort = Integer.valueOf(ipandport[1]);
	}


    public UDPDemarshaller[] dataToServer (UDPMarshaller[] udpMarshallers, int elementsToReceive, long tranID)
            throws IOException
    {
    	System.out.println();
    	UDPClientStreamHandler udpClientStreamHandler = new UDPClientStreamHandler(serverUDPPort, serverIPString, udpMarshallers, elementsToReceive, tranID);
    	udpClientStreamHandler.sendUdpParametersPackets();
        SimpleDateFormat ft = 
        new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        System.out.println("Current Date: " + ft.format(new Date( )));    	
        udpClientStreamHandler.recieveUdpResultsPackets();
        System.out.println("Current Date: " + ft.format(new Date( )));    	
        return  udpClientStreamHandler.getRecievedResultPackets();

    }

	public void requestMethodToServer (ClientStub clientStub, byte[] requestToServer) throws UnknownHostException, IOException
	{
		try {
            System.out.println("Connecting to Server on its port " + serverTCPPort);

            Socket client = new Socket(serverIPString, serverTCPPort);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());


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
            System.out.println("port is:" + serverUDPPort);
            client.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("Errors detected when communicating to the server using TCP!!!");
        }
    }

	// check the port Mapper for the relative IP and port number
	public void requestMethodToPortMapper (byte[] requestToPortMapper)
	{
		try
		{
			System.out.println("Connecting to PortMapper on its port " + mapperPort);
			Socket client = new Socket(mapperIPString, mapperPort);
			System.out.println("Just connected to "+ client.getRemoteSocketAddress());
       
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
			demarshalReplyFromPortMapper (response);
            client.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
            System.out.println("Errors detected when communicating to the port mapper!!!");
		}
	}
	
	private void demarshalReplyFromPortMapper (byte[] response)
	{
		TCPMapperReplyDemarshaller replyDemarshaller = new TCPMapperReplyDemarshaller();
        replyDemarshaller.setStream(response);
        int ip1 = replyDemarshaller.getIp1();
        int ip2 = replyDemarshaller.getIp2();
        int ip3 = replyDemarshaller.getIp3();
        int ip4 = replyDemarshaller.getIp4();
        
        serverIPString = ip1 + "." +ip2 + "." + ip3 + "." + ip4 ;
        serverTCPPort = replyDemarshaller.getPort();

        System.out.println("The corresponding IP and port acquired from the PortMapper is   " + serverIPString + ":" +serverTCPPort);
        

	}
	
}
