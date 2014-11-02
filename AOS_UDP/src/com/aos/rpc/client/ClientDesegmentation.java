package com.aos.rpc.client;

import com.aos.rpc.dataMarshalling.UDPDemarshaller;

/**
 * Created by zhangchi on Oct/29/14.
 */
public class ClientDesegmentation
{
    private UDPDemarshaller[] udpDemarshallers;
    private long rowSize, columnSize;
    private double[] vector;
    private double[][] vectorFinal;
    public ClientDesegmentation (UDPDemarshaller[] demarshallers)
    {
        udpDemarshallers = demarshallers;
    }

    public void reorganize ()
    {
        vector = new double [(int)rowSize * (int)columnSize];
        vectorFinal = new double[(int)rowSize][(int)columnSize];

        int previousIndex = 0;
        int i, j;
        for ( i = 0; i < udpDemarshallers.length; i ++)
        {

            int streamSize = udpDemarshallers[i].getVector().length;
            for ( j = 0; j < streamSize; j++)
            {
                vector[previousIndex + j] = udpDemarshallers[i].getVector()[j];

            }
            previousIndex = previousIndex + j;
        }


        for (i = 0; i < rowSize; i ++)
            for (j = 0; j < columnSize; j ++)
            {
                vectorFinal[i][j] = vector[i * (int)columnSize + j];
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
