package com.aos.rpc.helperClasses;

public class MatrixResolver
{
	private double[][] matrix;
	private double[] vectorFromMatrix;
	private double[] matrixVector;
	private double[][] matrixFromVector;
	
	
	public MatrixResolver()
	{
		vectorFromMatrix = matrixVector = null;
		matrix = matrixFromVector = null;
	}
	
	private void transformToVector()
	{
		int k = 0;
		
		if(matrix != null)
		{
			vectorFromMatrix = new double[matrix.length * matrix[0].length];
			for(int i = 0; i < matrix.length; i++)
				for(int j = 0; j < matrix[0].length; j++)
					vectorFromMatrix[k++] = matrix[i][j];
		}
	}
	
	private void transformToMatrix(int r, int c)
	{
		matrixFromVector = new double[r][c];
		if(matrixVector != null)
		{
	        for (int i = 0; i < r; i ++)
	        	for (int j = 0; j < c; j ++)
	        		matrixFromVector[i][j] = matrixVector[i*c + j];
		}
	}

	public void setVectorMatrix(double[] matrix, int row, int col)
	{
		matrixVector = matrix;
		transformToMatrix(row,col);
	}
	
	public double[][] getMatrixFromVector()
	{
		return matrixFromVector;
	}
	
	public void setMatrix(double[][] matrix)
	{
		this.matrix = matrix;
	}
	
	public double[] getVectorFromMatrix()
	{
		transformToVector();
		return vectorFromMatrix;
	}
}
