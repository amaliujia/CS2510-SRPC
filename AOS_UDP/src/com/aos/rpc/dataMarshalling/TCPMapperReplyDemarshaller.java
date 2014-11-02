package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class TCPMapperReplyDemarshaller
{
	//input
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
	
	//error handling
	private boolean replyReady;
	private boolean crcError;

	//crc
	private CRC32 crcHandler;


	public TCPMapperReplyDemarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		type = replyType = result = -1;
		replyReady = crcError = false;
		programNumber = programVersion = procedureNumber = -1;
		ip1 = ip2 = ip3 = ip4 = port = -1;
	}
	
	private void resetAll()
	{
		stream = null;
		buffer = null;
		crcHandler.reset();
		type = replyType = result = -1;
		replyReady = crcError = false;
		programNumber = programVersion = procedureNumber = -1;
		ip1 = ip2 = ip3 = ip4 = port = -1;
	}


	private void demarshalReply()
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

		//move on with rest
		if(recievedCRC == calculatedCRC)
		{
			type = buffer.getShort();
			if(type == 2)
			{
				replyType = buffer.getShort();
				switch(replyType)
				{
				case 0://register
					result = buffer.getShort();
					break;
				case 1://lookup
					ip1 = buffer.getInt();
					ip2 = buffer.getInt();
					ip3 = buffer.getInt();
					ip4 = buffer.getInt();
					port = buffer.getInt();
					break;
				case 2://re-register
					result = buffer.getShort();
					break;
				default:
					//something
					break;
				}
				replyReady = true;
			}
		}
		else
			crcError = true;

		buffer.clear();
	}

	public void setStream(byte[] stream) {
		resetAll();
		this.stream = stream;
		demarshalReply();
	}

	public short getType() {
		return type;
	}

	public short getReplyType() {
		return replyType;
	}

	public short getResult() {
		return result;
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

	public long getProgramNumber() {
		return programNumber;
	}

	public long getProgramVersion() {
		return programVersion;
	}

	public long getProcedureNumber() {
		return procedureNumber;
	}

	public boolean isReplyReady() {
		return replyReady;
	}

	public boolean isCrcError() {
		return crcError;
	}
	
	
}