package com.wenyun.k2i.utils;

public class DBFactory {
	
	public static DB getADB(String type)
	{
		DB tempdb=null;
		if (type.compareTo("IFX")==0)
		{
			tempdb = new influxdb();
		}
		
		return tempdb;
	}

}
