package com.aos.rpc.udpServer;

public class ProgramLibrary
{
	private int programNumber;
	private int programVersion;
	private int[] procedureNumbers;
	private int numberOfProcedures;

	public ProgramLibrary()
	{
		programNumber = 1;
		programVersion = 1;
		numberOfProcedures = 4;
		procedureNumbers = new int[numberOfProcedures];
		for(int i = 1; i <= numberOfProcedures; i++)
			procedureNumbers[i-1] = i;
	}

	public String sayHello()
	{
		return "Hello";
	}
	
	private void copyArray(double[] r, double[] v)
	{
		for(int i = 0; i < r.length; i++)
			r[i] = v[i];
	}

	//proc#1
	public double min(double[] vector) throws Exception
	{
		double min = -1;

		if(vector != null && vector.length != 0)
		{
			min = vector[0];
			for(int i = 0; i < vector.length; i++)
				if(min > vector[i])
					min = vector[i];
		}
		else
			throw new Exception("Execution error: the vector has no elements");

		return min;
	}

	//proc#2
	public double max(double[] vector) throws Exception
	{
		double max = -1;

		if(vector != null && vector.length != 0)
		{
			max = vector[0];
			for(int i = 0; i < vector.length; i++)
				if(max < vector[i])
					max = vector[i];
		}
		else
			throw new Exception("Execution error: the vector has no elements");

		return max;
	}

	//proc#3
	//we assume it is assending sort using insertion sort
	public double[] sort(double[] vector) throws Exception
	{
		double[] result = new double[vector.length];
		
		int n = vector.length;
		for (int j = 1; j < n; j++)
		{
			double key = vector[j];
			int i = j-1;
			while ( (i > -1) && ( vector [i] > key ) )
			{
				vector [i+1] = vector [i];
				i--;
			}
			vector[i+1] = key;
		}
		
		copyArray(result, vector);
		return result;
	}
	
	//proc#4
	//we assume it that we multiply it using the traditional 
	public double[][] multiply(double[][] matrix1, double[][] matrix2) throws Exception
	{
		System.out.println("Multiplying");
		int el1_r = matrix1.length;
		int el1_c = matrix1[0].length;
		int el2_r = matrix2.length;
		int el2_c = matrix2[0].length;
		
		double[][] result = new double [(int)el1_r][(int)el2_c];
		if(el1_c == el2_r)
		{
			//initialize to zeroes
			for(int i = 0; i < el1_r; i++)
				for(int j = 0; j < el2_c; j++)
					result[i][j] = 0.0;
			
			
			for(int i = 0; i < el1_r; i++)
				for(int j = 0; j < el2_c; j++)
					for(int k = 0; k < el1_c; k++)
						result[i][j] += (matrix1[i][k] * matrix2[k][j]);
		}
		else
			throw new Exception("Execution error: problem with matrix dimensions");
		System.out.println("Multiplying DONEEEEEEEEEEEEEEEEEEE");
		return result;
	}

	
	public int getProgramNumber() {
		return programNumber;
	}
//
//	public void setProgramNumber(int programNumber) {
//		this.programNumber = programNumber;
//	}
//
	public int getProgramVersion() {
		return programVersion;
	}
//
//	public void setProgramVersion(int programVersion) {
//		this.programVersion = programVersion;
//	}
//
	public int[] getProcedureNumbers() {
		return procedureNumbers;
	}
//
//	public void setProcedureNumbers(int[] procedureNumbers) {
//		this.procedureNumbers = procedureNumbers;
//	}
//
//	public int[] getProcedureVersions() {
//		return procedureVersions;
//	}
//
//	public void setProcedureVersions(int[] procedureVersions) {
//		this.procedureVersions = procedureVersions;
//	}
//
//	public int getNumberOfProcedures() {
//		return numberOfProcedures;
//	}
//
//	public void setNumberOfProcedures(int numberOfProcedures) {
//		this.numberOfProcedures = numberOfProcedures;
//	}
	
	public boolean isProgramNumberSupported(long programNumber)
	{
		boolean result = false;
		
		result = this.programNumber == programNumber;
		
		return result;
	}
	
	public boolean isProgramVersionSupported(long programVersion)
	{
		boolean result = false;
		
		result = this.programVersion == programVersion;
		
		return result;
	}
	
	public boolean isProcedureSupported(long procedureNumber)
	{
		boolean result = false;
		
		for(int i = 0; i < procedureNumbers.length; i++)
			if(procedureNumber == procedureNumbers[i])
				result = true;
		
		return result;
	}


}
