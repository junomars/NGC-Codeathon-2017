package com.northropgrumman.wayz;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.northropgrumman.wayz.model.Report;

public class MainActivity extends FragmentActivity implements UserInputFragment.OnUserSubmitInteraction {
    MapsFragment mapsFragment = new MapsFragment();
    UserInputFragment inputFragment = new UserInputFragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_survivors:
                    if (!inputFragment.isVisible()) {
                        inputFragment.setArguments(getIntent().getExtras());
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, inputFragment);
                        transaction.commit();
                    }
                    return true;
                case R.id.navigation_maps:
                    if (!mapsFragment.isVisible()) {
                        mapsFragment.setArguments(getIntent().getExtras());
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, mapsFragment);
                        transaction.commit();
                    }
                    return true;
                case R.id.navigation_zombies:
                    if (!inputFragment.isVisible()) {
                        inputFragment.setArguments(getIntent().getExtras());
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, inputFragment);
                        transaction.commit();
                    }

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setSelectedItemId(R.id.navigation_maps);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    this.finish();
                }
            }
        }
    }

    @Override
    public void onSubmit() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String provider = manager.getBestProvider(new Criteria(), true);

        @SuppressLint("MissingPermission") Location currentLocation = manager.getLastKnownLocation(provider);
        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        EditText zombieCount = findViewById(R.id.zombieCountInput);
        EditText locationDescription = findViewById(R.id.locationDescriptionInput);

        //Needs input validation
        //Get number of survivors
        String numSurvivors = zombieCount.getText().toString();
        int survivorCount = Integer.parseInt(numSurvivors);


        //Get location description
        String locationInput = locationDescription.getText().toString();

        Report newPerson = new Report(location, locationInput, null, survivorCount);

        myRef.setValue(newPerson);
    }
}
