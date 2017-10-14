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
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.northropgrumman.wayz.model.Report;

public class MainActivity extends FragmentActivity implements UserInputFragment.OnUserSubmitInteraction, ZombieUserInput.OnZombieUserSubmitInteraction {
    MapsFragment mapsFragment = new MapsFragment();
    UserInputFragment inputFragment = new UserInputFragment();
    ZombieUserInput zombieFragment = new ZombieUserInput();
    RadioGroup group;

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener
            = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.map_people:
                    // Get list of data
                    // Update map stuff
                    break;
                case R.id.map_zombies:
                    Toast.makeText(getApplicationContext(), "Pressed zombies", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "" + radioGroup.getCheckedRadioButtonId(), Toast.LENGTH_SHORT).show();
            }
        }
    };

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
                    group.setVisibility(View.INVISIBLE);
                    group.setEnabled(false);
                    return true;
                case R.id.navigation_maps:
                    if (!mapsFragment.isVisible()) {
                        mapsFragment.setArguments(getIntent().getExtras());
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, mapsFragment);
                        transaction.commit();
                    }
                    group.setVisibility(View.VISIBLE);
                    group.setEnabled(true);
                    return true;
                case R.id.navigation_zombies:
                    if (!zombieFragment.isVisible()) {
                        zombieFragment.setArguments(getIntent().getExtras());
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, zombieFragment);
                        transaction.commit();
                    }
                    group.setVisibility(View.INVISIBLE);
                    group.setEnabled(false);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        group = findViewById(R.id.button_display_group);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        group.setOnCheckedChangeListener(mOnCheckedChangeListener);

        navigation.setSelectedItemId(R.id.navigation_maps);
        ((RadioButton) findViewById(R.id.map_people)).setChecked(true);
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
    public void onUserSubmit() {
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

    @Override
    public void onZombieSubmit() {

    }
}
