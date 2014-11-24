package com.aos.rpc.dataMarshalling;




import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;


public class UDPMarshaller
{
	//input
	private byte[] stream;
	
	//buffer
	private ByteBuffer buffer;
		
	//vector size
	private long vectorSize;
	
	//data
	private long sequenceID;
	
	private long transactionID;
	private double[] vector;
	
	//type 0 -- error, 1 -- data, 2 -- ack 
	private short type;

		
	//crc
	private long crcValue;
	private CRC32 crcHandler;
		
	//error handling
	private boolean udpReady;

	
	public UDPMarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		udpReady = false;
		transactionID = sequenceID = vectorSize = crcValue = -1;
		type = -1;
		vector = null;
	}
	
	private void computeCRC(byte[] dataStream)
	{
		crcHandler.reset();
		crcHandler.update(dataStream, 0, dataStream.length);
		crcValue = crcHandler.getValue();
	}

	
	private void marshalRequest()
	{
		int byteSize = 8 * (int)vectorSize + 18;
		byte[] tempStream = new byte[byteSize];
		
		buffer = ByteBuffer.allocate(byteSize + 8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putLong(sequenceID);
		buffer.putLong(transactionID);
		buffer.putShort(type);
		
		for(int i = 0; i < vectorSize; i++)
			buffer.putDouble(vector[i]);
		
		buffer.rewind();
		
		buffer.get(tempStream);
		computeCRC(tempStream);
		
		buffer.position(byteSize);
		buffer.putLong(crcValue);
		buffer.rewind();
		stream = new byte[byteSize + 8];
		buffer.get(stream);
		buffer.clear();
		udpReady = true;
	}
	
	public void resetAll()
	{
		stream = null;
		buffer = null;
		crcHandler.reset();
		udpReady = false;
		transactionID = sequenceID = vectorSize = crcValue = -1;
		type = -1;
		vector = null;
	}

	
	public byte[] getStream() {
		return stream;
	}
	
	public boolean isUDPReady() {
		return udpReady;
	}
	
	public void setSequenceID(long sequenceID) {
		this.sequenceID = sequenceID;
	}
	
	public void setType(short type) {
		this.type = type;
	}

	public void setTransactionID(long transactionID) {
		this.transactionID = transactionID;
	}

	public void setVector(double[] vector) {
		this.vector = vector;
	}

	public void setVectorSize (long vectorSize)
	{
		this.vectorSize = vectorSize;
	}
	
	public short getType()
	{
		return type;
	}

	public boolean formStream()
	{
		boolean result = false;
		boolean flag =  
				(transactionID != -1) && (sequenceID != -1) && (vectorSize != -1) 
				&& (type != -1);
		if(flag)
		{
			marshalRequest();
			result = true;
		}
		return result;
	}
}
