package wisc.drivesense.utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wisc.drivesense.database.DatabaseHelper;

/**
 * Created by lkang on 3/29/16.
 */
public class Trip implements Serializable {

    private long startTime_ = 0;
    private long endTime_ = 0;
    private double distance_ = 0; // in miles
    private int score = 100;
    private List<Trace> gps_;

    //private DatabaseHelper dbHelper_ = null;

    public Trip () {
        gps_ = new ArrayList<Trace>();
    }

    public void setStartTime(long time) {
        this.startTime_ = time;
    }
    public void setEndTime(long time) {
        this.endTime_ = time;
    }
    public void setDistance(double dist) {
        this.distance_ = dist;
    }


    public long getStartTime() {
        return this.startTime_;
    }
    public long getEndTime() {
        return this.endTime_;
    }
    public double getDistance() {
        return this.distance_ * Constants.kMeterToMile;
    }
    public void addGPS(Trace trace) {
        gps_.add(trace);
        int sz = gps_.size();
        if(sz >= 2) {
            distance_ += distance(gps_.get(sz - 2), gps_.get(sz - 1));
        }
    }


    public void setGPSPoints(List<Trace> gps) {
        this.gps_ = gps;
    }
    public List<Trace> getGPSPoints() {
        return gps_;
    }



    private static double distance(Trace gps0, Trace gps1) {

        double lat1 = Math.toRadians(gps0.values[0]);
        double lng1 = Math.toRadians(gps0.values[1]);
        double lat2 = Math.toRadians(gps1.values[0]);
        double lng2 = Math.toRadians(gps1.values[1]);

        double p1 = Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng1-lng2);
        double p2 = Math.sin(lat1)*Math.sin(lat2);

        double res = Math.acos(p1 + p2);
        if(res< Constants.kSmallEPSILON || res!=res) {
            res = 0.0;
        }
        //Log.log("dis:", res);
        return res * Constants.kEarthRadius;
    }
}
