package com.example.ehar.SensorExample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


public class MainActivity
        extends AppCompatActivity
        implements Observer {

    // TextViews
    private TextView accel_x_view = null;
    private TextView accel_y_view = null;
    private TextView starting_coordinates = null;
    private TextView ending_coordinates = null;
    private TextView current_coordinates = null;
    private TextView current_velocity = null;
    private TextView total_distance = null;
    private TextView total_velocity = null;

    // Buttons
    private Button start, stop, drop_pin, reset;

    // New TableLayout
    TableLayout stk;

    // Location, Handler, and Manager
    private Location startLocation;
    private Location endLocation;
    private Location prevLocation;
    private Observable accel;
    private LocationHandler location;
    private LocationManager lm;

    // Counter for pin clicks
    private int pincount = 0;
    private double totalDistance = 0;

    // Times for total velocity
    private long startTime;
    private long totalTime;
    private long endTime;


    //Sounds
    MediaPlayer mpPin;
    MediaPlayer mpStop;
    MediaPlayer mpStart;
    MediaPlayer mpReset;

    // Decimal format
    DecimalFormat df = new DecimalFormat("0.####");

    final public static int REQUEST_ASK_FINE_LOCATION = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize all views
        init();
        addTableHeaders();

        // Start button onClick
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get location when user hits start
                startLocation = getLastKnownLocation();
                double start_lat = startLocation.getLatitude();
                double start_long = startLocation.getLongitude();

                // Save start location for first pin
                prevLocation = startLocation;

                // Set starting coordinates texts
                starting_coordinates.setText(("(" + df.format(start_lat) + ", " +
                                            df.format(start_long) + ")"));

                // Save the time the timer was set
                startTime = System.currentTimeMillis();
                totalTime = 0;

                // Disable Buttons and change
                // colors accordingly
                start.setEnabled(false);
                start.setBackgroundColor(Color.GRAY);
                stop.setEnabled(true);
                stop.setBackgroundColor(Color.RED);
                drop_pin.setEnabled(true);
                drop_pin.setBackgroundColor(Color.CYAN);

                // Sound for start button
                mpStart.start();
            }
        });

        // Stop button onClick
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Save the end time
                endTime = System.currentTimeMillis();

                // Get location when user hits stop
                endLocation = getLastKnownLocation();
                double end_lat = endLocation.getLatitude();
                double end_long = endLocation.getLongitude();

                // Set the ending coordinates text
                ending_coordinates.setText(("(" + df.format(end_lat) + ", " +
                                        df.format(end_long) + ")"));

                // Set the overall velocity
                double totalTime = (endTime - startTime) / 1000.0;
                double avgVel = totalDistance / totalTime;
                total_velocity.setText(df.format(avgVel) + " m/s");

                // Change button enabled and
                // colors accordingly
                stop.setEnabled(false);
                stop.setBackgroundColor(Color.GRAY);
                drop_pin.setEnabled(false);
                drop_pin.setBackgroundColor(Color.GRAY);

                // Save the end time
                endTime = System.currentTimeMillis();

                double distance_temp = (1000* Utility.greatCircleDistance(
                        prevLocation.getLatitude(),
                        prevLocation.getLongitude(),
                        endLocation.getLatitude(),
                        endLocation.getLongitude()));

                totalDistance += distance_temp;

                total_distance.setText(df.format(totalDistance) + " meters");

                // Sound for stop button
                mpStop.start();
            }
        });

        // Drop pin button onClick
        drop_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Update pin counter
                pincount ++;

                // Create a new pin
                addNewPin();

                // Sound for Pin button
                mpPin.start();
            }
        });


        // Reset button onClick
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Remove all views and re add headers
                stk.removeAllViews();
                addTableHeaders();
                pincount = 0;
                totalDistance = 0;

                // Reset text of starting/ending coordinates
                starting_coordinates.setText("(0, 0)");
                ending_coordinates.setText("(0, 0)");
                total_distance.setText("0 meters");
                total_velocity.setText("0 m/s");

                // Reset all the buttons
                drop_pin.setEnabled(false);
                drop_pin.setBackgroundColor(Color.GRAY);
                start.setEnabled(true);
                start.setBackgroundColor(Color.GREEN);
                stop.setEnabled(false);
                stop.setBackgroundColor(Color.GRAY);

                // Sound for reset button
                mpReset.start();
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {

        // Observable acceleration change
        if (observable instanceof AccelerometerHandler) {
            float[] values = (float[]) o;
            accel_x_view.setText(Float.toString(values[0]));
            accel_y_view.setText(Float.toString(values[1]));

        }

        // Observable location change
        else if (observable instanceof LocationHandler) {
            // constantly get current location
            Location l = (Location) o;
            double lat = l.getLatitude();
            double lon = l.getLongitude();
            // Constantly update velocity
            instantaneousVelocity();

            // Update current coordinates on the fly
            current_coordinates.setText(("(" + df.format(lat) + ", " +
                                        df.format(lon) + ")"));
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ASK_FINE_LOCATION)
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.i("INFO: ", "Permission not granted for location.");
            }
            else {
                Log.i("INFO: ", "Permission granted for location.");
                //this.location.initializeLocationManager();
            }
    }

    private Location getLastKnownLocation() {
        lm = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            for (String provider : providers) {
                Location l = lm.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }


    // Initialize all views to clean up code
    public void init() {

        // Find views
        accel_x_view = (TextView) findViewById(R.id.accel_x);
        accel_y_view = (TextView) findViewById(R.id.accel_y);
        starting_coordinates = (TextView) findViewById(R.id.starting_coordinates);
        ending_coordinates = (TextView) findViewById(R.id.ending_coordinates);
        current_coordinates = (TextView) findViewById(R.id.current_coordinates);
        current_velocity = (TextView) findViewById(R.id.current_velocity);
        total_distance = (TextView) findViewById(R.id.total_distance);
        total_velocity = (TextView) findViewById(R.id.total_velcoity);

        // Find buttons
        start = (Button) findViewById(R.id.start_button);
        stop = (Button) findViewById(R.id.stop_button);
        drop_pin = (Button) findViewById(R.id.pin_button);
        reset = (Button) findViewById(R.id.reset_button);

        // Initialize MediaPlayers for our sounds
        mpPin = MediaPlayer.create(this.getApplicationContext(), R.raw.droppin);
        mpStop = MediaPlayer.create(this.getApplicationContext(), R.raw.stoppin);
        mpStart = MediaPlayer.create(this.getApplicationContext(), R.raw.start);
        mpReset = MediaPlayer.create(this.getApplicationContext(), R.raw.resetpin);

        //Initialize button colors
        start.setBackgroundColor(Color.GREEN);
        stop.setBackgroundColor(Color.GRAY);
        drop_pin.setBackgroundColor(Color.GRAY);
        reset.setBackgroundColor(Color.YELLOW);

        //Properly enable buttons
        start.setEnabled(true);
        stop.setEnabled(false);
        drop_pin.setEnabled(false);
        reset.setEnabled(true);

        // Create new AccelerometerHandler
        accel = new AccelerometerHandler(500, this);
        accel.addObserver(this);

        // Create new LocationHandler
        location = new LocationHandler(this);
        location.addObserver(this);


    }


    /**
     * Borrowed from https://stackoverflow.com/questions/18207470
     * /adding-table-rows-dynamically-in-android/22682248#22682248
     */
    // Add new pin to the table
    public void addNewPin() {

        // Find table
        stk = (TableLayout) findViewById(R.id.table_main);

        // Get current location
        Location currentLocation = getLastKnownLocation();

        TableRow tbrow = new TableRow(this);

        // Pin Number
        TextView t1v = new TextView(this);
        // Latitude
        TextView t2v = new TextView(this);
        // Longitude
        TextView t3v = new TextView(this);
        // Distance
        TextView t4v = new TextView(this);
        // Velocity
        TextView t5v = new TextView(this);
        // Compass Direction
        TextView t6v = new TextView(this);

        setTableRows(t1v, t2v, t3v, t4v, t5v, t6v, tbrow, currentLocation);

        // add row view to the stack
        stk.addView(tbrow);

        // Save Previous location
        prevLocation = currentLocation;
    }

    public void addTableHeaders() {

        // Table layout
        // Adds first rows for categories
        // Drop Pin onClick should add data
        stk = (TableLayout) findViewById(R.id.table_main);

        // Pin Number
        TableRow tbrow = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText(" Pin Number ");
        tv0.setTextColor(Color.WHITE);
        tbrow.addView(tv0);

        // Latitude
        TextView tv1 = new TextView(this);
        tv1.setPadding(20,9,20,0);
        tv1.setText(" Latitude ");
        tv1.setTextColor(Color.WHITE);
        tbrow.addView(tv1);

        // Longitude
        TextView tv2 = new TextView(this);
        tv2.setPadding(20,9,20,0);
        tv2.setText(" Longitude ");
        tv2.setTextColor(Color.WHITE);
        tbrow.addView(tv2);

        // Distance
        TextView tv3 = new TextView(this);
        tv3.setPadding(20, 0, 20, 0);
        tv3.setText(" Distance From Previous");
        tv3.setTextColor(Color.WHITE);
        tbrow.addView(tv3);

        // Velocity
        TextView tv4 = new TextView(this);
        tv4.setText(" Velocity ");
        tv4.setTextColor(Color.WHITE);
        tbrow.addView(tv4);

        // Compass Direction
        TextView tv5 = new TextView(this);
        tv5.setPadding(20,0,0,0);
        tv5.setText(" Direction From Previous ");
        tv5.setTextColor(Color.WHITE);
        tbrow.addView(tv5);

        stk.addView(tbrow);
    }

    public void instantaneousVelocity() {
        Velocity v = new Velocity(getLastKnownLocation(), 10);
        Utility.delayedRunOnUiThread(this, 10000, v);
    }

    private class Velocity implements Runnable {
        private Location start;
        private  int time;
        public Velocity(Location start, int time) {
            this.start = start;
            this.time = time;
        }

        @Override
        public void run() {
            Location curr = getLastKnownLocation();
            double distanceTravelled = Utility.greatCircleDistance(
                    start.getLatitude(),
                    start.getLongitude(),
                    curr.getLatitude(),
                    curr.getLongitude());
            // Divide the distance travelled by
            // the time period it runs over
            double speed = distanceTravelled / this.time * 1000;
            String forTextView = df.format(speed) + " m/s";
            current_velocity.setText(forTextView);
        }
    }

    public void setTableRows(TextView a, TextView b, TextView c,
                             TextView d, TextView e, TextView f,
                             TableRow row, Location currloco) {

        // Pin Number
        a.setText(("" + pincount));
        a.setTextSize(20);
        a.setTextColor(Color.WHITE);
        a.setGravity(Gravity.CENTER);
        row.addView(a);

        // Latitude
        b.setText(df.format(currloco.getLatitude()));
        b.setTextSize(20);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        row.addView(b);

        // Longitude
        c.setText(df.format(currloco.getLongitude()));
        c.setTextSize(20);
        c.setTextColor(Color.WHITE);
        c.setGravity(Gravity.CENTER);
        row.addView(c);

        // Distance
        double distance_temp = (1000* Utility.greatCircleDistance(
                prevLocation.getLatitude(),
                prevLocation.getLongitude(),
                currloco.getLatitude(),
                currloco.getLongitude()));
        d.setText(df.format(distance_temp));
        // update total distance
        totalDistance = totalDistance + distance_temp;
        d.setTextSize(20);
        d.setTextColor(Color.WHITE);
        d.setGravity(Gravity.CENTER);
        row.addView(d);

        // Velocity
        long currentTime = System.currentTimeMillis();
        double timeDiff = (currentTime - totalTime - startTime) / 1000.0;
        double velocity = distance_temp / timeDiff;
        e.setText(df.format(velocity));
        e.setTextSize(20);
        e.setTextColor(Color.WHITE);
        e.setGravity(Gravity.CENTER);
        row.addView(e);

        // Compass Direction
        f.setText(Utility.compassHeading(prevLocation, currloco));
        f.setTextSize(20);
        f.setTextColor(Color.WHITE);
        f.setGravity(Gravity.CENTER);
        row.addView(f);

    }

}

