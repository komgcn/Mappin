package com.example.gary.myapplication;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, OnMyLocationButtonClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnPolylineClickListener {

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final long update_interval = 10000;
    private static final long fastest_interval = 5000;
    private static final int event_distance_tolerance = 50;
    private static final int dark_blue = Color.rgb(0, 0, 255);
    private static final int light_blue = Color.rgb(0, 249, 250);
    private static final int dark_green = Color.rgb(0, 128, 0);
    private static final int light_green = Color.rgb(0, 255, 4);
    private static final int dark_yellow = Color.rgb(255, 167, 0);
    private static final int light_yellow = Color.rgb(255, 255, 0);
    private static final int dark_red = Color.rgb(255, 0, 0);
    private static final int light_red = Color.rgb(255, 0, 231);
    private static final int[] blue = {dark_blue, light_blue};
    private static final int[] green = {dark_green, light_green};
    private static final int[] yellow = {dark_yellow, light_yellow};
    private static final int[] red = {dark_red, light_red};
    private Location lastLocation, targetLocation;
    private ArrayList<Journey> routes;
    private GoogleApiClient client;
    private GoogleMap map;
    private CameraUpdate camera;
    private HashMap<Integer, int[]> colormap = new HashMap<>();
    private HashMap<Integer, Integer> bicolormap = new HashMap<>();
    private LatLngBounds routeBounds = null;
    private Boolean location_update_flag;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> instructionList = new ArrayList<>();
    private TextView warnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        warnings = (TextView) findViewById(R.id.warnings);
        warnings.setTextColor(Color.RED);

        Intent intent = getIntent();
        lastLocation = intent.getParcelableExtra("last_location");
        targetLocation = intent.getParcelableExtra("target_location");
        routes = intent.getParcelableArrayListExtra("routes");

        Log.d(getString(R.string.debug_tag), "User enters route result page.");

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.guide_map);
        mapFragment.getMapAsync(this);

        colormap.put(0, blue);
        colormap.put(1, green);
        colormap.put(2, yellow);
        colormap.put(3, red);
        bicolormap.put(dark_blue, 0);
        bicolormap.put(light_blue, 0);
        bicolormap.put(dark_green, 1);
        bicolormap.put(light_green, 1);
        bicolormap.put(dark_yellow, 2);
        bicolormap.put(light_yellow, 2);
        bicolormap.put(dark_red, 3);
        bicolormap.put(light_red, 3);

        location_update_flag = false;

        ListView listView = (ListView) findViewById(R.id.instruction_listview);
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, instructionList);
        listView.setAdapter(mArrayAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (location_update_flag) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(getString(R.string.debug_tag), "Google map ready.");
        map = googleMap;
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        if (routes != null) {
            for (Journey route : routes) {
                computeScores(route);
            }
            Collections.sort(routes);
            drawMap(0);
        }
        client.connect();
    }

    private void computeScores(Journey route) {
        Log.d(getString(R.string.debug_tag), "Computing scores.");
        ArrayList<Step> steps = route.getSteps();
        List<LatLng> polyline = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            polyline.addAll(steps.get(i).getPolyline());
        }
        for (int i = 0; i < HomeActivity.roadEvents.size(); i++) {
            RoadEvent event = HomeActivity.roadEvents.get(i);
            if (event.isLoc()) {
                LatLng latlng = new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude());
                if (PolyUtil.isLocationOnPath(latlng, polyline, true, event_distance_tolerance)) {
                    route.scorePlus(20 + event.getLikes());
                }
            }
        }
    }

    /***
     * GoogleApiClient and Location
     ***/
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getString(R.string.debug_tag), "Google api client connected.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        } else if (map != null) {
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnPolylineClickListener(this);
            if (routeBounds != null) {
                camera = CameraUpdateFactory.newLatLngBounds(routeBounds, 50);
                map.animateCamera(camera);
            }
            Log.d(getString(R.string.debug_tag), "Map camera animated.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Google service network lost or disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        Log.d(getString(R.string.debug_tag), "api client connection suspended, toast shown to user.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(getString(R.string.debug_tag), "api client connection failed.");
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(update_interval);
        locationRequest.setFastestInterval(fastest_interval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
            location_update_flag = true;
            Log.d(getString(R.string.debug_tag), "Start location updates.");
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        location_update_flag = false;
        Log.d(getString(R.string.debug_tag), "Stop location updates.");
    }

    /***
     * Location and camera codes
     ***/
    @Override
    public void onLocationChanged(Location location) {
        Log.d(getString(R.string.debug_tag), "Location changed.");
        lastLocation = location;
        updateMapCamera();
    }

    private void updateMapCamera() {
        LatLng latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        camera = CameraUpdateFactory.newLatLngZoom(latlng, 17);
        map.animateCamera(camera);
        Log.d(getString(R.string.debug_tag), "Map camera updated.");
    }

    /**
     * Motion and gestures
     **/
    public void reportTraffic(View view) {
        Log.d(getString(R.string.debug_tag), "User click on \"report traffic\" button.");
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    public void stopUpdateCamera(View view) {
        Log.d(getString(R.string.debug_tag), "User click on Stop update button");
        Toast.makeText(this, "Location update stopped", Toast.LENGTH_LONG).show();
        stopLocationUpdates();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(getString(R.string.debug_tag), "User click on my location button");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Start location update", Toast.LENGTH_LONG).show();
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            updateMapCamera();
            if (!location_update_flag) {
                startLocationUpdates();
            }
        }
        return true;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        map.clear();
        int route_num = bicolormap.get(polyline.getColor());
        Log.d(getString(R.string.debug_tag), "User click on polyline " + route_num);
        Journey route = routes.get(route_num);
        drawMap(route_num);
        displayInstruction(route);
    }

    /***
     * Polyline and instructions drawing
     ***/
    private void drawMap(int route_num) {
        Log.d(getString(R.string.debug_tag), "Drawing map with route num: " + route_num);
        for (RoadEvent re : HomeActivity.roadEvents) {
            if (re.isLoc()) {
                LatLng latlng = new LatLng(re.getLocation().getLatitude(), re.getLocation().getLongitude());
                map.addMarker(new MarkerOptions()
                        .position(latlng)
                        .title(re.getMessage())
                        .snippet(String.valueOf(re.getLikes()) + " likes")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
            }
        }
        map.addMarker(new MarkerOptions()
                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .title(getString(R.string.start_location_marker)));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude()))
                .title(getString(R.string.end_location_marker)));


        int index = 5;
        int highIndex = 10;

        for (int i = 0; i < routes.size(); i++) {

            Journey route = routes.get(i);
            ArrayList<Step> steps = route.getSteps();


            for (int j = 0; j < steps.size(); j++) {
                Step step = steps.get(j);
                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.addAll(step.getPolyline());
                if (i == route_num) {
                    lineOptions.width(25);
                    lineOptions.zIndex(highIndex);
                } else {
                    lineOptions.width(10);
                    lineOptions.zIndex(index);
                }
                if (step.getTravelMode() == 1) {
                    lineOptions.color(colormap.get(i)[0]);
                } else {
                    lineOptions.color(colormap.get(i)[1]);
                }
                lineOptions.geodesic(true);
                Polyline line = map.addPolyline(lineOptions);
                line.setClickable(true);
            }
            index--;

            Log.d(getString(R.string.debug_tag), route.getDurationText());
            Log.d(getString(R.string.debug_tag), "Route " + i + " score of " + route.getScore());
            if (i == 0) {
                routeBounds = new LatLngBounds(route.getSWBound(), route.getNEBound());
                displayInstruction(route);
            }
        }
    }

    private void displayInstruction(Journey route) {
        Log.d(getString(R.string.debug_tag), "Displaying instructions.");
        instructionList.clear();
        ArrayList<String> warning = route.getWarning();
        StringBuilder sb = new StringBuilder();
        for (String s : warning) {
            sb.append(s);
        }
        String display_warning = "Warning: " + sb.toString();
        warnings.setText(display_warning);
        instructionList.add("Estimated travel time: " + route.getScore() + " min");
        ArrayList<Step> steps = route.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            if (step.getTravelMode() == 1) {
                instructionList.add(step.getDepartureTime() + ". " + step.getInstruction() + " (Bus: " + step.getBusName() + ". " + step.getStops() + " stops. Arrival stop: " + step.getArrivalStop() + ")");
            } else {
                instructionList.add(step.getInstruction() + " (" + step.getDistance() + ")");
            }
        }
        mArrayAdapter.notifyDataSetChanged();
    }

    public void showHelp(View view) {
        Log.d(getString(R.string.debug_tag), "User clicks on Help button.");
        DialogFragment frag = new HelpDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HelpDialogFragment.HELP_MSG, getString(R.string.map_help_msg));
        frag.setArguments(bundle);
        frag.show(getFragmentManager(), HelpDialogFragment.HELP_TAG);
    }
}
