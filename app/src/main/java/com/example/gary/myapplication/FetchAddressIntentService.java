package com.example.gary.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**Fetch address intent service
 * Created by gary on 03/02/2016.
 */
public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;
    private String address_type ="";
    private int num;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        String query_type = intent.getStringExtra(Constants.QUERY_TYPE);
        List<Address> addresses = null;

        try {
            if (query_type.equals("latlng")) {
                num = intent.getIntExtra(Constants.POSITION_NO, 0);
                String address = intent.getStringExtra(Constants.LOCATION_DATA_EXTRA);
                addresses = geocoder.getFromLocationName(address, 1);
            }
            if (query_type.equals("address")) {
                address_type = intent.getStringExtra(Constants.ADDRESS_TYPE);
                Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            }
        } catch (IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e(getString(R.string.debug_tag), errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(getString(R.string.debug_tag), errorMessage, illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(getString(R.string.debug_tag), errorMessage);
            }
        } else {
            Address address = addresses.get(0);
            if (query_type.equals("latlng")) {
                Location location = new Location("");
                location.setLatitude(address.getLatitude());
                location.setLongitude(address.getLongitude());
                deliverLocationToReceiver(Constants.SUCCESS_RESULT, location);
            }
            if (query_type.equals("address")) {
                ArrayList<String> addressFragments = new ArrayList<>();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.d(getString(R.string.debug_tag), getString(R.string.address_found));
                Log.d(getString(R.string.debug_tag), String.valueOf(Constants.SUCCESS_RESULT));
                Log.d(getString(R.string.debug_tag), TextUtils.join(System.getProperty("line.separator"), addressFragments));
                deliverAddressToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
            }
        }
    }

    private void deliverLocationToReceiver(int resultCode, Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY, location);
        bundle.putInt(Constants.POSITION_NO, num);
        mReceiver.send(resultCode, bundle);
    }

    private void deliverAddressToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        bundle.putString(Constants.ADDRESS_TYPE, address_type);
        mReceiver.send(resultCode, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final String PACKAGE_NAME = "com.example.gary.myapplication";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
        public static final String ADDRESS_TYPE = PACKAGE_NAME + ".ADDRESS_TYPE";
        public static final String QUERY_TYPE = PACKAGE_NAME + ".QUERY_TYPE";
        public static final String POSITION_NO = PACKAGE_NAME + ".POSITION_NO";
    }
}
