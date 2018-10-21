package com.sosial.sudoers.sosial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class SharedLocations extends AppCompatActivity implements OnMapReadyCallback {

    private SharedPreferences location;
    private SharedPreferences currentUser;
    private Marker locationMarker;
    private GoogleMap mMap;
    private LatLng memberLocation;
    private SharedPreferences allmessages;
    private int numOfMsgs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_locations);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Shared Locations");

        location = getSharedPreferences("location",MODE_PRIVATE);
        currentUser = getSharedPreferences("login",MODE_PRIVATE);
        allmessages = getSharedPreferences("allmessages",MODE_PRIVATE);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sharedLocationsMap);
        mapFragment.getMapAsync(this);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Your Location", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //13.366999, 74.706136    //13.322447, 74.861606

        numOfMsgs = allmessages.getInt("allmymessagescount",0);

        String msgJsonStr;
        String msgJsonKey;
        JSONObject msgJson;
        String receiverId;
        String senderName;
        int i;
        for (i = 0; i < 1; i++)
        {
            try
            {
                msgJsonKey = "allmymessages" + i;
                msgJsonStr = allmessages.getString(msgJsonKey,"");
                msgJson = new JSONObject(msgJsonStr);

                receiverId = msgJson.getString("receiver");setTitle("hello");

                if(receiverId.equals(String.valueOf(currentUser.getInt("myid",0))))
                {
                    senderName = msgJson.getString("name");
                    memberLocation = new LatLng(
                            Double.parseDouble(location.getString("latitude", "0"))
                        ,Double.parseDouble(location.getString("longitude", "0")));
                    locationMarker = mMap.addMarker(new MarkerOptions().position(memberLocation));
                    locationMarker.setTitle(senderName);
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }


        memberLocation = new LatLng(Double.parseDouble(location.getString("latitude","0"))
                , Double.parseDouble(location.getString("longitude","0")));
        locationMarker = mMap.addMarker(new MarkerOptions().position(memberLocation));
        locationMarker.setTitle("My Location");
        locationMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(memberLocation, 10));
    }
}