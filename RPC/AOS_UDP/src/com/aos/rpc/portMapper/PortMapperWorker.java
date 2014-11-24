package com.aos.rpc.portMapper;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.aos.rpc.dataMarshalling.TCPMapperReplyMarshaller;
import com.aos.rpc.dataMarshalling.TCPMapperRequestUnmarshaller;

public class PortMapperWorker extends Thread
{
	private Socket socket;
	private mapperTable table;
	
	public PortMapperWorker (Socket tcpConnection, mapperTable table)
	{
		this.table = table;
		this.socket = tcpConnection;
	}
	

    public void run ()
	{
		try
		{
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream (socket.getOutputStream());
			
			//System.out.println("I'm reading the stream now");
			int requestSize = in.readInt();
			byte[] request = new byte[requestSize];
			for (int i = 0; i < requestSize; i++)
				request[i] = (byte) in.read();

            TCPMapperRequestUnmarshaller  requestUnmarshaller= new TCPMapperRequestUnmarshaller();
            requestUnmarshaller.setStream(request);


            if(requestUnmarshaller.isCRCError() == false)
            {

                if (requestUnmarshaller.getRequestType() == 0)
                {

                    int ip1 = requestUnmarshaller.getIp1();
                    int ip2 = requestUnmarshaller.getIp2();
                    int ip3 = requestUnmarshaller.getIp3();
                    int ip4 = requestUnmarshaller.getIp4();
                    int port = requestUnmarshaller.getPort();
                    long prog = requestUnmarshaller.getProgramNumber();
                    long progV = requestUnmarshaller.getProgramVersion();
                    long proc = requestUnmarshaller.getProcedureNumber();
                    String ipString = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                    String procedure = prog + "," + progV + "," + proc;
                    IPAndPort ipp = new IPAndPort(InetAddress.getByName(ipString), port);
        			System.out.println("- Server " + ipString + " is registering the procedure: " + proc);

                    
                    table.register(procedure, ipp);


                    TCPMapperReplyMarshaller replyMarshaller = new TCPMapperReplyMarshaller();
                    replyMarshaller.setType((short) 2);
                    replyMarshaller.setReplyType((short) 0);
                    replyMarshaller.setResult((short)1);
                    replyMarshaller.formStream();
                    byte[] reply;
                    reply = replyMarshaller.getStream();

                    out.writeInt(reply.length);
                    out.write(reply);
                    out.flush();




                }
                else if (requestUnmarshaller.getRequestType() == 1)
                {
                    long prog = requestUnmarshaller.getProgramNumber();
                    long progV = requestUnmarshaller.getProgramVersion();
                    long proc = requestUnmarshaller.getProcedureNumber();

                    String procedure = prog + "," + progV + "," + proc;

                    IPAndPort ipp = table.query(procedure);

                    TCPMapperReplyMarshaller replyMarshaller = new TCPMapperReplyMarshaller();
                    replyMarshaller.setType((short) 2);
                    replyMarshaller.setReplyType((short)1);
                    if (ipp != null)
                    {

                        String[] ipString = ipp.getIP().getHostAddress().split("\\.");
                        int[] ip = new int[4];
                        for (int i = 0; i < ipString.length; i ++)
                            ip[i] = Integer.parseInt(ipString[i]);
                        replyMarshaller.setIPsWithPort(ip[0], ip[1], ip[2], ip[3],ipp.getPort());


                    }
                    else
                    {
                        replyMarshaller.setIPsWithPort(0, 0, 0, 0, 0);
                    }
                    replyMarshaller.formStream(); 
                    byte[] reply = replyMarshaller.getStream();
                    
//                    System.out.println(reply);

                    out.writeInt(reply.length);
                    out.write(reply);
                    out.flush();
                }



            }
            else
            {
                System.out.println("CRC error detected !!!");
            }


			


			socket.close();
		}
		catch(SocketTimeoutException s)
		{
			System.out.println("Socket timed out!");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

}
