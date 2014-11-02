package com.aos.rpc.dataMarshalling;

import java.util.Scanner;

import com.aos.rpc.server.MatrixResolver;

public class Testing {

	public static void main(String[] args) throws Exception
	{
//		Scanner s = new Scanner(System.in);
//		double[][] matrix1 = new double[100][100];
//		for (int i = 0; i < matrix1.length; i++) {
//			for (int j = 0; j < matrix1[0].length; j++) {
//				matrix1[i][j] = Math.random();
//			}
//		}
		
		
//		System.out.println("result:");
//		for (int i = 0; i < 2; i++) {
//			for (int j = 0; j < 5; j++) {
//				System.out.print(matrix1[i][j] + " ");
//			}
//			System.out.println();
//		}
		
//		MatrixResolver r = new MatrixResolver();
//		r.setMatrix(matrix1);
//		double[] mtov = r.getVector();
//		
//		System.out.println();
//		System.out.println("r: " + matrix1.length + " c: " + matrix1[0].length);
//		
//		for(int i = 0; i < mtov.length; i++)
//			System.out.println(mtov[i]);
//
//
//		
//		
//		
//		TCPRequestMarshaller reqMar = new TCPRequestMarshaller();
//		reqMar.setType((short)1);
//		reqMar.setProgramNumber(123);
//		reqMar.setProgramVersion(456);
//		reqMar.setProcedureNumber(789);
//		reqMar.setTransactionID(100000101);
//		reqMar.setNumberOfElements1_r(matrix1.length);
//		reqMar.setNumberOfElements1_c(matrix1[0].length);
//		reqMar.setNumberOfElements2_r(0);
//		reqMar.setNumberOfElements2_c(0);
//		reqMar.setVector1(mtov);
//		reqMar.formStream();
//		System.out.println(reqMar.isRequestReady());
////		
//		TCPRequestDemarshaller reqDem = new TCPRequestDemarshaller();
//		reqDem.setStream(reqMar.getStream());
//		System.out.println(reqDem.getProgramNumber());
//		System.out.println(reqDem.getProgramVersion());
//		System.out.println(reqDem.getProcedureNumber());
//		System.out.println(reqDem.getTransactionID());
//		System.out.println(reqDem.getNumberOfElements1_r());
//		System.out.println(reqDem.getNumberOfElements1_c());
//		double[] rec = reqDem.getVector1();
//				
//		for(int i = 0; i < rec.length; i++)
//			System.out.println(rec[i]);
//
//		System.out.println();
//		System.out.println();
//		
//		double[] arr = new double[1000];
//		for(int i = 0; i < 1000; i++)
//		{
//			arr[i] = Math.random();
//			System.out.println(arr[i]);
//		}
//		
//
//		TCPReplyMarshaller repMar = new TCPReplyMarshaller();
//		repMar.setType((short)2);
//		repMar.setNumberOfElements(1000);
//		repMar.setResultVector(arr);
//		repMar.setTransactionID(10002000);
//		repMar.formStream();
//		System.out.println(repMar.isReplyReady());
//		
//		
//		TCPReplyDemarshaller dem = new TCPReplyDemarshaller();
//		dem.setStream(repMar.getStream());
//		System.out.println(dem.getTransactionID());
//		System.out.println(dem.getNumberOfElements());
//		double[] recarr = dem.getResultVector();
//		for(int i = 0; i < dem.getNumberOfElements(); i++)
//			System.out.println(recarr[i]);
		
//		TCPMapperRequestMarshaller m = new TCPMapperRequestMarshaller();
//		m.setIPsWithPort(212, 198, 50, 53, 21009);
//		m.setProgramNumber(123);
//		m.setProgramVersion(456);
//		m.setProcedureNumber(789);
//		m.setType((short)1);
//		m.setRequestType((short)0);
//		m.formStream();
//		System.out.println(m.isRequestReady());
//		
//		
//		TCPMapperRequestDemarshaller d = new TCPMapperRequestDemarshaller();
//		d.setStream(m.getStream());
//		System.out.println(d.isCRCError());
//		System.out.println(d.getIp1());
//		System.out.println(d.getIp2());
//		System.out.println(d.getIp3());
//		System.out.println(d.getIp4());
//		System.out.println(d.getPort());
//		System.out.println(d.getProgramNumber());
//		System.out.println(d.getProgramVersion());
//		System.out.println(d.getProcedureNumber());
//		System.out.println(d.isRequestReady());

//		TCPMapperRequestMarshaller m = new TCPMapperRequestMarshaller();
//		m.setProgramNumber(123);
//		m.setProgramVersion(456);
//		m.setProcedureNumber(789);
//		m.setType((short)1);
//		m.setRequestType((short)1);
//		m.formStream();
//		System.out.println(m.isRequestReady());
//		
//		
//		TCPMapperRequestDemarshaller d = new TCPMapperRequestDemarshaller();
//		d.setStream(m.getStream());
//		System.out.println(d.isCRCError());
////		System.out.println(d.getIp1());
////		System.out.println(d.getIp2());
////		System.out.println(d.getIp3());
////		System.out.println(d.getIp4());
////		System.out.println(d.getPort());
//		System.out.println(d.getProgramNumber());
//		System.out.println(d.getProgramVersion());
//		System.out.println(d.getProcedureNumber());
//		System.out.println(d.isRequestReady());
		
//		TCPMapperRequestMarshaller m = new TCPMapperRequestMarshaller();
//		m.setIPsWithPort(212, 198, 50, 53, 21009);
//		m.setProgramNumber(123);
//		m.setProgramVersion(456);
//		m.setProcedureNumber(789);
//		m.setType((short)1);
//		m.setRequestType((short)2);
//		m.formStream();
//		System.out.println(m.isRequestReady());
//		
//		
//		TCPMapperRequestDemarshaller d = new TCPMapperRequestDemarshaller();
//		d.setStream(m.getStream());
//		System.out.println(d.isCRCError());
//		System.out.println(d.getIp1());
//		System.out.println(d.getIp2());
//		System.out.println(d.getIp3());
//		System.out.println(d.getIp4());
//		System.out.println(d.getPort());
//		System.out.println(d.getProgramNumber());
//		System.out.println(d.getProgramVersion());
//		System.out.println(d.getProcedureNumber());
//		System.out.println(d.isRequestReady());
		
//		TCPMapperReplyMarshaller m = new TCPMapperReplyMarshaller();
//		m.setResult((short)-1);
//		m.setType((short)2);
//		m.setReplyType((short)0);
//		m.formStream();
//		System.out.println(m.isReplyReady());
//		
//		
//		TCPMapperReplyDemarshaller d = new TCPMapperReplyDemarshaller();
//		d.setStream(m.getStream());
//		System.out.println(d.isCrcError());
//		System.out.println(d.getResult());
//		System.out.println(d.isReplyReady());

//		TCPMapperReplyMarshaller m = new TCPMapperReplyMarshaller();
//		m.setIPsWithPort(101, 102, 103, 104, 4000);
//		m.setType((short)2);
//		m.setReplyType((short)1);
//		m.formStream();
//		System.out.println(m.isReplyReady());
//		
//		
//		TCPMapperReplyDemarshaller d = new TCPMapperReplyDemarshaller();
//		d.setStream(m.getStream());
//		System.out.println(d.isCrcError());
//		System.out.println(d.getIp1() + " " + d.getIp2() + " " + d.getIp3() + " " + d.getIp4() + " " + d.getPort());
//		System.out.println(d.isReplyReady());
		
		
//		TCPMapperReplyMarshaller m = new TCPMapperReplyMarshaller();
//		m.setIPsWithPort(101, 102, 103, 104, 4000);
//		m.setProgramNumber(123);
//		m.setProgramVersion(456);
//		m.setProcedureNumber(789);
//		m.setType((short)2);
//		m.setReplyType((short)2);
//		m.formStream();
//		System.out.println(m.isReplyReady());
//		
//		
//		TCPMapperReplyDemarshaller d = new TCPMapperReplyDemarshaller();
//		d.setStream(m.getStream());
//		System.out.println(d.isCrcError());
//		System.out.println(d.getIp1() + " " + d.getIp2() + " " + d.getIp3() + " " + d.getIp4() + " " + d.getPort());
//		System.out.println(d.getProgramNumber());
//		System.out.println(d.getProgramVersion());
//		System.out.println(d.getProcedureNumber());
//		System.out.println(d.isReplyReady());
		
////		Program_01_01 p = new Program_01_01();
////		double[] temp = {1};
////		
////		System.out.println(p.sayHello_01());
//		CRC32 crcHandler = new CRC32();
//
//		
//		String text = "Byte Buffer Test";
//        byte[] attributeValue = text.getBytes();
//
//        long lastModifiedDate = 1289811105109L;
//        short employeeId = 32767;
//
//        int size = 2 + 8 + 4 + attributeValue.length; // short is 2 bytes, long 8 and int 4
//
//        ByteBuffer bbuf = ByteBuffer.allocate(size + 8); 
//
//        bbuf.order(ByteOrder.BIG_ENDIAN);
//        bbuf.putShort(employeeId);
//        bbuf.putLong(lastModifiedDate);
//        bbuf.putInt(attributeValue.length);
//        bbuf.put(attributeValue);
//
//        bbuf.rewind();
//        
//        byte[] temp = new byte[2 + 8 + 4 + attributeValue.length];
//        bbuf.get(temp);
//        
//        
//        crcHandler.update(temp, 0 , temp.length);
//        long crc = crcHandler.getValue();
//        System.out.println("crc is: " + crc);
//        bbuf.position(2 + 8 + 4 + attributeValue.length);
//        bbuf.putLong(crc);
//        
//        bbuf.rewind();
//
//        // best approach is copy the internal buffer
//        byte[] bytesToStore = new byte[size + 8];
//        bbuf.get(bytesToStore);
//
//        
//        
//        
//        
//        // write bytesToStore in Cassandra...
//
//        // Now retrieve the Byte Array data from Cassandra and deserialize it...
//        byte[] allWrittenBytesTest = bytesToStore;//magicFunctionToRetrieveDataFromCassandra();
//
//        ByteBuffer bb = ByteBuffer.wrap(allWrittenBytesTest);
//
//        bb.order(ByteOrder.BIG_ENDIAN);
//        bb.rewind();
//        
//        bb.position(allWrittenBytesTest.length - 8);
//        crc = bb.getLong();
//        bb.rewind();
//        byte[] data = new byte[allWrittenBytesTest.length - 8];
//        bb.get(data, 0, data.length);
//        
//        
//        crcHandler.reset();
//        crcHandler.update(data, 0, data.length);
//        ;
//        if (crc == crcHandler.getValue())
//        	System.out.println("dgbsdngdhblsgkbgmdlmd GO AHEEEEEEEAD");
//        
//        bb.rewind();
//        
//
//        short extractEmployeeId = bb.getShort();
//        long extractLastModifiedDate = bb.getLong();
//        int extractAttributeValueLength = bb.getInt();
//        byte[] extractAttributeValue = new byte[extractAttributeValueLength];
//
//        bb.get(extractAttributeValue, 0, extractAttributeValue.length); // read attributeValue from the remaining buffer
//        crc = bb.getLong();
//        
//        
//        
//        System.out.println(extractEmployeeId);
//        System.out.println(extractLastModifiedDate);
//        System.out.println(new String(extractAttributeValue));
//        System.out.println(crc);
	}
}
