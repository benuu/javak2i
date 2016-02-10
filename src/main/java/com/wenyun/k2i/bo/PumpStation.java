package com.wenyun.k2i.bo;

import java.util.HashMap;
import java.util.List;

import com.wenyun.k2i.utils.DB;
import com.wenyun.k2i.utils.DBFactory;
import com.wenyun.k2i.utils.Kware;

public class PumpStation {
	private String stationName;
	private List<Device> devices;
	private List<HashMap> commonStatus;
	private List<String> tags;
	private DB psDB= DBFactory.getADB("IFX");
	private Kware pskware = new Kware();

}
