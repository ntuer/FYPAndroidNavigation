package com.iot.locallization_ibeacon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iot.locallization_ibeacon.R;
import com.iot.locallization_ibeacon.algorithm.WPL_Limit_BlutoothLocationAlgorithm;
import com.iot.locallization_ibeacon.navigation.Navigation;
import com.iot.locallization_ibeacon.pojo.Beacon;
import com.iot.locallization_ibeacon.pojo.Destionation;
import com.iot.locallization_ibeacon.pojo.Edge;
import com.iot.locallization_ibeacon.pojo.GlobalData;
import com.iot.locallization_ibeacon.tools.Tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class DemoActivity extends Activity {
    private GoogleMap map;
    private Circle currmark=null;
    public static  String logstring ="";
    private Handler updateHandler = new Handler();
    private GroundOverlay buildingMapImage =null;
    private Location currentLocation =null;
    private LocationManager locationManager;

    private  boolean isNavigation =false;
    private WPL_Limit_BlutoothLocationAlgorithm location =new WPL_Limit_BlutoothLocationAlgorithm();
    Destionation destinaton ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        GlobalData.loghandler = updatelog ;//如果楼层改变loghandler用于改变地图

        //-------------------------------------------------------------------
        //下面两步顺序不能改变
        initMap();  //初始化地图
        readConf(); //读取配置文件
        //--------------------------------------------------------------------

        changeBuildingMap();    //显示当前楼层地图
        initUI();
        initSensor();

    }


    private void initUI(){
        Spinner spiner = (Spinner)findViewById(R.id.spinner);

//        Destionation d1 = new Destionation("Chair of EEE", GlobalData.beaconlist.get("1128"));
//        Destionation d2 = new Destionation("Men Toilet", GlobalData.beaconlist.get("1121"));
//        Destionation d3 = new Destionation("Office of Prof Mo Yilin", GlobalData.beaconlist.get("114"));
//        Destionation d4 = new Destionation("IOT Lab", GlobalData.beaconlist.get("1412"));
//        Destionation d5= new Destionation("Robotic Lab", GlobalData.beaconlist.get("1440"));
//        Destionation d6= new Destionation("Office of Prof Costas Spanos", GlobalData.beaconlist.get("1114"));

        Destionation d1 = new Destionation("Chair of EEE", GlobalData.beaconlist.get("147"));
        Destionation d2 = new Destionation("Men Toilet", GlobalData.beaconlist.get("149"));
        Destionation d3 = new Destionation("Office of Prof Mo Yilin", GlobalData.beaconlist.get("118"));
        Destionation d4 = new Destionation("IOT Lab", GlobalData.beaconlist.get("119"));
        Destionation d5= new Destionation("Robotic Lab", GlobalData.beaconlist.get("211"));
        Destionation d6= new Destionation("Office of Prof Costas Spanos", GlobalData.beaconlist.get("217"));

        final List<Destionation> destionList = new ArrayList<Destionation>();
        destionList.add(d1);
        destionList.add(d2);
        destionList.add(d3);
        destionList.add(d4);
        destionList.add(d5);
        destionList.add(d6);

        destinaton = destionList.get(0);

        List<String> dataList = new ArrayList<String>();
        for (int i = 0 ; i < destionList.size() ; i++){
            dataList.add(destionList.get(i).name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,dataList);
        spiner.setAdapter(adapter);
        spiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("onItemSelected", destionList.get(i).name);
                destinaton = destionList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final Button start = (Button)findViewById(R.id.BT_SatrtNavigation);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNavigation) {
                    start.setText("Stop");
                } else {
                    start.setText("Start");
                }

                isNavigation = !isNavigation;
                Log.e("navigation", "start navigation");
                //Navigation nv = new Navigation(GlobalData.beaconlist.get("141"), GlobalData.beaconlist.get("148"));
                //nv.startFindPath();

            }
        });


    }
    /**
     * 改变楼层地图
     */
    private void changeBuildingMap() {
        BitmapDescriptor img =null;
        Log.e("changeBuildingMap", " floor = " + GlobalData.curr_floor);
        switch(GlobalData.curr_floor)
        {
            case 1:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k11);
                break;
            case 2:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k22);
                break;
            case 3:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k33);
                break;
            case 4:
                img=BitmapDescriptorFactory.fromResource(R.drawable.k44);
                break;
            default:
                return;
        }
        buildingMapImage.remove();
        buildingMapImage = map.addGroundOverlay(new GroundOverlayOptions()
                .image(img).anchor(0, 0).bearing(-45f)
                .position(GlobalData.ancer, GlobalData.hw[0], GlobalData.hw[1]));
    }

    /**
     * the updateMap() runs every second
     */
    private void updateMap() {
        updateLocation(GlobalData.currentPosition);
        Date date = new Date();
        if (Math.abs(date.getTime() - GlobalData.IPS_UpdateTime.getTime()) >  6000)//if 6 seconds is passed
        {
            openGPS();//initializa GPS
            return;
        }

        if(locationManager!=null ){//remove GPSlistener
            locationManager.removeUpdates(GPSlistener);
            locationManager = null;
        }

        location.setHandler(updatelog);
        location.DoLocalization();
        //updateLocation(GlobalData.currentPosition); //moved to the first line --peixiang at 08 Mar

        cleanScanbeaconlist();

    }

    private  void cleanScanbeaconlist(){
        Date now = new Date();


            Iterator<String> iter =GlobalData.scanbeaconlist.keySet().iterator();  //用一个时间段内扫描到的beacon计算
          //  Log.e("cleanScanbeaconlist","cleanScanbeaconlist start size = "+GlobalData.scanbeaconlist.size());
            List<Beacon> removeList = new ArrayList<>();
            while (iter.hasNext()) {
                String key =  iter.next();
                Beacon sensor =GlobalData.scanbeaconlist.get(key);
                //Log.e("out   ============", "time = " + (now.getTime() - sensor.updateTime) );
                if (now.getTime() - sensor.updateTime > 2000)
                {
                   // Log.e("int   ============", "time = " + (now.getTime() - sensor.updateTime) );
                    removeList.add(sensor);
                }
            }


          for (int index = 0 ; index < removeList.size();index++){
                Beacon beacon = removeList.get(index);
                GlobalData.scanbeaconlist.remove(beacon.ID);
            }

            removeList =null;
        //Log.e("cleanScanbeaconlist","cleanScanbeaconlist end size = "+GlobalData.scanbeaconlist.size());



    }


    public  Beacon getBestBeacon(){
       if (GlobalData.calculateBeacons.size() > 0 ){
           return GlobalData.calculateBeacons.get(0);
       }

        return  null;
    }

    List<Polyline> pathlines = new ArrayList<>();
    Marker navigtionEndMark = null;
    /**
     * 更新位置
     * @param location
     */
    public void updateLocation(LatLng location){
        if (currmark!= null)
        {
            currmark.remove();//remove the current marker
        }
        currmark=map.addCircle(new CircleOptions().center(location)
                .fillColor(Color.argb(100, 147, 112, 219)).radius(1).strokeWidth(0));//add the marker on the new location


        if (isNavigation){//if the navigation is on
            Log.e("navigation", "navigation is running");
            //Beacon srcBeacon = getBestBeacon();//beacon with max rssi, disable for debug purpose
            Beacon srcBeacon = GlobalData.beaconlist.get("142");
            //Log.e("srcBeacon", srcBeacon.ID);
            if(srcBeacon==null)
            {
                Log.e("navigation", "srcBeacon is null");
                return;
            }


            for (int i = 0 ; i < pathlines.size() ; i++){
                pathlines.get(i).remove();
            }

            if (navigtionEndMark!=null){
                navigtionEndMark.remove();
            }

            pathlines.clear();

            Navigation nv = new Navigation(srcBeacon, destinaton.postion);
            //Navigation nv = new Navigation(srcBeacon, GlobalData.beaconlist.get("117"));
            Log.e("navigation", "constructor should be running");
            List<Beacon> path = nv.startFindPath();//the navigation algorithm
            if (path.size() <= 1)
            {
                isNavigation = false;
                Button start = (Button)findViewById(R.id.BT_SatrtNavigation);
                start.setText("Start");
                Toast.makeText(this,"Congratulations!\n Navigation completed!\n Welcome to "+destinaton.name,Toast.LENGTH_LONG).show();
                return;
            }

            if (path.size()>=2
                    &&GlobalData.BeaconType.values()[path.get(0).type] == GlobalData.BeaconType.ELEVATOR
                    &&path.get(0).floor!=path.get(1).floor )
            {
                TextView TV_ShowTip = (TextView)findViewById(R.id.TV_ShowTip);
                TV_ShowTip.setText("Please take the lift to B"+path.get(1).floor);
            }
            else{
                TextView TV_ShowTip = (TextView)findViewById(R.id.TV_ShowTip);
                TV_ShowTip.setText("");
            }
            if (path.size() >=2 && GlobalData.calculateBeacons.size() >= 2
                    &&(GlobalData.calculateBeacons.get(1).ID.equals(path.get(1).ID))){
                path.remove(0);

            }else if (path.size() >=3 && GlobalData.calculateBeacons.size() >= 2
                    &&(GlobalData.calculateBeacons.get(1).ID.equals(path.get(2).ID))){
                path.remove(0);

            }

            pathlines.add(map.addPolyline(new PolylineOptions()
                    .add(GlobalData.currentPosition)
                    .add(path.get(0).position).color(Color.RED)));

            for (int i = 0 ; i < path.size()-1 ; i++){

                if (path.get(i).floor == GlobalData.curr_floor){
                    Polyline line =  map.addPolyline(new PolylineOptions()
                            .add(path.get(i).position)
                            .add(path.get(i+1).position).color(Color.RED));
                    pathlines.add(line);
                }else{
                    Polyline line =  map.addPolyline(new PolylineOptions()
                            .add(path.get(i).position)
                            .add(path.get(i+1).position).color(Color.GREEN).geodesic(true));
                    pathlines.add(line);
                }



            }
            navigtionEndMark = map.addMarker(new MarkerOptions().position(path.get(path.size()-1).position));
            navigtionEndMark.setTitle(destinaton.name);
            navigtionEndMark.setSnippet("Your destination is here");
            navigtionEndMark.showInfoWindow();

        }
        else
        {
            if (pathlines.size()!=0){
                for (int i = 0 ; i < pathlines.size() ; i++){
                    pathlines.get(i).remove();
                }
                pathlines.clear();
            }

            if (navigtionEndMark!=null){
                navigtionEndMark.remove();
            }
        }
    }



    /**
     * 初始化GPS设置
     */
    private void openGPS() {

        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 0, GPSlistener);
            return;

        }

        Toast.makeText(this, "GPS dont open", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        startActivityForResult(intent, 0);


    }

    /**
     * 初始化地图
     */
    private void  initMap() {
        //GoogleMap object
        map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setIndoorEnabled(true);
        //map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        buildingMapImage = map.addGroundOverlay(new GroundOverlayOptions()//the anchor and bearing for loading the map
                .image(BitmapDescriptorFactory.fromResource(R.drawable.k44)).anchor(0, 0).bearing(-45f)//anchor(0,0) is top left， bearing is degrees clockwise from north around anchor
                .position(GlobalData.ancer, GlobalData.hw[0],GlobalData.hw[1]));

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(GlobalData.ancer, 23);//update the map camera to focus on the building map
        map.moveCamera(update);

    }


    private void readConf(){

        Tools.ReadConfigFile(this);//set the marker position for every beacon in the database
        List<Edge> edges = Tools.getAllEdge(this);//return a list of edges based on the beacons in the database
        for(int index = 0 ; index < edges.size() ; index++){
            Edge edge = edges.get(index);


            Beacon from = GlobalData.beaconlist.get(edge.ID_From);
            Beacon to = GlobalData.beaconlist.get(edge.ID_To);
            from.neighbors.put(to.ID,to);
            from.edges.put(edge.ID,edge);//assign the neighbors for each beacon based on the edges info

            /*edge.polyline = map.addPolyline(new PolylineOptions()
                    .add(from.position)
                    .add(to.position).color(Color.RED));*/
        }
        updateHandler.postDelayed(updateMap, 1000);//update map every second

    }


    LocationListener GPSlistener =  new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if(currentLocation!=null){
                if(Tools.isBetterLocation(location, currentLocation)){
                    Log.v("GPSTEST", "It's a better location");
                    currentLocation=location;
                    updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                }
                else{
                    Log.v("GPSTEST", "Not very good!");
                }
            }
            else if(location.getAccuracy() < 5)
            {
                Log.v("GPSTEST", "It's first location");
                currentLocation=location;
                updateLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private SensorManager mSensorManager;
    private Sensor mOrientationSensor;

    private int length =10;
    private int pos=0;
    public float currDegree;
    public float[] AvgDegree = new float[length];

    public float LastDegree =0;
    public void setDegree(float degree){//return the average of 10 degrees
        AvgDegree[pos] = degree;
        pos = (pos + 1)%length;
        float sum = 0;
        for (int i = 0 ; i < AvgDegree.length;i++){
            sum += AvgDegree[i];
        }
        currDegree=sum/length;
    }

    public void initSensor(){

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mOrientationSensor != null) {
            mSensorManager.registerListener(new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float direction = sensorEvent.values[0] ;
                setDegree(direction);
                //Log.e("onSensorChanged", " degree" + direction);// 赋值给全局变量，让指南针旋转
                if (Math.abs(LastDegree-currDegree) < 10){

                    return;
                }



                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(GlobalData.currentPosition).zoom(map.getCameraPosition().zoom)
                        .bearing(currDegree).build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                LastDegree = currDegree;

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }},mOrientationSensor, 2);
        } else {
            /*Toast.makeText(this, R.string.cannot_get_sensor, Toast.LENGTH_SHORT)
                    .show();*/
        }
    }

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }
    private Runnable updateMap = new Runnable()
    {
        @Override
        public void run()
        {
            updateMap();//run updateMap() in another thread every second
            updateHandler.postDelayed(updateMap, 1000);
        }
    };

    Handler updatelog = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.arg1 == 2)
            {
                changeBuildingMap();
            }
            super.handleMessage(msg);

        }
    };
}
