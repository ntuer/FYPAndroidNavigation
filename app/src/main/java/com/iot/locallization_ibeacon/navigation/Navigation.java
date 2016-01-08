package com.iot.locallization_ibeacon.navigation;

import android.util.Log;

import com.iot.locallization_ibeacon.pojo.Beacon;
import com.iot.locallization_ibeacon.pojo.GlobalData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhujianjie on 2015/9/21.
 */
public class Navigation {


    public Navigation(){
        findAllFloorElevatorNode();
        current = new ArrayList<>();
        best = new ArrayList<>();
    }

    public List<Beacon> startFindPath(Beacon startBeacon,Beacon endBeacon){

        Log.e("startFindPath" ,"strat ID = " +startBeacon.ID +" end ID = " + endBeacon.ID);

        if (startBeacon.floor == endBeacon.floor){
            initParameter();
            Log.e("================>", "start findSameFloorPath");
            findSameFloorPath(0, startBeacon, endBeacon);


        }else{
            Log.e("================>", "start findDifferentFloorPath");
            best = findDifferentFloorPath(startBeacon, endBeacon);
            Log.e("================>", "end findDifferentFloorPath");


        }
        Log.e("================>", "end findSameFloorPath length = "+best.size());
        String path="";
        for (int i = 0 ; i <best.size();i++){
            path +=" "+best.get(i).ID;
        }
        Log.e("best Path ",path);
        return best;
    }

    private List<Beacon> current ;
    private List<Beacon> best ;
    private int shortestLenth ;

    public void  findSameFloorPath(int cost,Beacon startBeacon,Beacon endBeacon){//postorder DFS


        cost++;
        current.add(startBeacon);
        if (startBeacon.ID.equals(endBeacon.ID))//if the destination is found
        {
            if (shortestLenth > cost)//get the shortest path from these available paths
            {
                shortestLenth = cost;

                best = null;
                best = new ArrayList<>();
                best.addAll(current);//assign the current path to the best path

                /*String path="";
                for (int i = 0 ; i <best.size();i++){
                    path +=" "+best.get(i).ID;
                }
                Log.e("best Path ",path);*/

            }else{
                current.remove(startBeacon);//if the current path is not the best path, remove the last node of the path
            }

        }

        startBeacon.isVisit =true;
       // boolean flag = false;

        Iterator<String> keytie = startBeacon.neighbors.keySet().iterator();

        while(keytie.hasNext()){//iterate its neighbors
            String key = keytie.next();
            Beacon beacon = GlobalData.beaconlist.get(key);
            if (!beacon.isVisit){//if its neighbor is not visited, find the path from its neighbor to the destination
                findSameFloorPath(cost, beacon, endBeacon);//recursively call the method to visit all nodes
            }

        }

        startBeacon.isVisit = false;
        current.remove(startBeacon);
    }

    public  List<Beacon> findSameFloorNode(int floor){
        List<Beacon> beaconlist = new ArrayList<Beacon>();
        Iterator<String> keytie =   GlobalData.beaconlist.keySet().iterator();
        while(keytie.hasNext()){
            String key = keytie.next();
            Beacon beacon = GlobalData.beaconlist.get(key);
            if (beacon.floor == floor)
            {
                beaconlist.add(beacon);
            }
        }

        return beaconlist;
    }


    public List<Beacon>  findDifferentFloorPath(Beacon startBeacon,Beacon endBeacon){

        List<Beacon> TempbestList=new ArrayList<>();
        int tempshortestlength = 100000;
        List<Beacon> bestList=new ArrayList<>();
        int pathLength = 0;

        for (int i =0; i < elevators.size() ;i++){
            Beacon beacon = elevators.get(i);
            if (beacon.floor == startBeacon.floor){//find the elevator beacon that is on the same floor as the startBeacon
                bestList.clear();
                pathLength=0;

                initParameter();

                findSameFloorPath(0,startBeacon,beacon);//find the best path from the startBeacon to the elevator beacon on the same floor
                pathLength += shortestLenth;
                bestList.addAll(best);

                for (int j =0; j < elevators.size() ;j++) {
                    Beacon beacon2 = elevators.get(j);
                    if(beacon2.pipeNum == beacon.pipeNum && beacon2.floor == endBeacon.floor){//if the elevator beacon is on the same elevator as the first elevator beacon and the floor is on the end floor

                        initParameter();
                        findSameFloorPath( 0, beacon2,endBeacon);
                        pathLength += shortestLenth;
                        bestList.addAll(best);

                        if (pathLength < tempshortestlength){
                            tempshortestlength = pathLength;
                            TempbestList.clear();
                            TempbestList.addAll(bestList);
                        }

                        break;
                    }
                }


            }

        }

        return  TempbestList;
    }

    public void initParameter(){
        current.clear();
        best.clear();
        shortestLenth = 10000000;
    }

    List<Beacon> elevators = new ArrayList<Beacon>();
    public  void findAllFloorElevatorNode( ){
        Iterator<String> keytie =   GlobalData.beaconlist.keySet().iterator();
        while(keytie.hasNext()){
            String key = keytie.next();
            Beacon beacon = GlobalData.beaconlist.get(key);
            if (GlobalData.BeaconType.values()[beacon.type]==GlobalData.BeaconType.ELEVATOR){
                elevators.add(beacon);
            }
        }

    }



}
