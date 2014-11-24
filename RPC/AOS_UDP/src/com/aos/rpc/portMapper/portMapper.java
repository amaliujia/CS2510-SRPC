package com.aos.rpc.portMapper;


import java.io.IOException;
import java.net.SocketTimeoutException;

public class portMapper 
{ 
	private mapperTable table;
	private portMapperRuntime runtime;
//
	public portMapper (String path) throws IOException
	{
		table = new mapperTable ();
		runtime = new portMapperRuntime (table, path);

	}


	public void run ()
            throws InterruptedException, SocketTimeoutException
	{
				runtime.run ();
	}
}
