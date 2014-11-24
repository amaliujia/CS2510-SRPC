package com.aos.rpc.portMapper;

import java.io.IOException;

public class TestingPortMapper
{
	public static void main (String[] args) throws InterruptedException, IOException
	{
		portMapper pMapper = new portMapper("address");
		pMapper.run();
	}

}
