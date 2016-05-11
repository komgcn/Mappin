package com.example.gary.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, OnMapClickListener, LocationListener, OnMyLocationButtonClickListener {

    private static final long update_interval = 10000;
    private static final long fastest_interval = 5000;
    private GoogleApiClient client;
    private GoogleMap map;
    private Location location, lastLocation;
    private AddressResultReceiver receiver;
    private static final String query_key = "address";
    private EditText editTextLocation;
    private String address = "";
    private boolean location_update_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        editTextLocation = (EditText) findViewById(R.id.text_location);
        receiver = new AddressResultReceiver(new Handler());
        location_update_flag = false;

        Log.d(getString(R.string.debug_tag), "User enters report page.");

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.report_map);
        mapFragment.getMapAsync(this);
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
        UiSettings ui = map.getUiSettings();
        ui.setZoomControlsEnabled(true);
        drawMap();
        client.connect();
    }

    public void reportWrite(View view) {
        Log.d(getString(R.string.debug_tag), "User click on report! button.");
        String userFill = editTextLocation.getText().toString();
        if (!address.equals(userFill)) {
            if (userFill.isEmpty()) {
                Toast.makeText(this, "Please fill in report location.", Toast.LENGTH_LONG).show();
                Log.d(getString(R.string.debug_tag), "Location field emptry, please fill in report location toast shown to user.");
            } else {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(userFill, 1);
                    if (addresses == null || addresses.size() == 0) {
                        Toast.makeText(this, "Cannot find address' latlng.", Toast.LENGTH_LONG).show();
                        Log.d(getString(R.string.debug_tag), "Cannot find address' latlng toast shown to user.");
                    } else {
                        address = userFill;
                        location.setLatitude(addresses.get(0).getLatitude());
                        location.setLongitude(addresses.get(0).getLongitude());
                        Log.d(getString(R.string.debug_tag), "Location set to " + location.getLatitude() + "," + location.getLongitude());
                        startReportWrite();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (location == null) {
                Toast.makeText(this, "Please select a location to report.", Toast.LENGTH_LONG).show();
                Log.d(getString(R.string.debug_tag), "Location null, select location to report toast shown to user.");
            } else {
                startReportWrite();
            }
        }
    }

    private void startReportWrite() {
        Intent intent = new Intent(this, ReportWriteActivity.class);
        intent.putExtra("location", location);
        intent.putExtra("address", address);
        startActivity(intent);
    }

    private void drawMap() {
        Log.d(getString(R.string.debug_tag), "Drawing map.");
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
    }

    /***
     * Map motion and gesture code
     ***/
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(getString(R.string.debug_tag), "User click on map: " + latLng.latitude + "," + latLng.longitude);
        map.clear();
        drawMap();
        map.addMarker(new MarkerOptions().position(latLng));
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        startIntentService();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(getString(R.string.debug_tag), "User click on my location button");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            updateMapCamera();
            if (!location_update_flag) {
                startLocationUpdates();
            }
        }
        return true;
    }

    /***
     * Google API Client code
     ***/
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getString(R.string.debug_tag), "Google api client connected.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.MY_PERMISSION_REQUEST_FINE_LOCATION);
        } else if (map != null) {
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMapClickListener(this);
            location = LocationServices.FusedLocationApi.getLastLocation(client);
            lastLocation = location;
            if (location != null) {
                LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latlng, 17);
                map.animateCamera(camera);
                Log.d(getString(R.string.debug_tag), "Map camera animated.");
                startIntentService();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Google service network lost or disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        Log.d(getString(R.string.debug_tag), "Google api client connection suspended, toast shown to user.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getString(R.string.debug_tag), "Google api client connection failed.");
    }

    /***
     * Location update code
     ***/
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

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getString(R.string.debug_tag), "Location changed report.");
        lastLocation = location;
        updateMapCamera();
    }

    public void updateMapCamera() {
        LatLng latlng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latlng, 17);
        map.animateCamera(camera);
        Log.d(getString(R.string.debug_tag), "Map camera updated report.");
    }

    /***
     * Location address geocoding code
     ***/
    protected void startIntentService() {
        Log.d(getString(R.string.debug_tag), "Starting intent service for location: " + location.getLatitude() + "," + location.getLongitude());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, receiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        intent.putExtra(FetchAddressIntentService.Constants.QUERY_TYPE, query_key);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    public class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                address = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
                editTextLocation.setText(address);
                Toast.makeText(ReportActivity.this, R.string.address_found, Toast.LENGTH_SHORT).show();
                Log.d(getString(R.string.debug_tag), "Address found toast shown, edittextlocation set to " + address);
            }
        }
    }

    public void showHelp(View view) {
        Log.d(getString(R.string.debug_tag), "User clicks on Help button.");
        DialogFragment frag = new HelpDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HelpDialogFragment.HELP_MSG, getString(R.string.report_help_msg));
        frag.setArguments(bundle);
        frag.show(getFragmentManager(), HelpDialogFragment.HELP_TAG);
    }


}
