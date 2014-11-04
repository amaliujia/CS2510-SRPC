package com.aos.rpc.client;



public class Testing 
{
	public static void main (String[] args) throws Exception
	{
		double[] onedim = new double[10];
		double[][] twodim1 = new double[10][1200];
		double[][] twodim2 = new double[1200][10];
		
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

		
		ClientInterface cInterface = new ClientInterface();
		
		System.out.println("\n\n------------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------------");

		System.out.println("We are going to sort the array of ...");
		printOnedim(onedim);
		System.out.println("The sorted array is ... ");
		double [] ans1 = cInterface.sort(onedim);
		if (ans1 == null)
		{
			System.out.println("Error happens in sort");
		}
		else {
			printOnedim(ans1);
		}
		
		System.out.println("\n\n------------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------------");

		System.out.println("We are going to get the min from the array of ...");
		printOnedim(onedim);
		//System.out.println("The min is ... \n" + cInterface.min(onedim));
        double ans2 = cInterface.min(onedim);
		if (ans2 == -1)
		{
			System.out.println("Error happens in min");
		}
		else 
		{
			System.out.println("The min is ... \n" + String.format("%.2f", ans2));
		}
        
		
		System.out.println("\n\n------------------------------------------------------------------------------");

		System.out.println("------------------------------------------------------------------------------");

		
		System.out.println("We are going to get the max from the array of ...");
		printOnedim(onedim);
        double ans3 = cInterface.max(onedim);
		if (ans3 == -1)
		{
			System.out.println("Error happens in max");
		}
		else 
		{
			System.out.println("The max is ... \n" + String.format("%.2f", ans3));
		}
		
		
		System.out.println("\n\n------------------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------------------");

		

		System.out.println("We are going to multiply two matrices ...");
		//printTwodim(twodim1);
		//printTwodim(twodim2);
		System.out.println("The result is ... ") ;
        double[][] ans4 = cInterface.multiplication(twodim1, twodim2);
        
        if (ans4 == null)
		{
			System.out.println("Error happens in mult");
		}
		else 
		{
		    //printTwodim(ans4);
			System.out.println("Mult done!");
		}

		
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
