package com.aos.rpc.dataMarshalling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class TCPRequestMarshaller
{
	//input
	private byte[] stream;
	
	//buffer
	private ByteBuffer buffer;
	
	//type (0:error, 1:request, 2:reply)
	private short type;
	
	//data
	private long transactionID;
	private long programNumber;
	private long programVersion;
	private long procedureNumber;
	private long numberOfElements1_r;	
	private long numberOfElements1_c;
	private long numberOfElements2_r;	
	private long numberOfElements2_c;	
	private long numOfPackets;	

		
	//crc
	private long crcValue;
	private CRC32 crcHandler;
		
	//error handling
	private boolean requestReady;

	
	public TCPRequestMarshaller()
	{
		stream = null;
		buffer = null;
		crcHandler = new CRC32();
		requestReady = false;
		transactionID = programNumber = programVersion = procedureNumber = crcValue = -1;
		type = -1;
		numOfPackets = -1;
	}
	
	private void computeCRC(byte[] dataStream)
	{
		crcHandler.reset();
		crcHandler.update(dataStream, 0, dataStream.length);
		crcValue = crcHandler.getValue();
	}

	
	private void marshalRequest()
	{
		int byteSize = 74;
		byte[] tempStream = new byte[byteSize];
		
		buffer = ByteBuffer.allocate(byteSize + 8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.putShort(type);
		buffer.putLong(transactionID);
		buffer.putLong(programNumber);
		buffer.putLong(programVersion);
		buffer.putLong(procedureNumber);
		
		buffer.putLong(numberOfElements1_r);
		buffer.putLong(numberOfElements1_c);
		buffer.putLong(numberOfElements2_r);
		buffer.putLong(numberOfElements2_c);
		buffer.putLong(numOfPackets);

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
	
	public void setType(short type) {
		this.type = type;
	}

	public void setTransactionID(long transactionID) {
		this.transactionID = transactionID;
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

	public void setNumOfPackets(long numOfPackets) {
		this.numOfPackets = numOfPackets;
	}


	public boolean formStream()
	{
		boolean result = false;
		boolean flag = (programNumber != -1) && (programVersion != -1) && 
				(transactionID != -1) && (procedureNumber != -1) && (numberOfElements1_r != -1) && 
				(numberOfElements1_c != -1) && (numberOfElements2_r != -1) &&
				(numberOfElements2_c != -1) && (transactionID != -1) &&(type != -1);
		if(flag)
		{
			
			marshalRequest();
			result = true;
		}
		return result;
	}
}
