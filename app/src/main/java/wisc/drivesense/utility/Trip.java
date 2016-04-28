package wisc.drivesense.utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lkang on 3/29/16.
 */
public class Trip implements Serializable {

    private long startTime_ = 0;
    private long endTime_ = 0;
    private double distance_ = 0; // in miles
    private double speed_ = 0.0;
    private double score_ = 10.0;
    private List<Trace> gps_;
    private Trace start_ = null;
    private Trace dest_ = new Trace();

    //private DatabaseHelper dbHelper_ = null;

    public Trip (long time) {
        gps_ = new ArrayList<Trace>();
        this.startTime_ = time;
    }

    public void setScore(double score) {this.score_ = score;}


    public long getStartTime() {
        return this.startTime_;
    }
    public long getEndTime() {
        return this.endTime_;
    }
    public double getDistance() {
        return this.distance_ * Constants.kMeterToMile;
    }
    public double getScore() {return this.score_;}
    public long getDuration() {return this.endTime_ - this.startTime_;}

    public Trace getStartPoint() {return start_;}
    public Trace getEndPoint() {return dest_;}
    public double getSpeed() {return speed_ * Constants.kMeterPSToMilePH;}

    public void addGPS(Trace trace) {
        gps_.add(trace);
        if(start_ == null) {
            start_ = new Trace();
            start_.copyTrace(trace);
        }
        dest_.copyTrace(trace);
        speed_ = trace.values[2];
        this.endTime_ = trace.time;

        int sz = gps_.size();
        if(sz >= 2) {
            distance_ += distance(gps_.get(sz - 2), gps_.get(sz - 1));
        }
    }


    public void setGPSPoints(List<Trace> gps) {
        for(int i = 0; i < gps.size(); ++i) {
            this.addGPS(gps.get(i));
        }
    }
    public List<Trace> getGPSPoints() {
        return gps_;
    }



    public static double distance(Trace gps0, Trace gps1) {

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
