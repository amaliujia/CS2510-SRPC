package com.aos.rpc.client;

import com.aos.rpc.udpServer.ProgramLibrary;


public class TestingSORT 
{
	public static void main (String[] args) throws Exception
	{
		double[] onedim = new double[10];
		double[][] twodim1_1 = new double[100][100];
		double[][] twodim1_2 = new double[100][100];
		double[][] twodim2_1 = new double[300][300];
		double[][] twodim2_2 = new double[300][300];
		double[][] twodim3_1 = new double[500][500];
		double[][] twodim3_2 = new double[500][500];
		double[][] twodim4_1 = new double[700][700];
		double[][] twodim4_2 = new double[700][700];
		double[][] twodim5_1 = new double[900][900];
		double[][] twodim5_2 = new double[900][900];
		double[][] twodim6_1 = new double[1000][1000];
		double[][] twodim6_2 = new double[1000][1000];
		
		double[] onedim1_1 = new double[1000];
		double[] onedim1_2 = new double[1000];
		double[] onedim2_1 = new double[3000];
		double[] onedim2_2 = new double[3000];
		double[] onedim3_1 = new double[5000];
		double[] onedim3_2 = new double[5000];
		double[] onedim4_1 = new double[7000];
		double[] onedim4_2 = new double[7000];
		double[] onedim5_1 = new double[9000];
		double[] onedim5_2 = new double[9000];
		double[] onedim6_1 = new double[10000];
		double[] onedim6_2 = new double[10000];

		
		
		
		//double[][] twodim1 = new double[2][3];
		//double[][] twodim2 = new double[3][2];

		ClientInterface cInterface = new ClientInterface();
		ProgramLibrary localPL = new ProgramLibrary();

//		
//		for (int i = 0; i < onedim.length; i ++)
//			onedim[i] = Math.random() * 100;
		
		for (int i = 0; i < twodim1_1.length; i ++)
			for (int j = 0; j < twodim1_1[0].length; j ++)
				twodim1_1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim1_2.length; i ++)
			for (int j = 0; j < twodim1_2[0].length; j ++)
				twodim1_2[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim2_1.length; i ++)
			for (int j = 0; j < twodim2_1[0].length; j ++)
				twodim2_1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim2_2.length; i ++)
			for (int j = 0; j < twodim2_2[0].length; j ++)
				twodim2_2[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim3_1.length; i ++)
			for (int j = 0; j < twodim3_1[0].length; j ++)
				twodim3_1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim3_2.length; i ++)
			for (int j = 0; j < twodim3_2[0].length; j ++)
				twodim3_2[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim4_1.length; i ++)
			for (int j = 0; j < twodim4_1[0].length; j ++)
				twodim4_1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim4_2.length; i ++)
			for (int j = 0; j < twodim4_2[0].length; j ++)
				twodim4_2[i][j] = Math.random() * 100;

		for (int i = 0; i < twodim5_1.length; i ++)
			for (int j = 0; j < twodim5_1[0].length; j ++)
				twodim5_1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim5_2.length; i ++)
			for (int j = 0; j < twodim5_2[0].length; j ++)
				twodim5_2[i][j] = Math.random() * 100;

		for (int i = 0; i < twodim6_1.length; i ++)
			for (int j = 0; j < twodim6_1[0].length; j ++)
				twodim6_1[i][j] = Math.random() * 100;
		
		for (int i = 0; i < twodim6_2.length; i ++)
			for (int j = 0; j < twodim6_2[0].length; j ++)
				twodim6_2[i][j] = Math.random() * 100;

		
		
//--------------------------------------------------------------------------------------------------
		
		for (int i = 0; i < onedim1_1.length; i ++)
		{
			onedim1_1[i] = Math.random() * 100;
			onedim1_2[i] = Math.random() * 100;
		}
		
		for (int i = 0; i < onedim2_1.length; i ++)
		{
			onedim2_1[i] = Math.random() * 100;
			onedim2_2[i] = Math.random() * 100;
		}
		
		for (int i = 0; i < onedim3_1.length; i ++)
		{
			onedim3_1[i] = Math.random() * 100;
			onedim3_2[i] = Math.random() * 100;
		}

		for (int i = 0; i < onedim4_1.length; i ++)
		{
			onedim4_1[i] = Math.random() * 100;
			onedim4_2[i] = Math.random() * 100;
		}

		for (int i = 0; i < onedim5_1.length; i ++)
		{
			onedim5_1[i] = Math.random() * 100;
			onedim5_2[i] = Math.random() * 100;
		}
		
		for (int i = 0; i < onedim6_1.length; i ++)
		{
			onedim6_1[i] = Math.random() * 100;
			onedim6_2[i] = Math.random() * 100;
		}


		
		 long startTime = System.currentTimeMillis();
		
		// localPL.multiply(twodim1_1, twodim1_2);
		 localPL.sort(onedim1_1);
		
		 long endTime = System.currentTimeMillis();

		 System.out.println("Total local execution time of SORT1: " + (endTime - startTime) );

		 
		 
		 startTime = System.currentTimeMillis();
		
		 cInterface.sort(onedim1_2);
		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total RPC execution time of SORT1: " + (endTime - startTime) );
//-------------------------------------------------------------------------------------------------------
		
			
		 startTime = System.currentTimeMillis();
		
		 //localPL.multiply(twodim2_1, twodim2_2);
		 localPL.sort(onedim2_1);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total local execution time of SORT2: " + (endTime - startTime) );

		 
		 
		 startTime = System.currentTimeMillis();
		
		//  cInterface.multiplication(twodim2_1, twodim2_2);
		 cInterface.sort(onedim2_2);
		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total RPC execution time of SORT2: " + (endTime - startTime) );
//-------------------------------------------------------------------------------------------------------

			
		 startTime = System.currentTimeMillis();
		
		// localPL.multiply(twodim3_1, twodim3_2);
		 localPL.sort(onedim3_1);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total local execution time of SORT3: " + (endTime - startTime) );

		 
		 
		 startTime = System.currentTimeMillis();
		
		// cInterface.multiplication(twodim3_1, twodim3_2);
		 cInterface.sort(onedim3_2);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total RPC execution time of SORT3: " + (endTime - startTime) );
//-------------------------------------------------------------------------------------------------------
		 
		 
			
		 startTime = System.currentTimeMillis();
		
		 //localPL.multiply(twodim4_1, twodim4_2);
		 localPL.sort(onedim4_1);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total local execution time of SORT4: " + (endTime - startTime) );

		 
		 
		 startTime = System.currentTimeMillis();
		
		// cInterface.multiplication(twodim4_1, twodim4_2);
		 cInterface.sort(onedim4_2);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total RPC execution time of SORT4: " + (endTime - startTime) );
//-------------------------------------------------------------------------------------------------------
		 
			
		 startTime = System.currentTimeMillis();
		
		// localPL.multiply(twodim5_1, twodim5_2);
		 localPL.sort(onedim5_1);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total local execution time of SORT5: " + (endTime - startTime) );

		 
		 
		 startTime = System.currentTimeMillis();
		
		// cInterface.multiplication(twodim5_1, twodim5_2);
		 cInterface.sort(onedim5_2);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total RPC execution time of SORT5: " + (endTime - startTime) );
//-------------------------------------------------------------------------------------------------------
		 
		 
		 
			
		 startTime = System.currentTimeMillis();
		
		// localPL.multiply(twodim6_1, twodim6_2);
		 localPL.sort(onedim6_1);

		
		 endTime = System.currentTimeMillis();

		 System.out.println("Total local execution time of SORT6: " + (endTime - startTime) );

		 
		 
		 startTime = System.currentTimeMillis();
		
	//	 cInterface.multiplication(twodim6_1, twodim6_2);
		 cInterface.sort(onedim6_2);
		 
		 endTime = System.currentTimeMillis();

		 System.out.println("Total RPC execution time of SORT6: " + (endTime - startTime) );
//-------------------------------------------------------------------------------------------------------
	}
		 
		 
		


}