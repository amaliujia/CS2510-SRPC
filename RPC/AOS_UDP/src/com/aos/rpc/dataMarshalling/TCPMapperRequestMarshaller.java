package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class TCPMapperRequestMarshaller
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
	private long crcValue;
	private CRC32 crcHandler;

	//error handling
	private boolean requestReady;


	public TCPMapperRequestMarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		requestReady = false;
		type = requestType = -1;
		programNumber = programVersion = procedureNumber = crcValue = -1;
		ip1 = ip2 = ip3 = ip4 = port = -1;
	}

	private void computeCRC(byte[] dataStream)
	{
		crcHandler.reset();
		crcHandler.update(dataStream, 0, dataStream.length);
		crcValue = crcHandler.getValue();
	}

	private void marshalRequest()
	{
		int byteSize = -1;
		byte[] tempStream = null;
		switch(requestType)
		{
		case 0://register
			byteSize = 48;
			tempStream = new byte[byteSize];
			buffer = ByteBuffer.allocate(byteSize + 8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putShort(type);
			buffer.putShort(requestType);
			buffer.putInt(ip1);
			buffer.putInt(ip2);
			buffer.putInt(ip3);
			buffer.putInt(ip4);
			buffer.putInt(port);
			buffer.putLong(programNumber);
			buffer.putLong(programVersion);
			buffer.putLong(procedureNumber);
			break;
		case 1://lookup
			byteSize = 28;
			tempStream = new byte[byteSize];
			buffer = ByteBuffer.allocate(byteSize + 8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putShort(type);
			buffer.putShort(requestType);
			buffer.putLong(programNumber);
			buffer.putLong(programVersion);
			buffer.putLong(procedureNumber);
			break;
		case 2://re-register
			byteSize = 48;
			tempStream = new byte[byteSize];
			buffer = ByteBuffer.allocate(byteSize + 8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putShort(type);
			buffer.putShort(requestType);
			buffer.putInt(ip1);
			buffer.putInt(ip2);
			buffer.putInt(ip3);
			buffer.putInt(ip4);
			buffer.putInt(port);
			buffer.putLong(programNumber);
			buffer.putLong(programVersion);
			buffer.putLong(procedureNumber);	
			break;
		default:
			//something
			break;
		}
		buffer.rewind();
		buffer.get(tempStream);
		computeCRC(tempStream);
		buffer.position(byteSize);
		buffer.putLong(crcValue);
		buffer.rewind();
		stream = new byte[byteSize + 8];
		buffer.get(stream);
		buffer.clear();
		requestReady = true;
	}

	public byte[] getStream() {
		return stream;
	}

	public boolean isRequestReady() {
		return requestReady;
	}

	public void setIPsWithPort(int ip1, int ip2, int ip3, int ip4, int port)
	{
		this.ip1 = ip1;
		this.ip2 = ip2;
		this.ip3 = ip3;
		this.ip4 = ip4;
		this.port = port;
	}

	
	public void setType(short type) {
		this.type = type;
	}

	public void setRequestType(short requestType) {
		this.requestType = requestType;
	}

	public void setProgramNumber(long programNumber) {
		this.programNumber = programNumber;
	}

	public void setProgramVersion(long programVersion) {
		this.programVersion = programVersion;
	}

	public void setProcedureNumber(long procedureNumber) {
		this.procedureNumber = procedureNumber;
	}

	public boolean formStream()
	{
		boolean result = false;
		boolean flag = false;
		
		if(requestType == 0 || requestType == 2)
		{
			flag = 	(ip1 != -1) && (ip2 != -1) && (ip3 != -1) && (ip4 != -1) && (port != -1);
			flag = flag && (programNumber != -1) && (programVersion != -1) && (procedureNumber != -1);
		}
		else if (requestType == 1)
			flag = 	(programNumber != -1) && (programVersion != -1) && (procedureNumber != -1);

		if(flag)
		{
			marshalRequest();
			result = true;
		}
		return result;
	}
}
