package org.mitre.abi.timePlayer.remote.client;

import java.io.Serializable;

public class SimpleTimeDot implements Serializable {

    private static final long serialVersionUID = -1223547282274660324L;
    private double lat;
    private double lon;
    private long startTime;
    private long endTime;
    
    public SimpleTimeDot(double lat, double lon, long startTime, long endTime) {
        super();
        this.lat = lat;
        this.lon = lon;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
}
