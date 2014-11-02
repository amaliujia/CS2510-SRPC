package com.aos.rpc.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;



public class Testing 
{
	public static void main (String[] args) throws Exception
	{
		double[] onedim = new double[10];
		double[][] twodim1 = new double[900][900];
		double[][] twodim2 = new double[900][900];
		
		//double[][] twodim1 = new double[2][3];
		//double[][] twodim2 = new double[3][2];


		
		for (int i = 0; i < onedim.length; i ++)
			onedim[i] = Math.random() * 100;
		
		for (int i = 0; i < twodim1.length; i ++)
			for (int j = 0; j < twodim1[0].length; j ++)
				twodim1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim2.length; i ++)
			for (int j = 0; j < twodim2[0].length; j ++)
				twodim2[i][j] = Math.random() * 100;

		
		ClientInterface cInterface = new ClientInterface("address");
		
		System.out.println("We are going to sort the array of ...");
		printOnedim(onedim);
		System.out.println("The sorted array is ... ");
      //  cInterface.sort(onedim);
		printOnedim(cInterface.sort(onedim));
		
		System.out.println("We are going to get the min from the array of ...");
		printOnedim(onedim);
		System.out.println("The min is ... \n" + cInterface.min(onedim));
    //    cInterface.min(onedim);

		System.out.println("We are going to get the max from the array of ...");
		printOnedim(onedim);
		//System.out.println("The max is ... \n" + cInterface.max(onedim));
        cInterface.max(onedim);

		System.out.println("We are going to multiply two matrices ...");
		//printTwodim(twodim1);
		//printTwodim(twodim2);
		System.out.println("The result is ... ") ;
		//printTwodim(cInterface.multiplication(twodim1, twodim2));
        cInterface.multiplication(twodim1, twodim2);
		
	}
	
	public static void printOnedim (double[] input)
	{
		for (int i = 0; i < input.length; i ++)
			System.out.print(String.format("%.2f", input[i]) + " ");
		System.out.println("");

	}
	
	public static void printTwodim (double[][] input)
	{
		for (int i = 0; i < input.length; i ++)
		{
			for (int j = 0; j < input[0].length; j ++)
				System.out.print(String.format("%.2f", input[i][j]) + " ");
			System.out.println("");

		}
	}

}