package com.example.ehar.SensorExample;

import android.location.Location;
import android.os.Handler;

import java.util.logging.LogRecord;

public class Utility {
    // calculates distance on a sphere
    // d = arccos(sin(lat1) sin(lat2) + cos(lat1) cos(lat2) cos(lon2 − lon1) ) R
    public static double greatCircleDistance(double lat1, double long1, double lat2, double long2){
        double latitude1 = Math.toRadians(lat1);
        double longitude1 = Math.toRadians(long1);
        double latitude2 = Math.toRadians(lat2);
        double longitude2 = Math.toRadians(long2);
        final double EARTH_RADIUS = 6371.0;
        double sins = Math.sin(latitude1)*Math.sin(latitude2);
        double longs = longitude2 - longitude1;
        double coss = Math.cos(latitude1)*Math.cos(latitude2)*Math.cos(longs);
        double arc = sins + coss;

        double d = Math.acos(arc);

        d *= EARTH_RADIUS;
        //return the distance
        return d;
    }

    // Borrowed the following function from the below source.
    // https://stackoverflow.com/questions/9457988/bearing-from-one-coordinate-to-another
    // Returns the direction traveled from the two locations.
    public static String compassHeading(Location prev, Location curr) {
        double prevLat = Math.toRadians(prev.getLatitude());
        double prevLon = Math.toRadians(prev.getLongitude());
        double currLat = Math.toRadians(curr.getLatitude());
        double currLon = Math.toRadians(curr.getLongitude());
        double diffPts = Math.toRadians(prevLon - currLon);

        double y= Math.sin(diffPts)*Math.cos(currLat);
        double x=Math.cos(prevLat)*Math.sin(currLat)-Math.sin(prevLat)*
                Math.cos(currLat)*Math.cos(diffPts);

        double degrees = (Math.toDegrees(Math.atan2(y, x))+360)%360;

        String directions[] = {"N","NNE", "NE","ENE","E", "ESE","SE","SSE", "S","SSW",
                "SW","WSW", "W","WNW", "NW","NNW", "N"};
        double direction = Math.round(degrees / 22.5);
        if (direction < 0) {
            direction = direction + 16;
        }
        return directions[(int) direction];
    }

    public static void delayedRunOnUiThread(
            final MainActivity mainActivity,
            int delay,
            final Runnable runnable
    ) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mainActivity.runOnUiThread(runnable);
            }
        }, delay);
    }
}
