package wisc.drivesense.utility;

/**
 * Created by lkang on 3/29/16.
 */
public class Constants {
    public static final double kEarthGravity = 9.80665; /*m^2/s*/

    /*for GPS*/
    public static final double kSmallEPSILON = 1e-8;
    public static final double kEarthRadius = 6371 * 1000; /*m*/

    public static final double kMeterToMile = 0.000621371;
    public static final double kMeterPSToMilePH = 2.23694;
    public static final double kKmPHToMPH = 0.621371;
    public static final double kKmPHToMeterPS = 0.277778;

    public static final String kInputSeperator = "\t";
    public static final String kOutputSeperator = "\t";
    public static final String slash = "/";

    public static final double kSampleRate = 1.0;
}
