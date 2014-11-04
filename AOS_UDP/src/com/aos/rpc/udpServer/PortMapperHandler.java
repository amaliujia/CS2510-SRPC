package com.aos.rpc.udpServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;

import com.aos.rpc.dataMarshalling.TCPMapperReplyUnmarshaller;
import com.aos.rpc.dataMarshalling.TCPMapperRequestMarshaller;

public class PortMapperHandler
{
	private ProgramLibrary program;
	private String portMapperPath;
	private String mapperIp;
	private int mapperPort;
	private int[] ip;
	private int port;
	private TCPMapperRequestMarshaller marshal;
	private TCPMapperReplyUnmarshaller unmarshal;


	public PortMapperHandler(String mapperPath, String serverIp, int serverPort) throws Exception
	{
		portMapperPath = mapperPath;
		program = new ProgramLibrary();
		marshal = new TCPMapperRequestMarshaller();
		unmarshal = new TCPMapperReplyUnmarshaller();
		ip = new int[4];
		port = serverPort;
		portMapperPath = mapperPath;
		fillServerIp(serverIp);
	}

	private void fillMapperIp() throws Exception
	{
		String line = "";
		File file = new File(portMapperPath);
		//change it to the afs path
		// if file doesnt exists, then create it
		if (!file.exists())
			throw new Exception("file openning problem");
		FileReader fr = new FileReader(file.getAbsoluteFile());
		BufferedReader br = new BufferedReader(fr);
		line = br.readLine();
		br.close();
		if(line == null)
			throw new Exception("file is empty problem");
		String[] ipandport = line.split(" ");
		mapperIp = ipandport[0];
		mapperPort = Integer.valueOf(ipandport[1]);
	}

	private void fillServerIp(String serverIp)
	{
		String[] ipAddress = serverIp.split("\\.");
		for(int i = 0; i < ip.length; i++)
			ip[i] = Integer.parseInt(ipAddress[i]);
	}

	private void communicate(byte[] stream) throws IOException
	{
			Socket mapper = new Socket(mapperIp, mapperPort);    
			OutputStream outToMapper = mapper.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToMapper);

			//sending
			out.writeInt(stream.length);
			out.write(stream);
			out.flush();

			InputStream inFromMapper = mapper.getInputStream();
			DataInputStream in = new DataInputStream(inFromMapper);

			//getting the reply message (blocking for reply)
			int replySize = in.readInt();
			byte[] reply = new byte[replySize];
			for (int i = 0; i < replySize; i++)
				reply[i] = (byte) in.read();

			//formatting the reply to String (unmarshalling)
			unmarshal.setStream(reply);
			mapper.close();
	}

	public boolean registerAtPortMapper() throws Exception
	{
		fillMapperIp();

		boolean result = true;

		int[] procedureNumbers = program.getProcedureNumbers();
		for(int i = 0; i < procedureNumbers.length && result; i++)
		{
			//fill the request
			marshal.setIPsWithPort(ip[0], ip[1], ip[2], ip[3], port);
			marshal.setProgramNumber(program.getProgramNumber());
			marshal.setProgramVersion(program.getProgramVersion());
			marshal.setProcedureNumber(procedureNumbers[i]);
			marshal.setRequestType((short)0);
			marshal.setType((short)1);
			if(marshal.formStream())
			{
				communicate(marshal.getStream());
				if(unmarshal.getResult() == 0)
				{
					result = false;
				}
			}
			else
				result = false;
		}
		return result;
	}
}