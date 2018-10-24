package com.sosial.sudoers.sosial;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessagingActivity extends AppCompatActivity {

    // UI references.
    private EditText mMessage;
    private Spinner mSpinner;
    private SharedPreferences sp;
    private SharedPreferences sp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        setupActionBar();

        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp2 = getSharedPreferences("allmessages", MODE_PRIVATE);
        // Set up the login form.
        mSpinner = (Spinner) findViewById(R.id.spinner2);

        String names[] = sp.getString("name", "").split(",");
        ArrayAdapter<String> adapter= new ArrayAdapter<>(MessagingActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mMessage = (EditText) findViewById(R.id.editText);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                Snackbar.make(view, sp.getString("message_error", "An Error Occurred. Please Try Again."), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void sendMessage() {

        // Store values at the time of the login attempt.
        int selected = mSpinner.getSelectedItemPosition();

        String id = sp.getString("userid", "").split(",")[selected];

        String message = mMessage.getText().toString();
        if(message.trim().equals("")){
            sp.edit().putString("message_error", "Please Write Some Message.").apply();
        }
        else
        if(id != "") {
            String response = sendJson(id, message);
            sp.edit().putString("message_error", response).apply();
        }
        else{
            sp.edit().putString("message_error", "An Error Occurred. Please Try Again.").apply();
        }
    }

    private String sendJson(String id, String msg){
        String url = "https://sosial.azurewebsites.net/message";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            JSONObject postDatai = new JSONObject();
            String myid = sp.getInt("myid", 0)+"";
            postDatai.put("sender_id", myid);
            postDatai.put("receiver_id", id);
            postDatai.put("message", msg);
            String key = myid+id+msg.substring(0,min(10, msg.length()))+msg.length();
            postDatai.put("unique_key", key);
            postData.put("0", postDatai);
            addMessagetoDatabase(myid, sp.getString("myname", ""), id, msg, key);
            SendRequest sdd =  new SendRequest();
            response  = sdd.execute(url, postData.toString()).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    class SendRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            String data = "Message Saved for Sharing Over Wifi";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                httpURLConnection.setRequestMethod("PUT");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                int response = httpURLConnection.getResponseCode();
                if(response == 200)
                    data = "Message sent to server.";

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

    public void addMessagetoDatabase(String myid, String name, String receiver, String msg, String key){
        int count = sp2.getInt("allmymessagescount",0);
        for(int i = 0; i < count; ++i){
            try {
                JSONObject json = new JSONObject(sp2.getString("allmymessages" + i, ""));
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
            mssg.put("name", name);
            mssg.put("message", msg);
            mssg.put("key", key);
            sp2.edit().putString("allmymessages"+count,mssg.toString()).apply();
            sp2.edit().putInt("allmymessagescount", ++count).apply();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    int min(int x, int y){
        return x<y ? x: y;
    }
}