package assignment.mobile.locationlogger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;






import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


/**
 * This application uses location data to calculate the distance between the starting point and the
 * ending point.  It calculates two types of distance.  One is a continuous distance and the other
 * is the distance between the start point and the end point.  It also sends the location data to
 * a CartoDB database for visual representation.
 *
 * @author Joshua Coyle.
 * @author Robert Slavik
 */
public class LocationMain extends Activity implements SettingsFragment.settingsListener{
    //TextView Objects
    private TextView textLocationView;
    private TextView battTextView;
    private TextView distanceTextview;
    //Location Objects
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;

    private float[] distanceArray;
    //Settings for Database and Location
    private String name;
    private int updateLocationTimer;
    private int updateLocationDistance;
    private int locationAccuracySetting;
    private String observation;

    //  Menu
    ActionBar actionBar;
    //  Menu
    private boolean settingsBoolean = false;
    private boolean mapBoolean = false;
    //Settings Fragment Object
    SettingsFragment settings;
    //Map Fragment Object
    MapView map;
    //Holds failed URLs
    ArrayList<String> failedHttpCallUrls;
    //List of Locations
    private ArrayList locationList;
    //HTML holder
    private WebView mWebView;
    //Used to hold continuous distance calculation
    private float continuousDistance = -1.0f;
    //Location object hold previous location
    private Location previousLocation;

    /**
     * Part of Application Lifecycle
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        actionBar = getActionBar();

        setContentView(R.layout.main);
        setupItems();
        setupLocation();
    }


    /**
     *This method sets up the Location Object and corresponding listener
     */
    private void setupLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location.getAccuracy() < locationAccuracySetting) {
                    //add location to location Array list
                    locationList.add(location);
                    //call method to calculate continuous distance
                    calculateContinuous(location);
                    //calculate straight line distance from start to stop
                    updateLocationInfo(location);

                    sendLocationInfoGet(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //Not Used
            }

            @Override
            public void onProviderEnabled(String provider) {
                //Not Used
            }

            @Override
            public void onProviderDisabled(String provider) {
                //Not Used
            }
        };
    }

    /**
     * This method is used to calculate the continuous distance
     * @param location - The New Location
     */
    private void calculateContinuous(Location location) {
        if(continuousDistance == -1.0f){
            previousLocation = location;
            continuousDistance = 0.0f;
        }else{
            continuousDistance += previousLocation.distanceTo(location);
            previousLocation = location;
        }
    }

    /**
     * This method is used to display the current Lat Lon Data point
     * @param location
     */
    private void updateLocationInfo(Location location) {
        textLocationView.append("Lat: " + location.getLatitude() + "  Lon: " + location.getLongitude() + "\n");
        textLocationView.append("\n");
        }

    /**
     *This method is called to setup UI items
     */
    private void setupItems() {

        mWebView = new WebView(this);
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");
        final Activity activity = this;
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//              Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                failedHttpCallUrls.add(failingUrl);
            }
        });

        failedHttpCallUrls = new ArrayList<>();
        //location Listener
        location = new Location("distance");

        //Connect XML UI Items
        textLocationView = (TextView) findViewById(R.id.textLocation);
        textLocationView.setMovementMethod(new ScrollingMovementMethod());

        battTextView = (TextView) findViewById(R.id.battLevel);
        distanceTextview = (TextView) findViewById(R.id.distanceText);
        final Button stop = (Button) findViewById(R.id.stopButton);
        final Button start = (Button) findViewById(R.id.startButton);

        //data for database
        observation = "location2";
        name = "josh";

        //Location update settings
        updateLocationDistance = 0;
        updateLocationTimer = 0;
        locationAccuracySetting = 10;

        // Menu
        settings = new SettingsFragment();
        map = new MapView();
        //Menu

        //Needed to store result when calling location method distanceBetween
        distanceArray = new float[5];
        //Array List to Hold location objects
        locationList = new ArrayList();



        start.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick Listener for Start Button
             * @param v
             */
            @Override
            public void onClick(View v) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateLocationTimer, updateLocationDistance, locationListener);
                //Clear Out Previous Info
                textLocationView.setText("");
                distanceTextview.setText("");
                //Clear Text field and Display Start Batt Level
                battTextView.setText("");
                battTextView.setText("Start Battery Level: " + Float.toString(getBatteryLevel()) + "\n");
                //Show Stop Button Hide Start Button
                stop.setVisibility(View.VISIBLE);
                start.setVisibility(View.INVISIBLE);


            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick Listener for Stop Button
             * @param v
             */
            @Override
            public void onClick(View v) {
                calculateStartEnd();
                //Show Start Button Hide Stop Button
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                //Stop getting updates
                locationManager.removeUpdates(locationListener);
                tryFailedUrls();
                displayResults();
            }
        });
    }

    /**
     *This method is called on button stop pressed,
     * tries to resend failed URLS.  Will only retry 75 to prevent an infinite loop situation
     * Could lead to data loss
     */
    private void tryFailedUrls() {
        String url = "";
        int count = 0;
        if(failedHttpCallUrls.size() != 0) {
            do {
                url = failedHttpCallUrls.get(0);
                failedHttpCallUrls.remove(0);
                mWebView.loadUrl(url);
                count++;
            } while (failedHttpCallUrls.size() != 0 && count < 75);
        }
    }

    //Menu

    /**
     * Inflate the menu
     * @param menu - Menu Object
     * @return boolean - Was successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Menu

    /**
     * This method is used to select the method that corresponds
     * with the selected menu item
     * @param item - Selected menu item
     * @return boolean - Was the menu Item found
     */
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.settings:
                callSettings();
                return true;
            case R.id.map:
                switchToMapView();
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }

    }


    /**
     *Called to bring up the map view
     */
    private void switchToMapView() {
        if(!mapBoolean) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.root, map);
            fragmentTransaction.commit();
            mapBoolean = true;
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(map);
            fragmentTransaction.commit();
            mapBoolean = false;
        }
    }

    /**
     * This method gets the current time and converts it into ISO format for Sending
     * @return - String Current Data and Time
     */
    private String getCurrentTime(){
        String isoTime;
        TimeZone tz = TimeZone.getTimeZone("America/Denver");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        isoTime = df.format(new Date());
        return isoTime;
    }

    /**
     * Method called to change current settings
     * @param newSettings
     */
    public void updateSettings(SettingsObject newSettings){
        observation = newSettings.getObservation();
        name = newSettings.getName();
        updateLocationTimer = newSettings.getTimerFrequencyVariable();
        updateLocationDistance = newSettings.getDistanceUpdateVariable();

    }


    /**
     * This method is called to send data to database
     * @param location
     */
    private void sendLocationInfoGet(Location location) {


        //Lon then Lat
        //Build URL
        String url = "http://coyle5280.cartodb.com/api/v2/sql?q=INSERT INTO " +
                "mobile_data(the_geom, observation, timestamp, name, battery_level)" +
                " VALUES (ST_GeomFromText('POINT(" + location.getLongitude() + " " +
                location.getLatitude() + ")', 4326),' " + observation + " ','" +
                getCurrentTime() + "','" + name + "','" + getBatteryLevel() + "')" +
                "&api_key=baba08a4c371dc7ed93027f141d0f425c4606c45";


        //Send Data to database
        mWebView.loadUrl(url);
        //Used for testing
        Log.i("another Try", url);
//        StringRequest stringRequest = new StringRequest(url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Log response
//                        Log.i("response Success", response);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("response get", error.getMessage(), error);
//            }
//        });
//        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//                params.put("Accept-Encoding", "gzip, deflate, sdch");
//                params.put("Accept-Language", "en-US,en;q=0.8");
//                params.put("Cache-Control", "max-age=0");
//                params.put("Connection", "keep-alive");
//                params.put("Accept-Encoding", "gzip, deflate, sdch");
//                params.put("Host", "coyle5280.cartodb.com");
//                params.put("Remote Address", "coyle5280.cartodb.com");
//                return params;
//            }
//        };
//        queue.add(stringRequest);






    }

    /**
     * This method calculates the current battery level
     * @return float - the Current Battery Level
     */
    private float getBatteryLevel (){
        float batteryLevel;
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, intentFilter);
        int batteryStartLevelInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        Log.i("batStartLevelInt", Integer.toString(batteryStartLevelInt));
        int batteryStartScaleInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        Log.i("batStartScaleInt", Integer.toString(batteryStartScaleInt));
        batteryLevel = batteryStartLevelInt/(float) batteryStartScaleInt;
        return  batteryLevel;
    }

    /**
     *This method is used to calculate the continuous distance
     */
    private void calculateStartEnd() {
        if (locationList.size() > 1) {
            int lastEntryIndex = (locationList.size() - 1);
            Location start_Location = (Location) locationList.get(0);
            Location stop_Location = (Location) locationList.get(lastEntryIndex);
            try {
                location.distanceBetween(start_Location.getLatitude(),start_Location.getLongitude(),
                        stop_Location.getLatitude(), stop_Location.getLongitude(), distanceArray);
            } catch (IllegalArgumentException e) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Error " + e.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }


        }else{
            distanceTextview.setText("Error You Do Not Have Enough Locations");
        }
    }

    /**
     *Used to display the settings fragment
     */
    private void callSettings() {
        if(!settingsBoolean) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.root, settings);
            fragmentTransaction.commit();
            settingsBoolean = true;
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(settings);
            fragmentTransaction.commit();
            settingsBoolean = false;
        }
    }

    /**
     *Method called to display results when stop button pressed
     */
    private void displayResults(){

        //Battery Level At End
        battTextView.append("End Battery Level: " + Float.toString(getBatteryLevel()));


        //Display Straight Line Distance
        if (distanceArray[0] > 1 && continuousDistance != -1.0f) {
            distanceTextview.setText("Start/End Distance: " +
                    Float.toString(distanceArray[0]) + "\n");
            distanceTextview.append("Continuous Distance: " + Float.toString(continuousDistance));
        } else {
            distanceTextview.setText("Error You Did Not Travel Any Distance");
        }
    }



}
