package com.aos.rpc.portMapper;


public class portMapperRefreshThread extends Thread 
{
	private mapperTable table;
	public portMapperRefreshThread (mapperTable table)
	{
		this.table = table; 
		
	}
	
	public void run ()
	{
		while (true)
		{
			try 
			{
				Thread.sleep(10000);
				System.out.println("- The port mapper starts re-registering...");
				table.refreshAll ();
				System.out.println("- The port mapper has done re-registering.");


				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
