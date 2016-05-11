package com.example.gary.myapplication;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Journey and Step object.
 * Created by gary on 11/02/2016.
 */
public class Journey implements Comparable<Journey>, Parcelable {

    private String duration_text;
    private int score;
    private ArrayList<String> warning = null;
    private ArrayList<Step> steps;
    private LatLng NE_bound, SW_bound;

    public Journey() {
        steps = new ArrayList<>();
    }

    protected Journey(Parcel in) {
        duration_text = in.readString();
        score = in.readInt();
        warning = in.createStringArrayList();
        steps = in.createTypedArrayList(Step.CREATOR);
        NE_bound = in.readParcelable(LatLng.class.getClassLoader());
        SW_bound = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Journey> CREATOR = new Creator<Journey>() {
        @Override
        public Journey createFromParcel(Parcel in) {
            return new Journey(in);
        }

        @Override
        public Journey[] newArray(int size) {
            return new Journey[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(duration_text);
        dest.writeInt(score);
        dest.writeStringList(warning);
        dest.writeTypedList(steps);
        dest.writeParcelable(NE_bound, flags);
        dest.writeParcelable(SW_bound, flags);
    }

    public void scorePlus(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    public void addStep(Step step) {
        steps.add(step);
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }

    public void setWarning(ArrayList<String> warn) {
        warning = warn;
    }

    public ArrayList<String> getWarning() {
        return warning;
    }

    public void setDuration(String text, double value) {
        double min = 60;
        duration_text = text;
        score = (int) Math.ceil(value / min);

    }

    public String getDurationText() {
        return duration_text;
    }

    public void setNEBound(LatLng bound) {
        NE_bound = bound;
    }

    public LatLng getNEBound() {
        return NE_bound;
    }

    public void setSWBound(LatLng bound) {
        SW_bound = bound;
    }

    public LatLng getSWBound() {
        return SW_bound;
    }

    @Override
    public int compareTo(Journey compareJourney) {
        return this.getScore() - compareJourney.getScore();
    }
}

class Step implements Parcelable {
    private String arrival_stop, bus_short, bus_name, instruction, distance, departure_time;
    private int stops, travel_mode;
    private List<LatLng> polyline;

    public Step(){}

    protected Step(Parcel in) {
        arrival_stop = in.readString();
        bus_short = in.readString();
        bus_name = in.readString();
        instruction = in.readString();
        distance = in.readString();
        departure_time = in.readString();
        stops = in.readInt();
        travel_mode = in.readInt();
        polyline = in.createTypedArrayList(LatLng.CREATOR);
    }

    public static final Creator<Step> CREATOR = new Creator<Step>() {
        @Override
        public Step createFromParcel(Parcel in) {
            return new Step(in);
        }

        @Override
        public Step[] newArray(int size) {
            return new Step[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(arrival_stop);
        dest.writeString(bus_short);
        dest.writeString(bus_name);
        dest.writeString(instruction);
        dest.writeString(distance);
        dest.writeString(departure_time);
        dest.writeInt(stops);
        dest.writeInt(travel_mode);
        dest.writeTypedList(polyline);
    }

    public void setInstruction(String instruct) {
        instruction = instruct;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setDistance(String dis) {
        distance = dis;
    }

    public String getDistance() {
        return distance;
    }

    public void setDepartureTime(String time) {
        departure_time = time;
    }

    public String getDepartureTime() {
        return departure_time;
    }

    public void setArrivalStop(String stop) {
        arrival_stop = stop;
    }

    public String getArrivalStop() {
        return arrival_stop;
    }

    public void setBusShort(String b) {
        bus_short = b;
    }

    public void setBusName(String b) {
        bus_name = b;
    }

    public String getBusName() {
        if (bus_short != null) {
            return bus_short;
        } else {
            return bus_name;
        }
    }

    public void setStops(int stop) {
        stops = stop;
    }

    public int getStops() {
        return stops;
    }

    public void setPolyline(List<LatLng> poly) {
        polyline = poly;
    }

    public List<LatLng> getPolyline() {
        return polyline;
    }

    public void setTravelMode(int mode) {
        travel_mode = mode;
    }

    public int getTravelMode() {
        return travel_mode;
    }
}
