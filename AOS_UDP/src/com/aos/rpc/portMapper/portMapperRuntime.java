package com.aos.rpc.portMapper;



import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.aos.rpc.dataMarshalling.TCPMapperReplyMarshaller;


public class portMapperRuntime
{
	private ServerSocket listeningSocket;
	private int listeningPort;
	private mapperTable table;
	private Thread refresher;
	
	public portMapperRuntime (mapperTable table, String path) throws IOException
	{
		this.table = table;
		listeningSocket = new ServerSocket(0);
		listeningPort = listeningSocket.getLocalPort();
		listeningSocket.setReceiveBufferSize(10);
		publishMyself(path);
		refresher = new portMapperRefreshThread (this.table);
	}
	
	private void publishMyself(String path) throws IOException
	{
		String toWrite = InetAddress.getLocalHost().getHostAddress() + " " +  listeningSocket.getLocalPort();		 
		File file = new File("address");
		//change it to the afs path

		// if file doesnt exists, then create it
		if (!file.exists()) 
			file.createNewFile();

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(toWrite);
		bw.close();
	}
	
	public void run () throws InterruptedException, SocketTimeoutException
	{
		System.out.println("Waiting for register/request on port " + listeningPort + "...");
		
		refresher.start();
		System.out.println ("The re-registering thread of Port Mapper starts working ...");
		
		while(true)
		{
			Thread[] threadArray = new Thread[10];

			
			Socket tcpConnection;
			try 
			{
				int entry = -1;
				

				tcpConnection = listeningSocket.accept();
				
				
				//print out the length of it
				boolean flag = true;
				for (int i = 0;i < threadArray.length && flag == true; i ++)
				{
					if (threadArray[i] == null)
					{
						entry = i;
						flag = false;
						
					}
					else if (!threadArray[i].isAlive())
					{
						entry = i;
						flag = false;
					}
					
				}
				
				if (entry != -1)
				{
					Thread worker = new PortMapperWorker (tcpConnection, table);
					threadArray[entry] = worker;
					worker.start();
				}
				else
				{

					System.out.println("Error: PortMapper busy");
                    DataOutputStream out = new DataOutputStream(tcpConnection.getOutputStream());


                    TCPMapperReplyMarshaller replyMarshaller = new TCPMapperReplyMarshaller();
                    replyMarshaller.setType((short) 0);
                    replyMarshaller.formStream();
                    byte[] reply;
                    reply = replyMarshaller.getStream();

                    out.writeInt(reply.length);
                    out.write(reply);
                    out.flush();


                    tcpConnection.close();


                }
			} 
			catch (SocketTimeoutException e)
			{
				e.printStackTrace();

				System.out.println("Error: Socket Time Out in PortMapper");
			}

			catch (IOException e)
			{
				e.printStackTrace();
				System.out.println("Error: Cloesd Connection detected in PortMapper");
			}
		}
	}
	
	

}
