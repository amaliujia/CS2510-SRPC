package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class TCPMapperReplyMarshaller
{
	//output
	private byte[] stream;

	//buffer
	private ByteBuffer buffer;

	//type (0:error, 1: request, 2: reply)
	private short type;

	//Reply type (0:register, 1:lookup, 2: re-register)
	private short replyType;

	//result for register (0:not-done, 1:done)
	private short result;	
	//result for lookup
	private int ip1;
	private int ip2;
	private int ip3;
	private int ip4;
	private int port;
	//result for re-register
	//body for lookup
	private long programNumber;
	private long programVersion;
	private long procedureNumber;
	//in addition to the ip and port

	//crc
	private long crcValue;

	//error handling
	private boolean replyReady;
	private CRC32 crcHandler;

	public TCPMapperReplyMarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		replyReady = false;
		type = replyType = -1;
		programNumber = programVersion = procedureNumber = crcValue = -1;
		ip1 = ip2 = ip3 = ip4 = -1;
		port = -1;
		result = -2;
	}

	private void computeCRC(byte[] dataStream)
	{
		crcHandler.reset();
		crcHandler.update(dataStream, 0, dataStream.length);
		crcValue = crcHandler.getValue();
	}

	private void marshalReply()
	{
		int byteSize = -1;
		byte[] tempStream = null;
		switch(replyType)
		{
		case 0://register
			byteSize = 6;
			tempStream = new byte[byteSize];
			buffer = ByteBuffer.allocate(byteSize + 8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putShort(type);
			buffer.putShort(replyType);
			buffer.putShort(result);
			break;
		case 1://lookup (-1s if not found)
			byteSize = 24;
			tempStream = new byte[byteSize];
			buffer = ByteBuffer.allocate(byteSize + 8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putShort(type);
			buffer.putShort(replyType);
			buffer.putInt(ip1);
			buffer.putInt(ip2);
			buffer.putInt(ip3);
			buffer.putInt(ip4);
			buffer.putInt(port);
			break;
		case 2://re-register
			byteSize = 6;
			tempStream = new byte[byteSize];
			buffer = ByteBuffer.allocate(byteSize + 8);
			buffer.order(ByteOrder.BIG_ENDIAN);
			buffer.putShort(type);
			buffer.putShort(replyType);
			buffer.putShort(result);
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
		replyReady = true;
	}

	public boolean isReplyReady() {
		return replyReady;
	}

	public byte[] getStream() {
		return stream;
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

	public void setReplyType(short replyType) {
		this.replyType = replyType;
	}

	public void setResult(short result) {
		this.result = result;
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

		if(replyType == 0)
			flag = this.result != -2;
		else if (replyType == 1)
			flag = 	(ip1 != -1) && (ip2 != -1) && (ip3 != -1) && (ip4 != -1) && (port != -1);
		else if (replyType == 2)
		{
			flag = this.result != -2;
		}

		if(flag)
		{
			marshalReply();
			result = true;
		}

		return result;
	}
}
