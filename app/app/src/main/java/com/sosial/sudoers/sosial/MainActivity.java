package com.sosial.sudoers.sosial;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
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

    private TextView mName;
    private TextView mEmail;

    private SharedPreferences sp, spmessage, splocation;

    private LocationManager locationManager;
    private LocationListener locationListener;


    Intent mServiceIntent = null;
    private TriggerChecker mTriggerChecker = null;

    public Context getCtx(){
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(mTriggerChecker == null)
            mTriggerChecker = new TriggerChecker();
        if(mServiceIntent == null)
            mServiceIntent = new Intent(getCtx(), mTriggerChecker.getClass());
        if (!isMyServiceRunning(mTriggerChecker.getClass())) {
            startService(mServiceIntent);
        }

        sp = getSharedPreferences("login", MODE_PRIVATE);
        spmessage = getSharedPreferences("allmessages", MODE_PRIVATE);
        splocation = getSharedPreferences("location", MODE_PRIVATE);
        sp.getString("token", "");
        sp.getString("token2", "");

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
        String cookie1 = sp.getString("token2", "");
        String cookie2 = sp.getString("token", "");
        GetProfile gp = new GetProfile();
        try {
            String response = gp.execute(url, cookie1, cookie2).get();
            if(response.equals(""))
                updateProfile();
            else
                updateProfile(response);
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
                updateLocation();
                Snackbar.make(view, initiateTrigger(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if (!isMyServiceRunning(mTriggerChecker.getClass()))
                    startService(mServiceIntent);
                sp.edit().putBoolean("trigger", true).apply();
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
                updateLocation();
                if(shareLocation())
                    Snackbar.make(v, "Location Sent.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                else
                    Snackbar.make(v, "Failed to send location. Please try again.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        if(splocation.getString("latitude", "").equals("") || splocation.getString("longitude", "").equals("")) {
            updateLocation();
            Snackbar.make(getWindow().getDecorView().getRootView(), "Please Update Your Location.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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

        if (id == R.id.end_process) {
            stopService(mServiceIntent);
            sp.edit().putBoolean("trigger", false).apply();
            TriggerChecker.end();
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
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Snackbar.make(getWindow().getDecorView().getRootView(), "Please connect to Internet to Log Out.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        stopService(mServiceIntent);
        sp.edit().putBoolean("trigger", false).apply();
        TriggerChecker.end();
        sp.edit().clear().apply();
        spmessage.edit().clear().apply();
        splocation.edit().clear().apply();
        sendRequest sr = new sendRequest();
        String cookie1 = sp.getString("token2","");
        String cookie2 = sp.getString("token","");
        try {
            sr.execute("https://sosial.azurewebsites.net/logout", "", cookie1, cookie2).get();
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

    //Profile

    public void updateProfile(){
        mName.setText(sp.getString("myname", "Unexpectedly logged out."));
        mEmail.setText(sp.getString("myemail", "Please Log in Again."));
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
    }

    static class GetProfile extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", params[1]);
                httpURLConnection.addRequestProperty("cookie", params[2]);
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
        }
    }


    //Location
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
        String latitude = splocation.getString("latitude", "");
        String longitude = splocation.getString("longitude", "");
        if (latitude.equals("") || latitude.equals(""))
            return false;
        String myfamilyid[] = sp.getString("userid", "").split(",");
        String temp = sp.getString("userid", "");

        String myid = sp.getInt("myid", 0) + "";
        String sendername = sp.getString("myname", "");
        String message =  "This is my location, " + latitude + "," + longitude;
        for (String i: myfamilyid){
            String key = myid+i+message.substring(message.length()-10, message.length())+message.length();
            addMessagetoDatabase(myid, sendername, i, message, key);
        }
        return true;
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
            String cookie1 = sp.getString("token2","");
            String cookie2 = sp.getString("token","");
            response  = sl.execute(url, postData.toString(), cookie1, cookie2).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    static class sendRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", params[2]);
                httpURLConnection.addRequestProperty("cookie", params[3]);
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
        }
    }

    //Messages
    public void addMessagetoDatabase(String myid, String sendername, String receiver, String msg, String key){
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
            mssg.put("name", sendername);
            mssg.put("message", msg);
            mssg.put("key", key);
            spmessage.edit().putString("allmymessages"+count,mssg.toString()).apply();
            spmessage.edit().putInt("allmymessagescount", ++count).apply();
            if(receiver.equals(sp.getInt("myid", 0)+""))
                new NotificationSender(this, "", "", sendername, msg);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

   //Trigger
    private String initiateTrigger() {
        String response = sendJson();
        JSONObject json = null;
        try {
            json = new JSONObject(response);
            response = json.getString("message");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    private String sendJson() {
        String url = "https://sosial.azurewebsites.net/trigger";
        String response = "An Error Occurred. Please Try Again.";
        JSONObject postData = new JSONObject();
        splocation = getSharedPreferences("location",MODE_PRIVATE);

        try{
            String lat = splocation.getString("latitude", "");
            String lon = splocation.getString("longitude", "");
            if(!lat.equals("") && !lon.equals("")){
                postData.put("latitude", lat);
                postData.put("longitude", lon);
                SendRequest sdd =  new SendRequest();
                String cookie1 = sp.getString("token2","");
                String cookie2 = sp.getString("token","");
                response  = sdd.execute(url, postData.toString(), cookie1, cookie2).get();
                if(response.equals("{\"message\": \"Trigger added.\"}"))
                    response = "Disaster Event Created. Please Update Location and allow wifi communication.";
                Log.e("triggercheck", response);
            }
            else{
                response = "Please Update location before SOS.";
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    static class SendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", params[2]);
                httpURLConnection.addRequestProperty("cookie", params[3]);
                httpURLConnection.setRequestMethod("PUT");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    data += line;
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
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
