package com.northropgrumman.wayz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.northropgrumman.wayz.model.Report;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private HeatmapTileProvider mProvider;
    private GoogleMap mMap;
    private ClusterManager<Report> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    mMap.setMyLocationEnabled(true);
//
                    LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    String provider = manager.getBestProvider(new Criteria(), true);

                    Location currentLocation = manager.getLastKnownLocation(provider);

//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(52.657534, -8.629455)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.81753, 144.9671), 20));
                } else {
                    this.finish();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.sample_police_stations);
        List<Report> items = new PersonReportReader().read(inputStream);
        mClusterManager.addItems(items);
    }

//
//    private void addHeatMap() {
//        List<LatLng> list = null;
//
//        // Get the data: latitude/longitude positions of police stations.
//        try {
//            list = readItems(R.raw.sample_police_stations);
////            list.addAll(readItems(R.raw.limerick_poi));
//        } catch (JSONException e) {
//            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
//        }
//
//        // Create a heat map tile provider, passing it the latlngs of the police stations.
//        mProvider = new HeatmapTileProvider.Builder()
//                .data(list)
//                .build();
//        // Add a tile overlay to the map, using the heat map tile provider.
//        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
//    }
//
//    private ArrayList<LatLng> readItems(int resource) throws JSONException {
//        ArrayList<LatLng> list = new ArrayList<>();
//        InputStream inputStream = getResources().openRawResource(resource);
//        String json = new Scanner(inputStream).useDelimiter("\\A").next();
//        JSONArray array = new JSONArray(json);
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.getJSONObject(i);
//            double lat = object.getDouble("lat");
//            double lng = object.getDouble("lng");
//            list.add(new LatLng(lat, lng));
//        }
//        return list;
//    }
}
