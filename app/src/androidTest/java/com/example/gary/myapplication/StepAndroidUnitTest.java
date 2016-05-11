package com.example.gary.myapplication;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Created by gary on 09/04/2016.
 */
@RunWith(AndroidJUnit4.class)
public class StepAndroidUnitTest {

    public static final String TEST_ARRIVAL_STOP = "SOTON STOP";
    public static final String TEST_BUS_SHORT = "U1C";
    public static final String TEST_BUS_NAME = "Unilink 1 to City Centre";
    public static final String TEST_INSTRUCTION = "Testing instruction 123.";
    public static final String TEST_DISTANCE = "1.7 km";
    public static final String TEST_DEPARTURE_TIME = "10:48am";
    public static final int TEST_STOPS = 7;
    public static final int TEST_TRAVEL_MODE = 1;
    public static final LatLng TEST_ONE = new LatLng(57.87,126.39);
    public static final LatLng TEST_TWO = new LatLng(24.78, 29.04);
    private Step step;

    @Before
    public void createStep(){
        step = new Step();
    }

    @Test
    public void step_ParcelableWriteRead(){

        List<LatLng> TEST_POLYLINE = new ArrayList<>();
        TEST_POLYLINE.add(TEST_ONE);
        TEST_POLYLINE.add(TEST_TWO);

        step.setArrivalStop(TEST_ARRIVAL_STOP);
        step.setBusShort(TEST_BUS_SHORT);
        step.setBusName(TEST_BUS_NAME);
        step.setInstruction(TEST_INSTRUCTION);
        step.setDistance(TEST_DISTANCE);
        step.setDepartureTime(TEST_DEPARTURE_TIME);
        step.setStops(TEST_STOPS);
        step.setTravelMode(TEST_TRAVEL_MODE);
        step.setPolyline(TEST_POLYLINE);

        Parcel parcel = Parcel.obtain();
        step.writeToParcel(parcel,step.describeContents());
        parcel.setDataPosition(0);

        Step parcelStep = Step.CREATOR.createFromParcel(parcel);
        List<LatLng> parcelPolyline = parcelStep.getPolyline();

        assertEquals(parcelStep.getArrivalStop(), TEST_ARRIVAL_STOP);
        assertEquals(parcelStep.getBusName(), TEST_BUS_SHORT);
        assertEquals(parcelStep.getInstruction(), TEST_INSTRUCTION);
        assertEquals(parcelStep.getDistance(), TEST_DISTANCE);
        assertEquals(parcelStep.getDepartureTime(), TEST_DEPARTURE_TIME);
        assertEquals(parcelStep.getStops(), TEST_STOPS);
        assertEquals(parcelStep.getTravelMode(), TEST_TRAVEL_MODE);
        assertEquals(parcelPolyline.get(0), TEST_ONE);
        assertEquals(parcelPolyline.get(1),TEST_TWO);
    }
}
