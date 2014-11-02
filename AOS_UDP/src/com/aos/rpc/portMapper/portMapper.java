package com.aos.rpc.portMapper;


import java.io.IOException;

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

//    public static void main (String[] args) throws IOException, InterruptedException {
//        mapperTable table = new mapperTable ();
//        portMapperRuntime runtime;
//        runtime = new portMapperRuntime(table);
//        runtime.run();
//
//    }


	public void run ()
            throws InterruptedException
	{
				runtime.run ();
	}
}
