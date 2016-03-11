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


public class Navigation {
    public final float HEURISTIC_COEFFICIENT = 0;
    public final float DIJKSTRA_COEFFICIENT = 1;
    public Beacon startBeacon, endBeacon;
    public LinkedList<Beacon> queue = new LinkedList<>();
    public ArrayList<Beacon> visitedBeaconList = new ArrayList<Beacon>();
    public ArrayList<Beacon> sameFloorNodes = new ArrayList<Beacon>();

    public Navigation(Beacon startBeacon, Beacon endBeacon){
        Log.e("Navigation constructor", "constructing...");
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
                Path bestPath = findDifferentFloorPath(startBeacon, endBeacon);
                bestPath.printPath();
                Log.e("================>", "end findDifferentFloorPath");
                return bestPath.getBeaconList();
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


    public Path findDifferentFloorPath(Beacon startBeacon,Beacon endBeacon){
        Path bestPath = null;
        int counter = 0;
        float minPathLength = 0;

        int startFloor = startBeacon.floor;
        int endFloor = endBeacon.floor;

        ArrayList<Beacon> startFloorBeaconList = findSameFloorNode(startFloor);
        ArrayList<Beacon> endFloorBeaconList = findSameFloorNode(endFloor);

        ArrayList<Beacon> startFloorElevators = findFloorElevatorNode(startFloor);
        ArrayList<Beacon> endFloorElevators = findFloorElevatorNode(endFloor);

        //iterate through all elevators on thses two floors
        for(Beacon startElevator : startFloorElevators)
        {
            for(Beacon endElevator : endFloorElevators)
            {
                if(startElevator.pipeNum == endElevator.pipeNum)
                {
                    Path startFloorPath = findSameFloorPath(startBeacon, startElevator);
                    Path endFloorPath = findSameFloorPath(endElevator, endBeacon);

                    float totalPathLength = startFloorPath.getLength() + endFloorPath.getLength();

                    if(counter == 0)
                    {
                        bestPath = combinePath(startFloorPath, endFloorPath);
                        minPathLength = bestPath.getLength();
                    }
                    else if(startFloorPath.getLength() + endFloorPath.getLength() < minPathLength)  //if this combined path is shorter
                    {
                        bestPath = combinePath(startFloorPath, endFloorPath);
                        minPathLength = bestPath.getLength();
                    }
                    counter++;
                }
            }
        }
        return bestPath;
    }

    public Path combinePath(Path firstPath, Path secondPath)
    {
        Path newPath = new Path();
        newPath.setLength(firstPath.getLength() + secondPath.getLength());
        newPath.addPathDetails(firstPath.getPathDetails() + secondPath.getPathDetails());
        newPath.getBeaconList().addAll(firstPath.getBeaconList());
        newPath.getBeaconList().addAll(secondPath.getBeaconList());
        newPath.setProcessTime(firstPath.getProcessTime() + secondPath.getProcessTime());

        return newPath;
    }

    public  ArrayList<Beacon> findFloorElevatorNode(int floor ){
        ArrayList<Beacon> elevators = new ArrayList<Beacon>();
        Iterator<String> keytie =   GlobalData.beaconlist.keySet().iterator();
        while(keytie.hasNext()){
            String key = keytie.next();
            Beacon beacon = GlobalData.beaconlist.get(key);
            if (GlobalData.BeaconType.values()[beacon.type]==GlobalData.BeaconType.ELEVATOR && beacon.floor == floor){
                elevators.add(beacon);
            }
        }
        return elevators;
    }



}
