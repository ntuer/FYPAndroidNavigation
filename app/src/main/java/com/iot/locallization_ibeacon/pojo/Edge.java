package com.iot.locallization_ibeacon.pojo;

import android.webkit.WebSettings;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by zhujianjie on 2015/9/20.
 */
public class Edge {
    public String ID;
    public String ID_From;
    public String ID_To;
    public Polyline polyline;//a list of points, where lines are drawn between two consecutive points
    public Edge(String from ,String to ,Polyline polyline){
        this.ID = from+to;//concatenate two node ID
        this.ID_From = from;//source node ID
        this.ID_To = to;//destination node ID
        this.polyline = polyline;
    }

    public Edge(){}

    public String toString(){
        return  ID+"  "+ID_From+"  "+ID_To;
    }
}
