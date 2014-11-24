package com.aos.rpc.helperClasses;


import com.aos.rpc.dataMarshalling.UDPUnmarshaller;

public class ServerDesegmentation
{
	private UDPUnmarshaller[] unmarshallers;
	private long numberOfElements1_r;	
	private long numberOfElements1_c;
	private long numberOfElements2_r;	
	private long numberOfElements2_c;
	private double[] vector1;
	private double[] vector2;
	private boolean parametersReady;
	private int numberOfVectors;
	
	public ServerDesegmentation()
	{		
		unmarshallers = null;
		vector1 = vector2 = null;
		parametersReady = false;
	}
	
	private void constructVectors()
	{
		int counter = 0;
		int majorHolderSize = (int)numberOfElements1_r*(int)numberOfElements1_c + (int)numberOfElements2_r*(int)numberOfElements2_c;
		double[] majorHolder = new double[majorHolderSize];
		
		for(int i = 0; i < unmarshallers.length; i++)
		{
			double[] tempVector = unmarshallers[i].getVector();
			for(int j = 0; j < tempVector.length; j++)
				majorHolder[counter++] = tempVector[j];
		}
		
		counter = 0;
		int vectorSize = (int)numberOfElements1_r*(int)numberOfElements1_c;
		vector1 = new double[vectorSize];
		
		for(int i = 0; i < vectorSize; i++)
		{
			vector1[i] = majorHolder[i];
			counter++;
		}
		
		// construc the second one if exist
		if(numberOfElements2_r != 0 && numberOfElements2_c != 0)
		{
			numberOfVectors = 2;
			vectorSize = (int)numberOfElements2_r*(int)numberOfElements2_c;
			vector2 = new double[vectorSize];
			
			for(int i = 0; i < vectorSize; i++)
			{
				vector2[i] = majorHolder[counter++];
			}
		}
		
		parametersReady = true;
	}
	
	public boolean getParametersReady()
	{
		return parametersReady;
	}

	public double[] getVector1() {
		return vector1;
	}

	public double[] getVector2() {
		return vector2;
	}

	public void setUnmarshallers(UDPUnmarshaller[] unmarshallers) {
		this.unmarshallers = unmarshallers;
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
	
	public int getNumberOfVectors()
	{
		return numberOfVectors;
	}
	
	public boolean constructParameters()
	{
		boolean result = false;
		boolean flag = (numberOfElements1_r != -1) && (numberOfElements1_c != -1) && (numberOfElements2_r != -1) 
				&& (numberOfElements2_c != -1) && (unmarshallers != null);
		if(flag)
		{
			constructVectors();
			result = true;
		}
		return result;
	}

}
