package com.iot.locallization_ibeacon.navigation;

import android.util.Log;

import java.util.ArrayList;
import com.iot.locallization_ibeacon.pojo.Beacon;

public class Path {
	private float length;
	private String pathDetails;
	private ArrayList<Beacon> beaconList;
	private long processTime;
	
	public Path()
	{
		this.length = 0;
		this.pathDetails = "";
		beaconList = new ArrayList<Beacon>();
	}

	public double getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public long getProcessTime()
	{
		return this.processTime;
	}

	public void setProcessTime(long processTime)
	{
		this.processTime = processTime;
	}

	public String getPathDetails() {
		return pathDetails;
	}

	public void addPathDetails(String pathDetails) {
		this.pathDetails = this.pathDetails + pathDetails;
	}
	
	public void printPath()
	{
		Log.e("Path: ", this.pathDetails);

	}
	
	public void addBeacon(Beacon beacon)
	{
		beaconList.add(beacon);
	}

	public ArrayList<Beacon> getBeaconList()
	{
		return this.beaconList;
	}
	
	
}
