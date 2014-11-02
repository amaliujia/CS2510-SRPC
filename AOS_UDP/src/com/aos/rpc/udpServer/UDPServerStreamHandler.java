package com.aos.rpc.udpServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import com.aos.rpc.dataMarshalling.UDPDemarshaller;
import com.aos.rpc.dataMarshalling.UDPMarshaller;
import com.aos.rpc.helperClasses.RequestStatus;

public class UDPServerStreamHandler
{
	private int clientPort;
	private InetAddress clientAddress; 
	private int burstSize;
	private Socket tcpConnection;
	private UDPDemarshaller demarshaller;
	private UDPMarshaller marshaller;
	private UDPDemarshaller[] recievedParametersPackets;
	private UDPMarshaller[] toSendResultsPackets;
	private long numberOfPacketsToReceive;
	private long numberOfPacketsToSend;
	private DatagramSocket udpSocket;
	private ConcurrentHashMap<String, RequestStatus> stateKeeper;
	private long TrID;

	public UDPServerStreamHandler(Socket tcpConnection, ConcurrentHashMap<String, RequestStatus> stateKeeper) throws IOException
	{
		this.tcpConnection = tcpConnection;
		demarshaller = new UDPDemarshaller();
		marshaller = new UDPMarshaller();
		burstSize = 5;
		udpSocket = new DatagramSocket(0);
		udpSocket.setSoTimeout(10000);
		this.stateKeeper = stateKeeper;
		sendPortAndCloseTCP();
	}
	
	private void sendPortAndCloseTCP() throws IOException
	{
		DataOutputStream out = new DataOutputStream(tcpConnection.getOutputStream());
		//sending the pot
		out.writeInt(udpSocket.getLocalPort());
        System.out.println("port is:" + udpSocket.getLocalPort());
		out.flush();
		tcpConnection.close();
	}
	
	private void sendAck(long neededPacket) throws IOException
	{
		marshaller.setTransactionID(TrID);
		marshaller.setSequenceID(neededPacket);
		marshaller.setVectorSize(0);
		marshaller.setType((short)2);
		marshaller.formStream();
		byte[] stream = marshaller.getStream();
		DatagramPacket ack = new DatagramPacket(stream, stream.length, clientAddress, clientPort);
		udpSocket.send(ack);
	}
	
	private byte[] getTheRightStream(byte[] bufferStream, int length)
	{
		byte[] result = new byte[length];
		
		for (int i = 0; i < length; i++)
			result[i] = bufferStream[i];
		
		return result;
	}

	public void sendUdpResultsPackets() throws IOException
	{		
		boolean flag = true;
		int attempts = 0;
		long packetToSendNum = 0;

		while((attempts < 3) && (packetToSendNum < numberOfPacketsToSend) && flag)
		{
			for (int i = 0; i < burstSize; i++)
			{
                if (packetToSendNum + i >= numberOfPacketsToSend) {
                    packetToSendNum = packetToSendNum + i;
                    break;
                }
                byte[] stream = toSendResultsPackets[(int)packetToSendNum + i].getStream();
				DatagramPacket packetToSend = new DatagramPacket(stream, stream.length, clientAddress, clientPort);
				udpSocket.send(packetToSend);
			}
			try
			{
				byte[] buffer = new byte[26];
				DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
				udpSocket.receive(receivePacket);
				demarshaller.resetAll();
                
				demarshaller.setStream(getTheRightStream(receivePacket.getData(), receivePacket.getLength()));

				if(!demarshaller.isCRCError()) {
                    packetToSendNum = demarshaller.getSequenceID() - 1;
                    attempts = 0;
                }
			}
			catch (SocketTimeoutException ste)
			{
				attempts++;
				if (attempts == 3)
					flag = false;
			}
		}

		if (attempts == 3)
		{
			//give up and issue a new request
            System.out.println("Give up!!!");
        }
		else if(packetToSendNum == numberOfPacketsToSend)
		{
			System.out.println("Good job!!!");
		}
	}

	public void recieveUdpParametersPackets() throws IOException
	{
		boolean flag = true;
		int attempts = 0;
		long neededPacket = 1;
		recievedParametersPackets = new UDPDemarshaller[(int)numberOfPacketsToReceive];
		
		while((attempts < 3) && ((neededPacket - 1) < numberOfPacketsToReceive))
		{
			for (int i = 0; i < burstSize && flag && (neededPacket <= numberOfPacketsToReceive); i++)
			{
				try
				{
					byte[] buffer = new byte[4122];
					DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
					udpSocket.receive(receivePacket);
					clientPort = receivePacket.getPort();
					clientAddress = receivePacket.getAddress();
					demarshaller.setStream(getTheRightStream(receivePacket.getData(), receivePacket.getLength()));
					if(!demarshaller.isCRCError())
					{
						if(demarshaller.getSequenceID() == neededPacket)
						{
							recievedParametersPackets[(int)neededPacket-1] = demarshaller;
							demarshaller = new UDPDemarshaller();
							attempts = 0;
							neededPacket++;
						}
						else
							flag = false;
					}
					else
						flag = false;
				}
				catch (SocketTimeoutException ste)
				{
					attempts++;
					if (attempts == 3)
						flag = false;
				}
			}
			if(attempts != 3)
			{
				sendAck(neededPacket);
			}
		}

		if (attempts == 3)
		{
			stateKeeper.remove(demarshaller.getTransactionID());
			//delete the entry from the status table with TrID (give up the request)
		}
		else if(neededPacket == numberOfPacketsToReceive)
		{
			System.out.println("Good Job!!! yes I'm complementing myself!!!");
		}
	}

	public void setTcpConnection(Socket tcpConnection) {
		this.tcpConnection = tcpConnection;
	}
	
	public UDPDemarshaller[] getRecievedParametersPackets()
	{
		return this.recievedParametersPackets;
	}
	
	public void setUDPMarshallers(UDPMarshaller[] results)
	{
		this.toSendResultsPackets = results;
	}

	public void setNumberOfPacketsToReceive(long numberOfPacketsToReceive) {
		this.numberOfPacketsToReceive = numberOfPacketsToReceive;
	}
	
	public void setNumberOfPacketsToSend(long numberOfPacketsToSend) {
		this.numberOfPacketsToSend = numberOfPacketsToSend;
	}

	public void setTrID(long TrID)
	{
		this.TrID = TrID;
	}
	
}
