package com.sosial.sudoers.sosial;

import android.app.admin.SystemUpdateInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Inbox extends AppCompatActivity {

    private SharedPreferences inboxsp;
    ListView msgList;
    ArrayList<String> testing = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        inboxsp = getSharedPreferences("currentMessage",MODE_PRIVATE);
        msgList = (ListView) findViewById(R.id.messageList);

        renderList();
        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                inboxsp.edit().putString("currentName",testing.get(position).toString()).apply();
                viewMessage();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                goToMessagingActivity();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void renderList() {

        testing.add(0,"Sarath");
        testing.add(0,"Shivesh");
        testing.add(0,"Shrijit");
        ListAdapter msgAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,testing);
        msgList.setAdapter(msgAdapter);
    }

    private void viewMessage() {
        Intent i = new Intent(this,ViewMessageActivity.class);
        startActivity(i);
    }



    private void goToMessagingActivity() {
        Intent i = new Intent(this,MessagingActivity.class);
        startActivity(i);
    }

}
