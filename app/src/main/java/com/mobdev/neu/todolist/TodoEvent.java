package com.mobdev.neu.todolist;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class TodoEvent implements Serializable {
    private String name;
    private String note;
    private String time;
    private String latitude;
    private String longitude;



    public TodoEvent() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        this.time = sdf.format(cal.getTime());
    }
    public TodoEvent(String name, String note) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        this.name = name;
        this.note = note;
        this.time = sdf.format(cal.getTime());
    }

    public TodoEvent(String name, String note, String time) {
        this.name = name;
        this.note = note;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name;}
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public String getLatitude() { return latitude;}
    public void setLatitude(String latitude) {this.latitude = latitude; }
    public String getLongitude() { return longitude;}
    public void setLongitude(String longitude) {this.longitude = longitude; }
    private static int lastEventId = 0;

    public static ArrayList<TodoEvent> createEventsList(int numEvents) {
        ArrayList<TodoEvent> events = new ArrayList<>();
        for (int i = 0; i < numEvents; i++) {
            events.add(new TodoEvent("Event"+i, null));
        }
        return events;
    }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> todo =  new HashMap<String,String>();
        todo.put("name", name);
        todo.put("note", note);
        todo.put("time", time);
        todo.put("latitude", latitude);
        todo.put("longitude", longitude);

        return todo;
    }
}
