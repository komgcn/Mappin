package com.example.gary.myapplication;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;


import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Test the Parcelable interface aspect of Journey class.
 * Created by gary on 09/04/2016.
 */

@RunWith(AndroidJUnit4.class)
public class JourneyAndroidUnitTest {

    public static final int TEST_DURATION = 37;
    public static final String TEST_DURATION_TEXT = String.valueOf(TEST_DURATION) + " mins";
    public static final int TEST_DURATION_VALUE = TEST_DURATION * 60;
    public static final int TEST_SCORE = TEST_DURATION;
    public static final String TEST_WARNING_ONE = "Warning: ";
    public static final String TEST_WARNING_TWO = " second line.";
    public static final LatLng TEST_NE_BOUND = new LatLng(73.48, 136.45);
    public static final LatLng TEST_SW_BOUND = new LatLng(-34.85, -123.98);
    public ArrayList<String> TEST_WARNINGS;
    public int STEP_SIZE;

    private Journey journey;

    @Before
    public void createJourney() {
        journey = new Journey();
    }

    @Test
    public void journey_ParcelableWriteRead() {

        TEST_WARNINGS = new ArrayList<>();
        TEST_WARNINGS.add(TEST_WARNING_ONE);
        TEST_WARNINGS.add(TEST_WARNING_TWO);

        journey.setDuration(TEST_DURATION_TEXT, TEST_DURATION_VALUE);
        journey.setWarning(TEST_WARNINGS);
        Random rnd = new Random();
        STEP_SIZE = rnd.nextInt(100) + 1;
        for (int i = 0; i < STEP_SIZE; i++) {
            Step step = new Step();
            journey.addStep(step);
        }
        journey.setNEBound(TEST_NE_BOUND);
        journey.setSWBound(TEST_SW_BOUND);

        Parcel parcel = Parcel.obtain();
        journey.writeToParcel(parcel, journey.describeContents());
        parcel.setDataPosition(0);

        Journey parcelJourney = Journey.CREATOR.createFromParcel(parcel);
        ArrayList<String> parcelWarning = parcelJourney.getWarning();

        assertEquals(parcelJourney.getDurationText(), TEST_DURATION_TEXT);
        assertEquals(parcelJourney.getScore(), TEST_SCORE);
        assertEquals(parcelWarning.size(), TEST_WARNINGS.size());
        for (int i = 0; i < parcelWarning.size(); i++) {
            assertEquals(parcelWarning.get(i), TEST_WARNINGS.get(i));
        }
        assertEquals(parcelJourney.getSteps().size(), STEP_SIZE);
        assertEquals(parcelJourney.getNEBound(), TEST_NE_BOUND);
        assertEquals(parcelJourney.getSWBound(), TEST_SW_BOUND);
    }
}
