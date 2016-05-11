package com.example.gary.myapplication;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JSON Parser class for parsing direction from google.
 * Created by gary on 08/02/2016.
 */
public class JSONParser {

    public List<Journey> parseJourneys(JSONObject jObj) {

        List<Journey> journeys = new ArrayList<>();
        ArrayList<String> encodedPolylines = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {
            if (jObj.get("status").equals("ZERO_RESULTS")) {
                return null;
            }
            jRoutes = jObj.getJSONArray("routes");

            for (int i = 0; i < jRoutes.length(); i++) {

                JSONObject route = (JSONObject) jRoutes.get(i);
                String overviewPolyline = (String) ((JSONObject) route.get("overview_polyline")).get("points");
                //check if route exist already, prevent same route but different timing
                if (!encodedPolylines.contains(overviewPolyline)) {
                    Journey journey = new Journey();
                    ArrayList<String> warning = new ArrayList<>();
                    encodedPolylines.add(overviewPolyline);
                    JSONArray jWarnings = route.getJSONArray("warnings");
                    for (int ii = 0; ii < jWarnings.length(); ii++) {
                        warning.add((String) jWarnings.get(ii));
                    }
                    journey.setWarning(warning);
                    JSONObject bounds = (JSONObject) route.get("bounds");
                    JSONObject ne_bound = (JSONObject) bounds.get("northeast");
                    JSONObject sw_bound = (JSONObject) bounds.get("southwest");
                    journey.setNEBound(new LatLng((double) ne_bound.get("lat"), (double) ne_bound.get("lng")));
                    journey.setSWBound(new LatLng((double) sw_bound.get("lat"), (double) sw_bound.get("lng")));

                    jLegs = route.getJSONArray("legs");
                    for (int j = 0; j < jLegs.length(); j++) {

                        JSONObject leg = (JSONObject) jLegs.get(j);

                        journey.setDuration((String) ((JSONObject) leg.get("duration")).get("text"), (int) ((JSONObject) leg.get("duration")).get("value"));

                        jSteps = leg.getJSONArray("steps");

                        for (int k = 0; k < jSteps.length(); k++) {

                            Step step = new Step();
                            JSONObject jStep = (JSONObject) jSteps.get(k);

                            step.setInstruction((String) jStep.get("html_instructions"));
                            if (jStep.has("transit_details")) {
                                JSONObject jTransit = (JSONObject) jStep.get("transit_details");
                                step.setArrivalStop((String) ((JSONObject) jTransit.get("arrival_stop")).get("name"));
                                step.setDepartureTime((String) ((JSONObject) jTransit.get("departure_time")).get("text"));
                                JSONObject jLine = (JSONObject) jTransit.get("line");
                                if (jLine.has("name")) {
                                    step.setBusName((String) ((JSONObject) jTransit.get("line")).get("name"));
                                }
                                if (jLine.has("short_name")) {
                                    step.setBusShort((String) ((JSONObject) jTransit.get("line")).get("short_name"));
                                }
                                step.setStops((int) jTransit.get("num_stops"));
                                step.setTravelMode(1);
                            }
                            step.setPolyline(PolyUtil.decode((String) ((JSONObject) jStep.get("polyline")).get("points")));
                            if (jStep.get("travel_mode").equals("WALKING")) {
                                step.setDistance((String) ((JSONObject) jStep.get("distance")).get("text"));
                            }
                            journey.addStep(step);
                        }
                    }
                    journeys.add(journey);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return journeys;
    }

    public ArrayList<RoadEvent> parseRoadEvent(JSONObject jObj) {

        ArrayList<RoadEvent> events = new ArrayList<>();

        try {
            JSONArray jData = jObj.getJSONArray("data");
            for (int i = 0; i < jData.length(); i++) {
                JSONObject data = (JSONObject) jData.get(i);
                if (data.has("message")) {
                    String[] structure = ((String) data.get("message")).split(":");
                    if (structure[0].equals("Report")) {

                        RoadEvent event = new RoadEvent();
                        int like_count = (int) ((JSONObject) ((JSONObject) data.get("likes")).get("summary")).get("total_count");
                        event.setLikes(like_count);

                        if (structure[1].equals(" LatLng")) {
                            String message = structure[2];
                            String latitude = message.substring(0, message.indexOf(","));
                            String longitude = message.substring(message.indexOf(",") + 1, message.indexOf(".", message.indexOf(",") + 7));
                            String reportMsg = message.substring(message.indexOf(".", message.indexOf(",") + 7) + 1, message.length());
                            String[] msg = reportMsg.split("\\.", 2);
                            Location loc = new Location("");
                            loc.setLatitude(Double.valueOf(latitude));
                            loc.setLongitude(Double.valueOf(longitude));
                            event.setLocation(loc);
                            event.setAddress(msg[0]);
                            event.setMessage(msg[1]);
                            events.add(event);
                        } else {
                            String[] message = structure[1].split("\\.", 2);
                            Log.d("MyApplication", message[0]);
                            event.setAddress(message[0]);
                            event.setMessage(message[1]);
                            events.add(event);
                        }

                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }

    public ArrayList<LatLng> parseSnappedPoints(JSONObject jObj) {

        ArrayList<LatLng> list = new ArrayList<>();

        try {
            JSONArray jPoints = jObj.getJSONArray("snappedPoints");
            for (int i = 0; i < jPoints.length(); i++) {
                JSONObject point = (JSONObject) jPoints.get(i);
                JSONObject location = (JSONObject) point.get("location");
                LatLng latlng = new LatLng((double) location.get("latitude"), (double) location.get("longitude"));
                list.add(latlng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
