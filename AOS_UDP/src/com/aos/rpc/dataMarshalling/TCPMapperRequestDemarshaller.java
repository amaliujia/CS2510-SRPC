package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/*
 * - check CRC here
 */
public class TCPMapperRequestDemarshaller
{
	//input
	private byte[] stream;

	//buffer
	private ByteBuffer buffer;

	//type (0:error, 1: request, 2: reply)
	private short type;

	//Request type (0:register, 1:lookup, 2: re-register)
	private short requestType;

	//body for lookup
	private long programNumber;
	private long programVersion;
	private long procedureNumber;
	//extras for register
	private int ip1;
	private int ip2;
	private int ip3;
	private int ip4;
	private int port;

	//crc
	private CRC32 crcHandler;


	//error handling
	private boolean requestReady;
	private boolean crcError;

	public TCPMapperRequestDemarshaller()
	{
		stream = null;
		buffer = null;
		requestReady = crcError = false;
		crcHandler = new CRC32();
		type = requestType = -1;
		programNumber = programVersion = procedureNumber = -1;
		ip1 = ip2 = ip3 = ip4 = -1;
		port = -1;
	}

	private void resetAll()
	{
		stream = null;
		buffer = null;
		requestReady = crcError = false;
		crcHandler.reset();
		type = requestType = -1;
		programNumber = programVersion = procedureNumber = -1;
		ip1 = ip2 = ip3 = ip4 = -1;
		port = -1;
	}


	private void demarshalRequest()
	{
		long recievedCRC = -1;
		long calculatedCRC = -1;

		buffer = ByteBuffer.wrap(stream);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.rewind();

		//calculate crc
		buffer.position(stream.length - 8);
		recievedCRC = buffer.getLong();
		buffer.rewind();
		byte[] dataStream = new byte[stream.length - 8];
		buffer.get(dataStream, 0, dataStream.length);
		crcHandler.reset();
		crcHandler.update(dataStream, 0, dataStream.length);
		calculatedCRC = crcHandler.getValue();
		buffer.rewind();


		if(recievedCRC == calculatedCRC)
		{
			type = buffer.getShort();
			if(type == 1)
			{
				requestType = buffer.getShort();
				switch(requestType)
				{
				case 0://register
					ip1 = buffer.getInt();
					ip2 = buffer.getInt();
					ip3 = buffer.getInt();
					ip4 = buffer.getInt();
					port = buffer.getInt();
					programNumber = buffer.getLong();
					programVersion = buffer.getLong();
					procedureNumber = buffer.getLong();
					break;
				case 1://lookup
					programNumber = buffer.getLong();
					programVersion = buffer.getLong();
					procedureNumber = buffer.getLong();
					break;
				case 2://re-register
					ip1 = buffer.getInt();
					ip2 = buffer.getInt();
					ip3 = buffer.getInt();
					ip4 = buffer.getInt();
					port = buffer.getInt();	
					programNumber = buffer.getLong();
					programVersion = buffer.getLong();
					procedureNumber = buffer.getLong();
					break;
				default:
					//something
					break;
				}
				requestReady = true;
			}else if(type == 3)
			{
				requestType = buffer.getShort();

				ip1 = buffer.getInt();
				ip2 = buffer.getInt();
				ip3 = buffer.getInt();
				ip4 = buffer.getInt();
				port = buffer.getInt();
				programNumber = buffer.getLong();
				programVersion = buffer.getLong();
				procedureNumber = buffer.getLong();
			}
			requestReady = true;
		}
		else
			crcError = true;

		buffer.clear();
	}

	public void setStream(byte[] stream) {
		resetAll();
		this.stream = stream;
		demarshalRequest();
	}

	public short getType() {
		return type;
	}

	public short getRequestType() {
		return requestType;
	}

	public long getProgramNumber() {
		return programNumber;
	}

	public long getProgramVersion() {
		return programVersion;
	}

	public long getProcedureNumber() {
		return procedureNumber;
	}

	public int getIp1() {
		return ip1;
	}

	public int getIp2() {
		return ip2;
	}

	public int getIp3() {
		return ip3;
	}

	public int getIp4() {
		return ip4;
	}

	public int getPort() {
		return port;
	}

	public boolean isRequestReady() {
		return requestReady;
	}

	public boolean isCRCError() {
		return crcError;
	}

}
