package com.example.gary.myapplication;

import android.location.Location;
import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by gary on 10/04/2016.
 */
@RunWith(AndroidJUnit4.class)
public class RoadEventAndroidUnitTest {

    public static final String TEST_MESSAGE = "Road accident causing traffic jam.";
    public static final String TEST_ADDRESS = "123 Abc Road";
    public static final int TEST_LIKES = 30;
    public static final double TEST_LAT = 27.32;
    public static final double TEST_LONG = -43.38;
    private RoadEvent roadEvent;

    @Before
    public void createRoadEvent() {
        roadEvent = new RoadEvent();
    }

    @Test
    public void roadEvent_ParcelableWriteRead() {
        Location TEST_LOCATION = new Location("");
        TEST_LOCATION.setLatitude(TEST_LAT);
        TEST_LOCATION.setLongitude(TEST_LONG);

        roadEvent.setMessage(TEST_MESSAGE);
        roadEvent.setAddress(TEST_ADDRESS);
        roadEvent.setLikes(TEST_LIKES);
        roadEvent.setLocation(TEST_LOCATION);

        Parcel parcel = Parcel.obtain();
        roadEvent.writeToParcel(parcel, roadEvent.describeContents());
        parcel.setDataPosition(0);

        RoadEvent parcelRoadEvent = RoadEvent.CREATOR.createFromParcel(parcel);

        assertEquals(parcelRoadEvent.getMessage(), TEST_MESSAGE);
        assertEquals(parcelRoadEvent.getAddress(), TEST_ADDRESS);
        assertEquals(parcelRoadEvent.getLikes(), TEST_LIKES);
        assertEquals(parcelRoadEvent.getLocation().getLatitude(), TEST_LAT, 0);
        assertEquals(parcelRoadEvent.getLocation().getLongitude(), TEST_LONG, 0);
    }
}
