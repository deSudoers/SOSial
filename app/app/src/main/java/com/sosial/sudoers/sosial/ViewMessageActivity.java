package com.sosial.sudoers.sosial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ViewMessageActivity extends AppCompatActivity {

    SharedPreferences inboxsp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inboxsp = getSharedPreferences("currentMessage",MODE_PRIVATE);
        String title = inboxsp.getString("currentName","");
        setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
