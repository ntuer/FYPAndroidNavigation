package com.iot.locallization_ibeacon.navigation;

import android.support.annotation.NonNull;
import android.util.Log;

import com.iot.locallization_ibeacon.pojo.Beacon;
import com.iot.locallization_ibeacon.pojo.GlobalData;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by zhujianjie on 2015/9/21.
 */
public class Navigation {
    public final float HEURISTIC_COEFFICIENT = 0;
    public final float DIJKSTRA_COEFFICIENT = 1;
    public Beacon startBeacon, endBeacon;
    public LinkedList<Beacon> queue = new LinkedList<>();
    public ArrayList<Beacon> visitedBeaconList = new ArrayList<Beacon>();
    public ArrayList<Beacon> sameFloorNodes = new ArrayList<Beacon>();

    public Navigation(Beacon startBeacon, Beacon endBeacon){
        Log.e("Navigation constructor", "constructing...");
        findAllFloorElevatorNode();
        current = new ArrayList<>();
        best = new ArrayList<>();
        this.startBeacon = startBeacon;
        this.endBeacon = endBeacon;

    }

    public List<Beacon> startFindPath(){

        Log.e("startFindPath" ,"strat ID = " +startBeacon.ID +" end ID = " + endBeacon.ID);
        Log.e("startLocation", startBeacon.building + ", " + startBeacon.floor + ", " + startBeacon.ID);
        Log.e("endLocation", endBeacon.building + ", " + endBeacon.floor + ", " + endBeacon.ID);
        if(startBeacon.building.equals(endBeacon.building))
        {
            if (startBeacon.floor == endBeacon.floor){  //same building, same floor
                Log.e("================>", "start findSameFloorPath");
                Path bestPath = findSameFloorPath(startBeacon, endBeacon);
                bestPath.printPath();
                return bestPath.getBeaconList();

            }else{      //same building, different floors
                Log.e("================>", "start findDifferentFloorPath");
                best = findDifferentFloorPath(startBeacon, endBeacon);
                Log.e("================>", "end findDifferentFloorPath");
                return null;
            }
        }
        else //different buildings
        {
            Log.e("================>", "start findDifferentBuildingPath");
            //findDifferentBuildingPath();
            return null;
        }

    }

    private List<Beacon> current ;
    private List<Beacon> best ;
    private int shortestLenth ;

    public Path  findSameFloorPath(Beacon startBeacon,Beacon endBeacon){//A star
        Integer floor = startBeacon.floor;//the current floor
        sameFloorNodes = findSameFloorNode(floor);
        initializeNodes(sameFloorNodes);
        while(!endBeacon.isVisited)
        {
            Beacon currentBeacon = queue.remove();
            visit(currentBeacon);
        }
        return getBestPath(endBeacon);
    }

    public Path getBestPath(Beacon endBeacon)
    {
        Path bestPath = new Path();
        bestPath.setLength(endBeacon.distance);
        ArrayList<Beacon> reversePathBeaconList = new ArrayList<Beacon>();
        String pathDetails = "";
        reversePathBeaconList.add(endBeacon);
        while(!endBeacon.previousBeacon.equals(startBeacon))
        {
            endBeacon = endBeacon.previousBeacon;
            reversePathBeaconList.add(endBeacon);
        }
        reversePathBeaconList.add(startBeacon);

        //reverse the beacon order
        for(int i = reversePathBeaconList.size() - 1; i >= 0; i--)
        {
            Beacon beacon = reversePathBeaconList.get(i);
            bestPath.addBeacon(beacon);
            if(i > 0)
            {
                pathDetails += beacon.ID + "->";
            }
            else
            {
                pathDetails += beacon.ID;
            }
        }
        bestPath.addPathDetails(pathDetails);
        return bestPath;
    }

    public void visit(Beacon beacon)
    {
        //visit the beacon
        beacon.isVisited = true;
        visitedBeaconList.add(beacon);

        ArrayList<Beacon> unvisitedNeighborList = new ArrayList<Beacon>();
        Iterator<String> keytie = beacon.neighbors.keySet().iterator();
        while(keytie.hasNext())
        {
            String key = keytie.next();
            Beacon neighbor = GlobalData.beaconlist.get(key);   //get its neighbor
            if(!neighbor.isVisited) //is the neighbor is not visited yet
            {
                float tempDijkstraDistance = beacon.dijkstraDistance + getDistance(beacon, neighbor);
                float tempDistance = tempDijkstraDistance + neighbor.heuristicDistance;//the temp path distance via node
                if (tempDistance < neighbor.distance)//if this is a better path
                {
                    neighbor.dijkstraDistance = tempDijkstraDistance;//update the dijkstraDistance and total distance
                    neighbor.updateDistance();
                    neighbor.previousBeacon = beacon;
                }
                unvisitedNeighborList.add(neighbor);
                Log.e("unvisited neighbor", neighbor.ID);
            }
        }

        Collections.sort(unvisitedNeighborList, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon beacon, Beacon t1) {
                if (beacon.distance < t1.distance) {
                    return -1;
                } else if (beacon.distance == t1.distance) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        for(Beacon neighbor : unvisitedNeighborList)
        {
            queue.add(neighbor);
            Log.e("enqueued neighbor", neighbor.ID);
        }
    }

    public float getDistance(Beacon startBeacon, Beacon endBeacon)
    {
        float xDist = Math.abs(startBeacon.x - endBeacon.x);
        float yDist = Math.abs(startBeacon.y - endBeacon.y);
        float distance = (float)Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));
        return distance;
    }

    public void initializeNodes(List<Beacon> sameFloorNodes)
    {
        for(Beacon beacon : sameFloorNodes)
        {
            beacon.isVisited = false;
            beacon.distance = Float.MAX_VALUE;
            beacon.heuristicDistance = HEURISTIC_COEFFICIENT * getDistance(beacon, endBeacon);
        }
        startBeacon.dijkstraDistance = 0;
        startBeacon.updateDistance();
        queue.add(startBeacon);
    }

    public void resetBeacons()
    {
        for(Beacon beacon : sameFloorNodes)
        {
            beacon.reset();
        }
    }

    public  ArrayList<Beacon> findSameFloorNode(int floor){
        ArrayList<Beacon> beaconlist = new ArrayList<Beacon>();
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

                findSameFloorPath(startBeacon,beacon);//find the best path from the startBeacon to the elevator beacon on the same floor
                pathLength += shortestLenth;
                bestList.addAll(best);

                for (int j =0; j < elevators.size() ;j++) {
                    Beacon beacon2 = elevators.get(j);
                    if(beacon2.pipeNum == beacon.pipeNum && beacon2.floor == endBeacon.floor){//if the elevator beacon is on the same elevator as the first elevator beacon and the floor is on the end floor

                        initParameter();
                        findSameFloorPath(beacon2,endBeacon);
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
