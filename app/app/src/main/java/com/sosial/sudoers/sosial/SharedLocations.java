package com.sosial.sudoers.sosial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SharedLocations extends AppCompatActivity implements OnMapReadyCallback {

    private SharedPreferences location;
    private SharedPreferences currentUser;
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
        numOfMsgs = allmessages.getInt("allmymessagescount",0);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker locationMarker[] = new Marker[numOfMsgs+1];
        String msgJsonStr;
        String msgJsonKey;
        JSONObject msgJson;
        String receiverId;
        String senderName;
        int i=0;
//        msgJsonStr[0]="{\"sender\":\"2345\",\"receiver\":\"40\",\"name\":\"Shivesh\",\"message\":\"This is my location, 13.3669991111111111,74.706136\",\"key\":\"ijnyhbrf\"}";
//        msgJsonStr[1]="{\"sender\":\"9572\",\"receiver\":\"40\",\"name\":\"Shrijit\",\"message\":\"This is my location, 13.427133, 74.856039\",\"key\":\"9hdb823t\"}";
//        msgJsonStr[2]="{\"sender\":\"2149\",\"receiver\":\"40\",\"name\":\"Sarath\",\"message\":\"This is my location, 13.225963, 74.737544\",\"key\":\"9hdb823t\"}";
        for (i = 0; i < numOfMsgs; i++)
        {
            try
            {
                msgJsonKey = "allmymessages" + i;
                msgJsonStr = allmessages.getString(msgJsonKey,"");

                msgJson = new JSONObject(msgJsonStr);

                receiverId = msgJson.getString("receiver");

                if(receiverId.equals(String.valueOf(currentUser.getInt("myid",0))))
                {
                    senderName = msgJson.getString("name");
                    String message = msgJson.getString("message");
                    if(message.startsWith("This is my location,")) {
                        String latlng = message.substring(21);
                        String location[] = latlng.split(",");
                        memberLocation = new LatLng(
                                Double.parseDouble(location[0])
                                , Double.parseDouble(location[1]));
                        locationMarker[i] = mMap.addMarker(new MarkerOptions().position(memberLocation));
                        locationMarker[i].setTitle(senderName);
                    }
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }


        memberLocation = new LatLng(Double.parseDouble(location.getString("latitude","0"))
                , Double.parseDouble(location.getString("longitude","0")));
        locationMarker[i] = mMap.addMarker(new MarkerOptions().position(memberLocation));
        locationMarker[i].setTitle("My Location");
        locationMarker[i].showInfoWindow();
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(memberLocation, 10));
        for (Marker marker : locationMarker) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);

    }
}
