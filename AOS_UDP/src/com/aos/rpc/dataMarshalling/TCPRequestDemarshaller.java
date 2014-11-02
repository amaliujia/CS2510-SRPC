package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;


public class TCPRequestDemarshaller
{
	//input
	private byte[] stream;
	
	//buffer
	private ByteBuffer buffer;
	
	//type (0:error, 1:request, 2:reply)
	private short type;
	
	//header
	private long transactionID;
	private long programNumber;
	private long programVersion;
	private long procedureNumber;
	private long numberOfElements1_r;	
	private long numberOfElements1_c;
	private long numberOfElements2_r;	
	private long numberOfElements2_c;	
	private long numOfPackets;	

	
	//error handling
	private boolean requestReady;
	private boolean crcError;
	
	//crc
	private CRC32 crcHandler;

	
	public TCPRequestDemarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		requestReady = crcError = false;
		transactionID = programNumber = programVersion = procedureNumber = -1;
		type = -1;
		numOfPackets = -1;
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
		
		//move on with rest
		if(recievedCRC == calculatedCRC)
		{
			type = buffer.getShort();
			if(type == 1)
			{
				transactionID = buffer.getLong();
				programNumber = buffer.getLong();
				programVersion = buffer.getLong();
				procedureNumber = buffer.getLong();
				numberOfElements1_r = buffer.getLong();
				numberOfElements1_c = buffer.getLong();
				numberOfElements2_r = buffer.getLong();
				numberOfElements2_c = buffer.getLong();
				numOfPackets = buffer.getLong();
				requestReady = true;
			}
		}
		else
			crcError = true;
		
		buffer.clear();
	}
	
	private void resetAll()
	{
		stream = null;
		buffer = null;
		crcHandler.reset();
		requestReady = crcError = false;
		transactionID = programNumber = programVersion = procedureNumber = -1;
		type = -1;
	}
	
	public void setStream(byte[] stream) {
		resetAll();
		this.stream = stream;
		demarshalRequest();
	}

	public long getTransactionID() {
		return transactionID;
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

	public long getNumOfPackets() {
		return numOfPackets;
	}


	public boolean isRequestReady() {
		return requestReady;
	}
	
	public boolean isCRCError() {
		return crcError;
	}

}