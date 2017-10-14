package com.northropgrumman.wayz.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Report implements ClusterItem {
    private LatLng mPosition;
    private String mTitle;
    private String mDescription;
    private int weight;

    public Report()
    {
        mPosition = null;
        mTitle = null;
        mDescription = null;
        weight = 0;
    }



    public Report(LatLng mPosition, int weight) {
        this.mPosition = mPosition;
        mTitle = null;
        mDescription = null;
    }

    public Report(LatLng mPosition, String mTitle, String mDescription, int weight) {
        this.mPosition = mPosition;
        this.mTitle = mTitle;
        this.mDescription = mDescription;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public void setPosition(LatLng position) {
        this.mPosition = position;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    @Override
    public String getSnippet() {
        return mDescription;
    }

    public int getWeight() { return weight;}

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
