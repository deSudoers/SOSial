package com.sosial.sudoers.sosial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    SharedPreferences myProfile;
    SharedPreferences location;
    TextView email,number,name;
    Button viewFamily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myProfile = getSharedPreferences("login",MODE_PRIVATE);
        displayDetails();

        viewFamily = (Button) findViewById(R.id.viewFamily);
        viewFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToFamilyActivity();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mymap);
        mapFragment.getMapAsync(this);




        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;
        location = getSharedPreferences("location",MODE_PRIVATE);
        Marker locationMarker;

        LatLng myLocation = new LatLng(Double.parseDouble(location.getString("latitude","0"))
                , Double.parseDouble(location.getString("longitude","0")));
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation));
        if(myLocation.longitude == 0 && myLocation.latitude == 0)
        {
            locationMarker.setTitle("Location Unknown");
        }
        else {
            locationMarker.setTitle("My Location");
        }

        locationMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
    }


    private void goToFamilyActivity() {
        Intent i = new Intent(this,MemberActivity.class);
        startActivity(i);
    }

    private void displayDetails() {

        name = (TextView) findViewById(R.id.profileName);
        name.setText(myProfile.getString("myname",""));
        email = (TextView) findViewById(R.id.profileEmail);
        email.setText(myProfile.getString("myemail",""));
        number = (TextView) findViewById(R.id.profileNumber);
        number.setText(myProfile.getString("mynumber", ""));
    }

}
