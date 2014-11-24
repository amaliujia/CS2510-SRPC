package com.aos.rpc.helperClasses;

import com.aos.rpc.dataMarshalling.UDPUnmarshaller;

/**
 * Created by zhangchi on Oct/29/14.
 */
public class ClientDesegmentation
{
	private UDPUnmarshaller[] udpUnmarshallers;
	private long rowSize, columnSize;
	private double[] vector = null;
	private double[][] vectorFinal = null;
	public ClientDesegmentation (UDPUnmarshaller[] unmarshallers)
	{
		udpUnmarshallers = unmarshallers;
	}

	public void reorganize ()
	{
		vector = new double [(int)rowSize * (int)columnSize];
		vectorFinal = new double[(int)rowSize][(int)columnSize];

		int previousIndex = 0;
		int i, j;

		if (udpUnmarshallers[0].getType() == 0)
		{
			System.out.println("Error: Execution time exception occured.");
			this.vectorFinal = null;
		}

		else 
		{
			for ( i = 0; i < udpUnmarshallers.length; i ++)
			{

				int streamSize = udpUnmarshallers[i].getVector().length;
				for ( j = 0; j < streamSize; j++)
				{
					vector[previousIndex + j] = udpUnmarshallers[i].getVector()[j];

				}
				previousIndex = previousIndex + j;
			}


			for (i = 0; i < rowSize; i ++)
				for (j = 0; j < columnSize; j ++)
				{
					vectorFinal[i][j] = vector[i * (int)columnSize + j];
				}

		}
	}

	public void setRowSize (long rowSize)
	{
		this.rowSize = rowSize;
	}

	public void setColumnSize (long columnSize)
	{
		if (columnSize == 0)
			this.columnSize = 1;
		else
			this.columnSize = columnSize;
	}

	public double[][] getVectorFinal ()
	{
		return vectorFinal;
	}

}
