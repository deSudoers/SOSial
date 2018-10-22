package com.sosial.sudoers.sosial;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mProfileInfo;
    private TextView mName;
    private TextView mEmail;

    private SharedPreferences sp, spmessage, splocation;

    private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
//            }
//        });

        sp = getSharedPreferences("login", MODE_PRIVATE);
        spmessage = getSharedPreferences("allmessages", MODE_PRIVATE);
        splocation = getSharedPreferences("location", MODE_PRIVATE);
        sp.getString("token", "");
        sp.getString("token2", "");

        mProfileInfo = (TextView) findViewById(R.id.profile);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        mName = (TextView) headerView.findViewById(R.id.name_id);
        mEmail = (TextView) headerView.findViewById(R.id.textView);

        String url = "https://sosial.azurewebsites.net/profile";
        GetProfile gp = new GetProfile();
        try {
            updateProfile(gp.execute(url).get());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Button sendMessage = (Button) findViewById(R.id.messageButton);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent m = new Intent(MainActivity.this, MessagingActivity.class);
                startActivity(m);
            }
        });

        Button openTrigger = (Button) findViewById(R.id.opentrigger);
        openTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent t = new Intent(MainActivity.this, TriggerActivity.class);
                startActivity(t);
            }
        });


        final Button mUpdateLocation = (Button) findViewById(R.id.updateLocation);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                splocation.edit().putString("latitude", location.getLatitude() + "").apply();
                splocation.edit().putString("longitude", location.getLongitude() + "").apply();
                serverUpdateLocation();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        mUpdateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocation();

            }
        });

        Button mShareLocation = (Button) findViewById(R.id.shareLocation);
        mShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shareLocation())
                    Snackbar.make(v, "Location Sent.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else
                    Snackbar.make(v, "Failed to send location. Please try again.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
    }


    public void updateProfile(String profile){
        String name = "";
        String email = "";
        JSONObject json = null;
        try{
            json = new JSONObject(profile);
            name = json.getString("name");
            mName.setText(name);
            email = json.getString("email");
            mEmail.setText(email);
            sp.edit().putInt("myid", json.getInt("user_id")).apply();
            String temp = json.getString("family_email");
            sp.edit().putString("email", temp+",").apply();
            temp = json.getString("family_name");
            sp.edit().putString("name", temp+",").apply();
            sp.edit().putString("myname", json.getString("name")).apply();
            temp = json.getString("family_id");
            sp.edit().putString("userid", temp+",").apply();
            sp.edit().putString("myemail", json.getString("email")).apply();
            sp.edit().putString("mynumber", json.getString("mobile")).apply();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        mProfileInfo.setText(profile);
    }

    class GetProfile extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        locationManager.removeUpdates(locationListener);
        this.finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_popup) {
            Intent i = new Intent(this,PopupActivity.class);
            this.startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_family) {
            goToMemberActivity();
            // Handle the camera action
        } else if (id == R.id.nav_inbox) {
            goToInbox();

        } else if (id == R.id.nav_shared_locations) {
            viewSharedLocations();

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_profile) {
            viewMyProfile();

        } else if (id == R.id.nav_logout) {
            goToLoginActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void viewSharedLocations() {
        Intent i = new Intent(this,SharedLocations.class);
        startActivity(i);
    }

    public void goToLoginActivity(){
        sp.edit().clear().apply();
        spmessage.edit().clear().apply();
        splocation.edit().clear().apply();
        sendRequest sr = new sendRequest();
        try {
            sr.execute("https://sosial.azurewebsites.net/logout", "").get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void goToMemberActivity(){
        Intent i = new Intent(this, MemberActivity.class);
        startActivity(i);
    }

    private void viewMyProfile() {
        Intent i = new Intent(this, ViewProfileActivity.class);
        startActivity(i);
    }

    public void goToInbox(){
        Intent i = new Intent(this, Inbox.class);
        startActivity(i);
    }


    void updateLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                }, 11);
        }
        else
            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    }

    boolean shareLocation(){
        updateLocation();
        String latitude = splocation.getString("latitude", "");
        String longitude = splocation.getString("longitude", "");
        if (latitude.equals("") || latitude.equals(""))
            return false;
        String myfamilyid[] = sp.getString("userid", "").split(",");
        String temp = sp.getString("userid", "");

        String myid = sp.getInt("myid", 0) + "";
        String message = sp.getString("myname", "") + "#" + "This is my location, " + latitude + "," + longitude;

        for (String i: myfamilyid){
            String key = Password.hashPassword(myid + message + i);
            addMessagetoDatabase(myid, i, message, key);
        }
        return true;
    }

    public void addMessagetoDatabase(String myid, String receiver, String msg, String key){
        int count = spmessage.getInt("allmymessagescount",0);
        for(int i = 0; i < count; ++i){
            try {
                JSONObject json = new JSONObject(spmessage.getString("allmymessages" + i, ""));
                String k = json.getString("key");
                if(key.equals(k)){
                    return;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        JSONObject mssg = new JSONObject();
        try {
            mssg.put("sender", myid);
            mssg.put("receiver", receiver);
            String name = msg.split("#")[0];
            String msssg = msg.split("#")[1];
            if(receiver.equals(sp.getInt("myid", 0)+""))
                new NotificationSender(this, "", "", name, msssg);
            mssg.put("name", name);
            mssg.put("message", msssg);
            mssg.put("key", key);
            spmessage.edit().putString("allmymessages"+count,mssg.toString()).apply();
            spmessage.edit().putInt("allmymessagescount", ++count).apply();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case 11: if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                try {
                    locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }
        }
    }

    void serverUpdateLocation(){
        String lat= splocation.getString("latitude", "");
        String lon= splocation.getString("longitude", "");
        if(!lat.equals("") && !lon.equals("")){
            sendJson(lat+", "+lon);
        }
    }

    private String sendJson(String location){
        String url = "https://sosial.azurewebsites.net/location";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("location", location);
            sendRequest sl =  new sendRequest();
            response  = sl.execute(url, postData.toString()).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    class sendRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", sp.getString("token2",""));
                httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                int response = httpURLConnection.getResponseCode();
                if(response == httpURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null){
                        data += line;
                    }
                }
                else{
                    data = "An Error Occurred. Please Try Again.";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }
}
