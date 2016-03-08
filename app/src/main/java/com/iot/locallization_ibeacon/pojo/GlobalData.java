package com.iot.locallization_ibeacon.pojo;

import android.os.Environment;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class GlobalData
{
	public static String log;
	public static  int curr_floor = -4;
	public static Handler loghandler ;
	public static float[] hw={188,23f};//used for defining ground overlay position, dimensions in meters, the S4 building is 188 meters long and 23 meters wide
	public static LatLng currentPosition = new LatLng(1.342518999,103.679474999);
	public static Date IPS_UpdateTime = new Date();
	public static enum BeaconType {OUTDOOR,INDOOR,STAIRS,ELEVATOR};
	public static enum EdgeAction {ADD_LINE,DELETE_LINE,NORMAL};
	public static LatLng ancer = new LatLng(1.342518999,103.679474999);//the gps position of the anchor point (local position of (0,0))
	private static File sd = Environment.getExternalStorageDirectory();
	public static String path = sd.getPath() + "sensorInfo.txt";
	public static Hashtable<String, Beacon> templist = new Hashtable();
	public static Hashtable<String, Beacon> beaconlist = new Hashtable();
	public static Hashtable<String, Beacon> scanbeaconlist = new Hashtable();
	public static List<Edge> edges = new ArrayList<>();
	public static List<Beacon> calculateBeacons =  new ArrayList<>();

}
