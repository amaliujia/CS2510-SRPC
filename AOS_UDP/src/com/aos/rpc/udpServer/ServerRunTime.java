package com.aos.rpc.udpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.aos.rpc.helperClasses.RequestStatus;

public class ServerRunTime
{
	private ConcurrentHashMap<String, LinkedList<RequestStatus>> stateKeeper;
	private ServerSocket serverSocket;
	//keep track of the worker threads as a max of 10 threads
	private ServerStub[] workers;
	private PortMapperHandler portMapper;

	ServerRunTime(String path) throws Exception
	{
		stateKeeper = new ConcurrentHashMap<String, LinkedList<RequestStatus>>();
		serverSocket = new ServerSocket(0);
		serverSocket.setReceiveBufferSize(5);
		workers = new ServerStub[10];
		portMapper = new PortMapperHandler(path, InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort());
	
	}
	

	public void run() throws InterruptedException, IOException
	{
		boolean mapperIsUp = true;
		try
		{
			portMapper.registerAtPortMapper();
		}
		catch(Exception e)
		{
			System.out.println("Error: Communication with Name Server failed, services cannot be registered.");
			mapperIsUp = false;
		}
		boolean flag = true;
		while(true && mapperIsUp)
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
					System.out.println("Warning: server is getting busy, a connection has been dropped");
					tcpConnection.close();
				}
			} 
			catch (IOException e)
			{
				System.out.println("Error: connection error, cannot accept connections");
			}
		}
	}
}
