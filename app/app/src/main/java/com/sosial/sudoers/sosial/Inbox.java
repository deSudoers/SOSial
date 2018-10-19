package com.sosial.sudoers.sosial;

import android.app.admin.SystemUpdateInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Inbox extends AppCompatActivity {

    private SharedPreferences inboxsp;
    private SharedPreferences currentUser;
    private JSONObject messageBundle = new JSONObject();
    private String msg = new String();
    ListView msgList;
    ArrayList<String> testing = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        inboxsp = getSharedPreferences("currentMessage",MODE_PRIVATE);
        currentUser = getSharedPreferences("login",MODE_PRIVATE);
        msgList = (ListView) findViewById(R.id.messageList);

        renderList();

        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                inboxsp.edit().putString("name",testing.get(position).toString()).apply();
                getMessageText(position);
                inboxsp.edit().putString("msgtext",msg).apply();
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

    private void renderList(){

        testing.add(0,"Shrijit");
        testing.add(0,"Shivesh");
        testing.add(0,"Sarath");

        JSONObject item0= new JSONObject();
        JSONObject item1= new JSONObject();
        JSONObject item2= new JSONObject();

        try {
            item0.put("name", "Sarath");
            item0.put("msg", "Hello this is Sarath!");
            messageBundle.put("0", item0);

            item1.put("name", "Shivesh");
            item1.put("msg", "Hello this is Shivesh!");
            messageBundle.put("1", item1);

            item2.put("name", "Shrijit");
            item2.put("msg", "Hello this is Shrijit!");
            messageBundle.put("2", item2);

            ListAdapter msgAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testing);
            msgList.setAdapter(msgAdapter);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void getMessageText(int position) {
        JSONObject currentMessage = new JSONObject();
        try {
            currentMessage = messageBundle.getJSONObject(String.valueOf(position));
            msg = currentMessage.getString("msg");
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
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
