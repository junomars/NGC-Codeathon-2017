package com.northropgrumman.wayz;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.northropgrumman.wayz.model.Report;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {
    private HeatmapTileProvider mProvider;
    private GoogleMap mMap;
    private ClusterManager<Report> mClusterManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        this.getMapAsync(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        this.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mClusterManager = new ClusterManager<>(getContext(), mMap);
        mMap.setOnCameraIdleListener(mClusterManager);

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Problem reading list of markers.", Toast.LENGTH_LONG).show();
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
