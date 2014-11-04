package com.aos.rpc.dataMarshalling;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class UDPUnmarshaller
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
	private boolean crcError;
	private CRC32 crcHandler;
			
	//error handling
	private boolean udpReady;

	
	public UDPUnmarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		udpReady = crcError = false;
		transactionID = sequenceID = vectorSize = -1;
		vector = null;
		type = -1;
	}
	
	private void unmarshalRequest()
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
			sequenceID = buffer.getLong();
			transactionID = buffer.getLong();
			type = buffer.getShort();
					
			if (vectorSize > 0)
			{
				vector = new double[(int)vectorSize];
				for(int i = 0; i < vectorSize; i++)
					vector[i] = buffer.getDouble();				
			}
			udpReady = true;		
		}
		else
			crcError = true;
		
		buffer.clear();
	}
	
	public void resetAll()
	{
		stream = null;
		buffer = null;
		crcHandler.reset();
		udpReady = crcError = false;
		transactionID = sequenceID = vectorSize = -1;
		vector = null;
		type = -1;
	}
	
	public void setStream(byte[] stream) {
		resetAll();
		this.stream = stream;
		vectorSize = (this.stream.length - 26) / 8;
		unmarshalRequest();
	}

	public long getTransactionID() {
		return transactionID;
	}

	public long getSequenceID() {
		return sequenceID;
	}

	public short getType ()
	{
		return type;
	}


	public double[] getVector() {
		return vector;
	}
	
	public boolean isUDPReady() {
		return udpReady;
	}
	
	public boolean isCRCError() {
		return crcError;
	}

}