package com.aos.rpc.portMapper;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.aos.rpc.dataMarshalling.TCPMapperReplyUnmarshaller;
import com.aos.rpc.dataMarshalling.TCPMapperRequestMarshaller;


public class mapperTable 
{
	private ConcurrentHashMap<String, LinkedList<IPAndPort>> portTable;
	private ConcurrentHashMap<String, Integer> counters;

	public mapperTable ()
	{
		portTable = new ConcurrentHashMap<String, LinkedList<IPAndPort>> ();
		counters = new ConcurrentHashMap<String, Integer> ();
	}

	public void register (String proc, IPAndPort ipp)
	{
		if (portTable.containsKey(proc) == false)
		{
			LinkedList<IPAndPort> ippList = new LinkedList<IPAndPort> ();
			ippList.add(ipp);
			portTable.put(proc, ippList);
			counters.put(proc, 0);
		}
		else
		{
			LinkedList<IPAndPort> temp = portTable.get(proc);
			temp.add(ipp);
		}
	}

	public IPAndPort query (String proc)
	{

		if (portTable.containsKey(proc) && counters.containsKey(proc))
		{
			int index = (counters.get(proc) + 1) % portTable.get(proc).size();
			//System.out.println("index is:" +index);
			counters.put(proc, index);
			return portTable.get(proc).get(index);
		}
		else
			return null;
	}

	public void refreshAll ()
	{
		Set<String> keys = portTable.keySet();
		for (String key : keys )
		{
			LinkedList<IPAndPort> temp = portTable.get(key);
			LinkedList<IPAndPort> newList = new LinkedList<IPAndPort>();


			for (int i = 0; i < temp.size(); i ++)
			{
				IPAndPort ipp = temp.get(i);
				if (reRegister (ipp, key) == true)
				{
					System.out.println("- Server " + ipp.getIP() + " is still offering prcedure number " + key.charAt(4));
					newList.add(ipp);
				}
			}
			if(newList.size() != 0)
			{
				portTable.put(key, newList);
				counters.put(key, newList.size()-1);
			}
			else
			{
				portTable.remove(key);
				counters.remove(key);
			}
		}
	}

	private boolean reRegister (IPAndPort ipp, String proc)
	{
		InetAddress ip = ipp.getIP();
		int port = ipp.getPort();
		boolean flag = false;
		try 
		{
			Socket socket = new Socket (ip, port);
			DataOutputStream out = new DataOutputStream (socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());


			//------------------------------------------------------------
			//convert InetAddress into string , then into integer
			String[] ipComponentsString = ip.getHostAddress().split("\\.");
			int[] ipComponents = new int[4];
			for (int i = 0; i < ipComponentsString.length; i++)
			{
				ipComponents[i] = Integer.parseInt(ipComponentsString[i]);
			}

			//------------------------------------------------------------
			//convert prog #, prog ver #, proc #  from string into integer
			String[] procComponentsString = proc.split(",");
			int[] procComponents = new int[3];
			for (int i = 0; i < procComponentsString.length; i ++)
			{
				procComponents[i] = Integer.parseInt(procComponentsString[i]);
			}


			TCPMapperRequestMarshaller m = new TCPMapperRequestMarshaller();
			m.setIPsWithPort(ipComponents[0], ipComponents[1], ipComponents[2], ipComponents[3], port);
			m.setProgramNumber(procComponents[0]);
			m.setProgramVersion(procComponents[1]);
			m.setProcedureNumber(procComponents[2]);
			m.setType((short)3);
			m.setRequestType((short)2);
			m.formStream();

			byte[] request = m.getStream();

			out.writeInt(request.length);
			out.write(request);
			out.flush();


			int replySize = in.readInt();
			byte[] response = new byte[replySize];
			for (int i = 0; i < replySize; i++)
				response[i] = (byte) in.read();





			socket.close();

			TCPMapperReplyUnmarshaller replyUnmarshaller = new TCPMapperReplyUnmarshaller();
			replyUnmarshaller.setStream(response);

			if (replyUnmarshaller.getResult() == 1)
				
				flag = true;
			else
				flag = false;


		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return flag;
		}
		return flag;

	}




}

class IPAndPort
{
	private InetAddress ip;
	private int port;

	public IPAndPort (InetAddress ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}

	public InetAddress getIP ()
	{
		return this.ip;
	}

	public int getPort ()
	{
		return this.port;
	}
}


