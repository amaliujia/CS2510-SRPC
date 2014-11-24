package com.aos.rpc.abusingClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Client
{
	public static void main(String[] args) throws IOException
	{
		int port = 4000;	
		Socket[] client = new Socket[20];
		for(int i = 0; i < client.length;i++)
		{
			client[i] = new Socket("127.0.0.1", port);
			OutputStream outToServer = client[i].getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
	   
			//preparing the request message (marshalling)
			String requestMsg = "Hello!";
			byte[] request = requestMsg.getBytes();
	   
			//sending the request message in channel
			out.writeInt(request.length);
			out.write(request);
			out.writeUTF("Hello!");
			System.out.println("messages sent");

	   
			//actual sending
			out.flush();
			client[i].close();
		}		
	}
}
