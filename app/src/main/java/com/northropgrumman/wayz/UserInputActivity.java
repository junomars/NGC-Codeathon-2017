package com.northropgrumman.wayz;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.northropgrumman.wayz.model.Report;

public class UserInputActivity extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_input,container,false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button submitButton = findViewById(R.id.submitButton);



        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

                String provider = manager.getBestProvider(new Criteria(), true);

                @SuppressLint("MissingPermission") Location currentLocation = manager.getLastKnownLocation(provider);
                LatLng location = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getInstance().getReference();

                EditText zombieCount = findViewById(R.id.zombieCountInput);
                EditText locationDescription = findViewById(R.id.locationDescriptionInput);

                //Needs input validation
                //Get number of survivors
                String numSurvivors = zombieCount.getText().toString();
                int survivorCount = Integer.parseInt(numSurvivors);


                //Get location description
                String locationInput = locationDescription.getText().toString();



                Report newPerson = new Report(location,locationInput,null, survivorCount);

                myRef.setValue(newPerson);

            }
        });
    }
}
