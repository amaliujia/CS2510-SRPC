package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class TCPReplyMarshaller
{
	//output
	private byte[] stream;
	
	//buffer
	private ByteBuffer buffer;
		
	//type (0:error, 1:request, 2:reply)
	private short type;
	
	//header
	private long transactionID;

	//number of elements
	private long numberOfElements1_r;	
	private long numberOfElements1_c;
	private long numberOfElements2_r;	
	private long numberOfElements2_c;	
	
	//data
	private double[] resultVector1;
	private double[] resultVector2;

	
	//crc
	private long crcValue;
	private CRC32 crcHandler;
		
	//error handling
	private boolean replyReady;
	
	public TCPReplyMarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		replyReady = false;
		crcValue = -1;
		transactionID = numberOfElements1_r = numberOfElements1_c = numberOfElements2_r
				= numberOfElements2_c = crcValue = -1;
		type = -1;
	}
	
	private void computeCRC(byte[] dataStream)
	{
		crcHandler.reset();
		crcHandler.update(dataStream, 0, dataStream.length);
		crcValue = crcHandler.getValue();
	}
	
	private void marshalReply()
	{
		int byteSize = 42 + 8*(int)numberOfElements1_r * (int)numberOfElements1_c +
				8*(int)numberOfElements2_r * (int)numberOfElements2_c;
		byte[] tempStream = new byte[byteSize];
		
		buffer = ByteBuffer.allocate(byteSize + 8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putShort(type);
		buffer.putLong(transactionID);
		
		buffer.putLong(numberOfElements1_r);
		buffer.putLong(numberOfElements1_c);
		for(int i = 0; i < numberOfElements1_r * numberOfElements1_c; i++)
			buffer.putDouble(resultVector1[i]);
		buffer.putLong(numberOfElements2_r);
		buffer.putLong(numberOfElements2_c);
		for(int i = 0; i < numberOfElements2_r * numberOfElements2_c; i++)
			buffer.putDouble(resultVector2[i]);
		
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

	public byte[] getStream() {
		return stream;
	}

	public boolean isReplyReady() {
		return replyReady;
	}

	public void setType(short type) {
		this.type = type;
	}
	
	public short getType() {
		return type;
	}


	public void setTransactionID(long transactionID) {
		this.transactionID = transactionID;
	}

	public void setNumberOfElements1_r(long numberOfElements1_r) {
		this.numberOfElements1_r = numberOfElements1_r;
	}
	public void setNumberOfElements1_c(long numberOfElements1_c) {
		this.numberOfElements1_c = numberOfElements1_c;
	}
	public void setNumberOfElements2_r(long numberOfElements2_r) {
		this.numberOfElements2_r = numberOfElements2_r;
	}
	public void setNumberOfElements2_c(long numberOfElements2_c) {
		this.numberOfElements2_c = numberOfElements2_c;
	}

	public void setResultVector1(double[] resultVector1) {
		this.resultVector1 = resultVector1;
	}
	
	public void setResultVector2(double[] resultVector2) {
		this.resultVector2 = resultVector2;
	}

	
	public boolean formStream()
	{
		boolean result = false;
		boolean flag = (resultVector1 != null) && (numberOfElements1_r != -1) && (numberOfElements1_c != -1) && 
				(numberOfElements2_r != -1) && (numberOfElements2_c != -1) && (transactionID != -1) && (type != -1);
		if(flag)
		{
			marshalReply();
			result = true;
		}
		return result;
	}
}
