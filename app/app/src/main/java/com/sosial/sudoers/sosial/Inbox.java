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
import android.util.Log;
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
    private SharedPreferences allmessages;
    private JSONObject messageBundle = new JSONObject();
    private int numOfMsgs;
    ListView msgList;
    ArrayList<String> senderListDisplay = new ArrayList<String>();
    ArrayList<String> msgListDisplay = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        inboxsp = getSharedPreferences("currentMessage",MODE_PRIVATE);
        allmessages = getSharedPreferences("allmessages",MODE_PRIVATE);
        currentUser = getSharedPreferences("login",MODE_PRIVATE);
        msgList = (ListView) findViewById(R.id.messageList);

        renderMessages();

        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                inboxsp.edit().putString("name",senderListDisplay.get(position).toString()).apply();
//                getMessageText(position);
                inboxsp.edit().putString("msgtext",msgListDisplay.get(position).toString()).apply();
                viewMessage();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMessagingActivity();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void renderMessages() {
        numOfMsgs = allmessages.getInt("allmymessagescount",0);

        String msgJsonStr = new String();
        String msgJsonKey = new String();
        JSONObject msgJson;
        String receiverId;
        String senderName;
        String message;
        int i;
        for (i = 0; i < numOfMsgs; i++)
        {
            try
            {
                msgJsonKey = "allmymessages" + i;
                msgJsonStr = allmessages.getString(msgJsonKey,"");
                msgJson = new JSONObject(msgJsonStr);
                msgJson = new JSONObject(json);

                receiverId = msgJson.getString("receiver");setTitle("hello");
                setTitle("hello23");

                if(receiverId.equals(String.valueOf(currentUser.getInt("myid",0))))
                {
                    senderName = msgJson.getString("name");
                    message = msgJson.getString("message");
                    senderListDisplay.add(0,senderName);
                    msgListDisplay.add(0,message);
                    ListAdapter msgAdapter = new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1,senderListDisplay);
                    msgList.setAdapter(msgAdapter);
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }

//        try{
//            test = new JSONObject(json);
//
//            msgListDisplay.add(0, test.getString("phonetype"));
//            msgListDisplay.add(0,"asdfgh");
//            msgListDisplay.add(0,"zxcvbn");
//        }
//        catch (JSONException e)
//        {
//            e.printStackTrace();
//        }

//        JSONObject item0= new JSONObject();
//        JSONObject item1= new JSONObject();
//        JSONObject item2= new JSONObject();
//
//
//        try {
//            item0.put("name", "Sarath");
//            item0.put("msg", "Hello this is Sarath!");
//            messageBundle.put("0", item0);
//
//            item1.put("name", "Shivesh");
//            item1.put("msg", "Hello this is Shivesh!");
//            messageBundle.put("1", item1);
//
//            item2.put("name", "Shrijit");
//            item2.put("msg", "Hello this is Shrijit!");
//            messageBundle.put("2", item2);
//
//            ListAdapter msgAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,msgListDisplay);
//            msgList.setAdapter(msgAdapter);
//        }
//        catch (JSONException e)
//        {
//            e.printStackTrace();
//        }
    }

//    private void getMessageText(int position) {
//        JSONObject currentMessage = new JSONObject();
//        try {
//            currentMessage = messageBundle.getJSONObject(String.valueOf(position));
//            msg = currentMessage.getString("msg");
//        } catch (JSONException e)
//        {
//            e.printStackTrace();
//        }
//    }

//    private void getMessageSender(int position) {
//        JSONObject currentMessage = new JSONObject();
//        try {
//            currentMessage = messageBundle.getJSONObject(String.valueOf(position));
//            msg = currentMessage.getString("name");
//        } catch (JSONException e)
//        {
//            e.printStackTrace();
//        }
//    }

    private void viewMessage() {
        Intent i = new Intent(this,ViewMessageActivity.class);
        startActivity(i);
    }



    private void goToMessagingActivity() {
        Intent i = new Intent(this,MessagingActivity.class);
        startActivity(i);
    }

}
