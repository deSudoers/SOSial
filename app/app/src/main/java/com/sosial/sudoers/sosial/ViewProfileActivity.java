package com.sosial.sudoers.sosial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ViewProfileActivity extends AppCompatActivity {

    SharedPreferences myProfile;
    TextView email,number,name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myProfile = getSharedPreferences("login",MODE_PRIVATE);
        displayDetails();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
