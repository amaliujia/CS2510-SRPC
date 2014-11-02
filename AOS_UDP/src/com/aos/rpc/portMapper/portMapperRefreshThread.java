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
				Thread.sleep(300000);
				table.refreshAll ();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
