package com.aos.rpc.udpServer;

import java.io.IOException;

public class Server 
{
	public static void main(String[] args) throws Exception
	{
		String mapperPath =  "address";
		ServerRunTime rpc = new ServerRunTime(mapperPath);
		rpc.run();
	}
}
