package com.aos.rpc.helperClasses;


import com.aos.rpc.dataMarshalling.UDPMarshaller;

/**
 * Created by zhangchi on Oct/28/14.
 */
public class ServerSegmentation
{
	private double[] vector1, vector2;
	private long tranID;
	private UDPMarshaller[] udpMarshallers;
	public ServerSegmentation (double[] vector1, double[] vector2, long tranID)
	{
		this.vector1 = vector1;
		this.vector2 = vector2;
		this.tranID = tranID;
		udpMarshallers = null;
	}

	public void processingSegmentation ()
	{
        int i, j;
        int vectorTotalLength = 0;
        double[] vectorTotal;

		if(vector1 == null && vector2 == null)
		{
			udpMarshallers = new UDPMarshaller[1];
			udpMarshallers[0] = new UDPMarshaller();
			udpMarshallers[0].resetAll();
			udpMarshallers[0].setSequenceID(1);
			udpMarshallers[0].setTransactionID(tranID);
			udpMarshallers[0].setType((short)0);
			udpMarshallers[0].setVectorSize(0);
			udpMarshallers[0].setVector(null);
		}
		else
		{
	        if (vector2 != null)
	        {
	            vectorTotal = new double[vector1.length + vector2.length];

	            vectorTotalLength = vectorTotal.length;
	            for (i = 0; i < vector1.length; i++)
	                vectorTotal[i] = vector1[i];
	            int pad = vector1.length;
	            for (; i < vectorTotalLength; i++)
	                vectorTotal[i] = vector2[i - pad];
	        }
	        else
	        {
	            vectorTotal = new double[vector1.length];

	            vectorTotalLength = vectorTotal.length;
	            for (i = 0; i < vector1.length; i++)
	                vectorTotal[i] = vector1[i];
	        }

	        int reminderOfLast = vectorTotalLength % 512;
	        int vectorEntries;

	        if (reminderOfLast == 0)
	        {
	            vectorEntries = vectorTotalLength / 512;
	        }
	        else
	        {
	            vectorEntries = vectorTotalLength / 512 + 1;
	        }

	        double[][] vectorTwoDim = new double[vectorEntries][512];


	        for (i = 0; i < vectorEntries; i ++)
	        {
	            for (j = 0; j < 512 && (i * 512 + j) < vectorTotalLength; j ++)
	            {
	                vectorTwoDim[i][j] = vectorTotal[i * 512 + j];
	            }
	        }

	        udpMarshallers = new UDPMarshaller[vectorEntries];
	        for (i = 0; i < vectorEntries; i ++ )
	        {
	            udpMarshallers[i] = new UDPMarshaller();
	            udpMarshallers[i].resetAll();
	            udpMarshallers[i].setSequenceID(i+1);
	            udpMarshallers[i].setTransactionID(tranID);
	            udpMarshallers[i].setType((short)1);
	            udpMarshallers[i].setVectorSize(512);
	            udpMarshallers[i].setVector(vectorTwoDim[i]);


	            if (i == (vectorEntries - 1) && reminderOfLast != 0)
	            {
	                udpMarshallers[i].setVectorSize(reminderOfLast);
	            }

	            if (udpMarshallers[i].formStream() == false)
	                System.out.println("Failed in forming streams of udpMarshallers!!!");
	        }
		}
	}

	public UDPMarshaller[] getUDPMarshallers ()
	{
		return udpMarshallers;
	}
}
