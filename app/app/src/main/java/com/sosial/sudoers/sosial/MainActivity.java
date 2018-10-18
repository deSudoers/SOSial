package com.sosial.sudoers.sosial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView mProfileInfo;
    private TextView mName;
    private TextView mEmail;

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent m = new Intent(MainActivity.this, MessagingActivity.class);
                startActivity(m);
            }
        });

        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.getString("token", "");

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

        String url = "http://192.168.43.168:5000/profile";
        GetProfile gp = new GetProfile();
        try {
            updateProfile(gp.execute(url).get());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Button openTrigger = (Button) findViewById(R.id.opentrigger);
        openTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent t = new Intent(MainActivity.this, TriggerActivity.class);
                startActivity(t);
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
            sp.edit().putString("myid", json.getString("user_id")).apply();
            sp.edit().putString("email", json.getString("family_email")).apply();
            sp.edit().putString("name", json.getString("family_name")).apply();
            sp.edit().putString("userid", json.getString("family_id")).apply();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            goToMemberActivity();
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            goToLoginActivity();
            sp.edit().putBoolean("logged", false).apply();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goToLoginActivity(){
        sp.edit().putBoolean("logged", false);
        sp.edit().putString("token", "");
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void goToMemberActivity(){
        Intent i = new Intent(this, MemberActivity.class);
        startActivity(i);
    }
}
