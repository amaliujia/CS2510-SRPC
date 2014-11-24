package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class TCPReplyUnmarshaller
{
	//input
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

	//error handling
	private boolean replyReady;
	private boolean crcError;

	//crc
	private CRC32 crcHandler;


	public TCPReplyUnmarshaller()
	{
		stream = null;
		resultVector1 = resultVector2 = null;
		buffer = null;
		crcHandler = new CRC32();
		replyReady = crcError = false;
		transactionID = numberOfElements1_r = numberOfElements1_c = 
				numberOfElements2_r = numberOfElements2_c= -1;
		type = -1;
	}

	private void unmarshalReply()
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
				transactionID = buffer.getLong();
				numberOfElements1_r = buffer.getLong();
				numberOfElements1_c = buffer.getLong();
				resultVector1 = new double[(int)numberOfElements1_r * (int)numberOfElements1_c];
				for(int i = 0; i < numberOfElements1_r * numberOfElements1_c; i++)
					resultVector1[i] = buffer.getDouble();
				numberOfElements2_r = buffer.getLong();
				numberOfElements2_c = buffer.getLong();
				resultVector2 = new double[(int)numberOfElements2_r * (int)numberOfElements2_c];
				for(int i = 0; i < numberOfElements2_r * numberOfElements2_c; i++)
					resultVector2[i] = buffer.getDouble();
				replyReady = true;
			}
		}
		else
			crcError = true;

		buffer.clear();
	}

	private void resetAll()
	{
		stream = null;
		resultVector1 = resultVector2 = null;
		buffer = null;
		crcHandler.reset();
		replyReady = crcError = false;
		transactionID = numberOfElements1_r = numberOfElements1_c = 
				numberOfElements2_r = numberOfElements2_c= -1;
		type = -1;
	}
	
	public void setStream(byte[] stream) {
		resetAll();
		this.stream = stream;
		unmarshalReply();
	}

	public long getTransactionID() {
		return transactionID;
	}

	public long getNumberOfElements1_r() {
		return numberOfElements1_r;
	}
	public long getNumberOfElements1_c() {
		return numberOfElements1_c;
	}
	public long getNumberOfElements2_r() {
		return numberOfElements2_r;
	}
	public long getNumberOfElements2_c() {
		return numberOfElements2_c;
	}

	public double[] getResultVector1() {
		return resultVector1;
	}
	
	public double[] getResultVector2() {
		return resultVector2;
	}

	public boolean isReplyReady() {
		return replyReady;
	}
	
	public boolean isCRCError() {
		return crcError;
	}
}