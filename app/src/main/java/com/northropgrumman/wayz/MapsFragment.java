package com.northropgrumman.wayz;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.northropgrumman.wayz.model.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {
    private static GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private ClusterManager<Report> mClusterManager; // people
    private TileOverlay mOverlay;
    private List<Report> list = null;
    private Marker start;
    private Marker destination;

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

    public void updateMap(String mapType) {
        switch (mapType) {
            case "people":
                try {
                    readItems();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mOverlay.remove();
                mMap.setOnMapClickListener(null);
                break;
            case "zombies":
                mClusterManager.clearItems();
                mMap.setOnMapClickListener(null);
                addHeatMap();
                break;
            case "route":
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (destination != null) {
                            start.remove();
                            start = mMap.addMarker(new MarkerOptions().position(latLng).title("Start"));
                            destination.remove();
                            destination = null;
                            return;
                        } else {
                            if (start == null) {
                                start = mMap.addMarker(new MarkerOptions().position(latLng).title("Start"));
                                return;
                            }
                            destination = mMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));
                        }

                        Log.i("atag", "calculating");
                        ArrayList<LatLng> points = calculatePath(start.getPosition(), destination.getPosition(), 5, list);

                        for (LatLng point : points) {
                            mMap.addMarker(new MarkerOptions().position(point));
                        }

                        GoogleDirection.withServerKey("AIzaSyCm36cPAlaMP0SlJo0fbvn3eejyuvMCrMw")
                                .from(start.getPosition())
                                .to(destination.getPosition())
                                .transportMode(TransportMode.WALKING)
                                .avoid(AvoidType.HIGHWAYS)
                                .avoid(AvoidType.FERRIES)
                                .avoid(AvoidType.TOLLS)
                                .alternativeRoute(true)
                                .waypoints(points)
                                .execute(new DirectionCallback() {
                                    @Override
                                    public void onDirectionSuccess(Direction direction, String rawBody) {
                                        for (Leg leg : direction.getRouteList().get(direction.getRouteList().size() - 1).getLegList()) {
                                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getContext(), directionPositionList, 5, Color.RED);
                                            mMap.addPolyline(polylineOptions);
                                        }
                                    }

                                    @Override
                                    public void onDirectionFailure(Throwable t) {
                                        Toast.makeText(getContext(), "Failed to make.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                break;
            default:

        }
    }

    private ArrayList<LatLng> calculatePath(LatLng start, LatLng destination, int coarseness, List<Report> zombieThreats) {
        float[] distance = new float[2];
        Location.distanceBetween(start.latitude, start.longitude, destination.latitude, destination.longitude, distance);
        int stepSize = (int) (distance[0] / coarseness);

        double distLatitude = (destination.latitude - start.latitude) / coarseness;
        double distLongitude = (destination.longitude - start.longitude) / coarseness;

        Toast.makeText(getContext(), String.format("y=%f feet%nx=%f feet", distLatitude, distLongitude), Toast.LENGTH_LONG).show();
        Log.i("atag", "setting up data");

        // Get points from local data
        LatLng[][] verticesMedium = new LatLng[coarseness][coarseness];
        HashSet<LatLng> vertices = new HashSet<>();
        HashMap<LatLng, HashSet<Edge>> neighbors = new HashMap<>();
        HashMap<LatLng, Integer> dist = new HashMap<>();
        HashMap<LatLng, LatLng> prev = new HashMap<>();

        for (int row = 0; row < coarseness; row++) {
            for (int col = 0; col < coarseness; col++) {
                verticesMedium[row][col] = new LatLng(start.latitude + (distLatitude * col), start.longitude + (distLongitude * row));
            }
        }

        for (int row = 0; row < coarseness; row++) {
            for (int col = 0; col < coarseness; col++) {
                LatLng vertex = verticesMedium[row][col];
                vertices.add(vertex);
                neighbors.put(vertex, new HashSet<Edge>());
                if (row - 1 >= 0 && col - 1 >= 0)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row - 1][col - 1], 1));
                if (row - 1 >= 0)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row - 1][col], 1));
                if (row - 1 >= 0 && col + 1 < coarseness)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row - 1][col + 1], 1));
                if (col - 1 >= 0)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row][col - 1], 1));
                if (col + 1 < coarseness)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row][col + 1], 1));
                if (row + 1 < coarseness && col - 1 >= 0)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row + 1][col - 1], 1));
                if (row + 1 < coarseness)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row + 1][col], 1));
                if (row + 1 < coarseness && col + 1 < coarseness)
                    neighbors.get(vertex).add(new Edge(verticesMedium[row + 1][col + 1], 1));
                dist.put(vertex, Integer.MAX_VALUE - 100000);

                Log.i("atag", "added: " + vertex.toString());
                prev.put(vertex, null);
            }
        }


        Log.i("atag", "adding weights");

        // Add weights to nodes
        for (Report zombieThreat : zombieThreats) {
            LatLng pos = zombieThreat.getPosition();
            int weight = zombieThreat.getWeight();
            for (LatLng vertex : vertices) {
                Location.distanceBetween(vertex.latitude, vertex.longitude, pos.latitude, pos.longitude, distance);
                if (distance[0] < weight * stepSize) {
                    for (Edge edge : neighbors.get(vertex)) {
                        edge.weight += distance[0] * stepSize / weight;
                    }
                }
            }
        }


        Log.i("atag", "running dijkstras");

        // Algorithm
        dist.put(start, 0);

        LatLng vertex;
        while (!vertices.isEmpty()) {
            vertex = vertices.iterator().next();

            for (LatLng latLng : vertices) {
                if (dist.get(vertex) < dist.get(latLng)) {
                    vertex = latLng;
                }
            }

//            for (int row = 0; row < coarseness; row++) {
//                for (int col = 0; col < coarseness; col++) {
//                    if (vertices.contains(verticesMedium[row][col]) && dist.get(verticesMedium[row][col]) < dist.get(vertex)) {
//                        vertex = verticesMedium[row][col];
//                    }
//                }
//            }


            Log.i("atag", "got: " + vertex);
            Log.i("atag", "is equal: " + vertices.contains(vertex));
            vertices.remove(vertex);

            Log.i("atag", "grid points left: " + vertices.size());

            for (Edge edge : neighbors.get(vertex)) {
                if (!vertices.contains(edge.latLng)) {
                    continue;
                }

                int alt = dist.get(vertex) + edge.weight;
                if (alt < dist.get(edge.latLng)) {
                    dist.put(edge.latLng, alt);
                    prev.put(edge.latLng, vertex);
                }
            }
        }

        ArrayList<LatLng> path = new ArrayList<>();
        LatLng dest = verticesMedium[coarseness - 1][coarseness - 1];

        while (prev.get(dest) != null) {
            path.add(0, dest);
            dest = prev.get(dest);
        }
        path.add(0, dest);

        return path;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getContext(), "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8136, 144.9631), 10));

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

    private void addHeatMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8136, 144.9631), 10));

        // Get the data: latitude/longitude positions of police stations.
        try {
//            list = readItems(R.raw.sample_police_stations);
            list = readItems(R.raw.sample_police_stations);
            Log.e("in maps", "read it all");
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Problem reading list of locations.", Toast.LENGTH_LONG).show();
            Log.e("didn't work", e.getMessage());
        }

        List<WeightedLatLng> latList = new ArrayList<>();
        for (Report report : list) {
            latList.add(new WeightedLatLng(report.getPosition(), report.getWeight()));
        }

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .weightedData(latList)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private ArrayList<Report> readItems(int resource) throws JSONException {
        ArrayList<Report> list = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            int weight = object.getInt("weight");
            list.add(new Report(new LatLng(lat, lng), weight));
        }
        return list;
    }

    class Edge {
        public LatLng latLng;
        public int weight;

        public Edge(LatLng latLng, int i) {
            this.latLng = latLng;
            this.weight = i;
        }
    }
}
