package com.aos.rpc.udpServer;
import java.util.Scanner;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

import com.aos.rpc.helperClasses.ParametersConstructor;

public class Testing {

	public static void main(String[] args) throws Exception
	{
//		double[] vector1 = new double[120];
//		double[] vector2 = new double[500];
//
//		for (int i = 0; i < vector1.length; i++)
//			vector1[i] = Math.random();
//		
//		System.out.println();
//
//		for (int i = 0; i < vector2.length; i++)
//			vector2[i] = Math.random();
//		
//		ClientSegmentation seg = new ClientSegmentation(vector1, vector2, 11111);
//		seg.processingSegmentation();
//
//		ParametersConstructor params = new ParametersConstructor();
//		params.setMarshallers(seg.getUDPMarshallers());
//		params.setNumberOfElements1_r(12);
//		params.setNumberOfElements1_c(10);
//		params.setNumberOfElements2_r(10);
//		params.setNumberOfElements2_c(50);
//		params.constructParameters();
//
//
//		double[] param1 = params.getVector1();
//		double[] param2 = params.getVector2();
//		
//		boolean flag = true;
//		for(int i = 0; i < 100; i++)
//		{
//			if(vector1[i] != param1[i])
//				flag = false;
//			if(vector2[i] != param2[i])
//				flag = false;
//		}
//		System.out.println(flag);
		//		Scanner s = new Scanner(System.in);
		////		ProgramLibrary p = new ProgramLibrary();
		////		
		//		double[][] matrix1 = new double[2][5];
		////		double[][] matrix2 = new double[4][2];
		////		double[][] result = new double[3][2];
		////		
		////		
		//		for (int i = 0; i < matrix1.length; i++) {
		//	           for (int j = 0; j < matrix1[0].length; j++) {
		//	        	   matrix1[i][j] = s.nextInt();
		//	           }
		//		}
		////	           
		////	   		for (int i = 0; i < matrix2.length; i++) {
		////		           for (int j = 0; j < matrix2[0].length; j++) {
		////		        	   matrix2[i][j] = s.nextInt();
		////		           }
		////	   		}
		////	          
		////		
		////		result = p.multiply(matrix1, matrix2);
		////
		////		
		//		System.out.println("result:");
		//		for (int i = 0; i < 2; i++) {
		//			for (int j = 0; j < 5; j++) {
		//				System.out.print(matrix1[i][j] + " ");
		//			}
		//			System.out.println();
		//		}
		//		
		//		MatrixResolver r = new MatrixResolver();
		//		r.setMatrix(matrix1);
		//		double[] mtov = r.getVector();
		//		
		//		System.out.println();
		//		System.out.println("r: " + matrix1.length + " c: " + matrix1[0].length);
		//		
		//		for(int i = 0; i < mtov.length; i++)
		//			System.out.println(mtov[i]);


		//		ProgramLibrary p = new ProgramLibrary();
		//		double[] temp = new double[15];
		//		for(int i = 0; i < temp.length; i++)
		//			temp[i] = Math.random();
		//		for(int i = 0; i < temp.length; i++)
		//			System.out.println(temp[i]);
		//		System.out.println();
		//		System.out.println();
		//		p.sort(temp);
		//		for(int i = 0; i < temp.length; i++)
		//			System.out.println(temp[i]);

		//		Socket socket = new Socket();
		//		socket.);
		//		System.out.println(socket.getInetAddress());

		//		
		//		System.out.println(p.sayHello_01());
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
