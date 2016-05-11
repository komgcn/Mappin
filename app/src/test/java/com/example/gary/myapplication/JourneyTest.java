package com.example.gary.myapplication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by gary on 30/03/2016.
 */
public class JourneyTest {

    @Test
    public void setDuration_valueLessThanSixty_setScoreToOne() {
        Journey journey = new Journey();
        journey.setDuration("1 min", 40);
        assertEquals(journey.getScore(), 1);
    }

    @Test
    public void setDuration_valueIsSixty_setScoreToOne() {
        Journey journey = new Journey();
        journey.setDuration("1 min", 60);
        assertEquals(journey.getScore(), 1);
    }

    @Test
    public void setDuration_valueMoreThanSixty_setScoreToMinutesPlusOne() {
        Journey journey = new Journey();
        journey.setDuration("3 min", 200);
        assertEquals(journey.getScore(), 4);
    }
}
