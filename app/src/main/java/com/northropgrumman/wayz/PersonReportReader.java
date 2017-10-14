package com.northropgrumman.wayz;

import com.google.android.gms.maps.model.LatLng;
import com.northropgrumman.wayz.model.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class PersonReportReader {
    List<Report> read(InputStream inputStream) throws JSONException {
        List<Report> items = new ArrayList<>();
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            String title = null;
            String snippet = null;
            int weight = 1;
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            if (!object.isNull("title")) {
                title = object.getString("title");
            }
            if (!object.isNull("snippet")) {
                snippet = object.getString("snippet");
            }
            if (!object.isNull("weight")) {
                weight = object.getInt("weight");
            }
            items.add(new Report(new LatLng(lat, lng), title, snippet, weight));
        }
        return items;
    }

}
