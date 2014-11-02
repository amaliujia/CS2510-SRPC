package com.aos.rpc.udpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import com.aos.rpc.dataMarshalling.TCPMapperRequestMarshaller;
import com.aos.rpc.helperClasses.RequestStatus;

public class ServerRunTime
{
	private ConcurrentHashMap<String, RequestStatus> stateKeeper;
	private ServerSocket serverSocket;
	private ServerStub[] workers;
	private PortMapperHandler portMapper;

	ServerRunTime(String path) throws Exception
	{
		stateKeeper = new ConcurrentHashMap<String, RequestStatus>();
		serverSocket = new ServerSocket(0);
		serverSocket.setReceiveBufferSize(5);
		workers = new ServerStub[10];
		portMapper = new PortMapperHandler(path, InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort());
	}
	

	public void run() throws InterruptedException, IOException
	{
		portMapper.registerAtPortMapper();
		boolean flag = true;
		while(true)
		{
			Socket tcpConnection = null;
			try 
			{
				tcpConnection = serverSocket.accept();
				flag = true;
				for(int i = 0; (i < workers.length) && flag; i++)
				{
					if(workers[i] != null)
					{
						if(!workers[i].isAlive())
						{
							workers[i] = new ServerStub(stateKeeper, tcpConnection);
							workers[i].start();
							flag = false;
						}
					}
					else
					{
						workers[i] = new ServerStub(stateKeeper, tcpConnection);
						workers[i].start();
						flag = false;
					}
				}
				if(flag)
				{
					
					System.out.println("Server busy");
					tcpConnection.close();
					//fetch the request and dump it and reply with
					//error server busy message and close conn.
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
