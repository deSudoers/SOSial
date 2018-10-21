package com.sosial.sudoers.sosial;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class MessagingActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

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
                attemptLogin();
                Snackbar.make(view, sp.getString("message_error", "An Error Occurred. Please Try Again."), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Store values at the time of the login attempt.
        int selected = mSpinner.getSelectedItemPosition();

        String id = sp.getString("userid", "").split(",")[selected];

        String message = mMessage.getText().toString();
        if(message.trim().equals("")){
            sp.edit().putString("message_error", "Please Write Some Message.").apply();
        }
        else
        if(id != "") {
            sendJson(id, message);
            sp.edit().putString("message_error", "Sent Successfully").apply();
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
            String myid = getString(sp.getInt("myid", 0));
            postDatai.put("sender_id", myid);
            postDatai.put("receiver_id", id);
            postDatai.put("message", msg);
            String key = Password.hashPassword(sp.getInt("myid", 0)+msg+id);
            postDatai.put("unique_key", key);
            postData.put("0", postDatai);

            addMessagetoDatabase(myid, id, msg, key);

            SendRequest sdd =  new SendRequest();
            response  = sdd.execute(url, postData.toString()).get();
        }
        catch (JSONException e){
            e.printStackTrace();
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

            String data = "";

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

    public void addMessagetoDatabase(String myid, String receiver, String msg, String key){
        int count = sp2.getInt("allmymessagescount",0);
        for(int i = 0; i < count; ++i){
            try {
                JSONObject json = new JSONObject(sp2.getString("allmymessages" + count, ""));
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
            mssg.put("name", name);
            mssg.put("message", msssg);
            mssg.put("key", key);
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        sp2.edit().putString("allmymessages"+count,mssg.toString()).apply();
        sp2.edit().putInt("allmymessagescount", ++count).apply();
    }

    public void addMessagetoDatabase(String allmsgs){
        String msgs[] = allmsgs.split("##");
        for(String msg: msgs){
            try{
                JSONObject json = new JSONObject(msg);
                String myid = json.getString("sender");
                String receiver = json.getString("receiver");
                String mssg = json.getString("message");
                String key = json.getString("key");
                addMessagetoDatabase(myid, receiver, mssg, key);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void addUsers(String allusers){
        String users[] = allusers.split(",");
        String allmyusers = sp2.getString("allmyusers", sp.getInt("myid", 0)+",");
        String already[] = allmyusers.split(",");
        for(String usr: users){
            int flag = 0;
            for(String alr: already){
                if(usr.equals(alr)){
                    flag = 1;
                    break;
                }
            }
            if(flag == 0){
                allmyusers = sp2.getString("allmyusers", sp.getInt("myid", 0)+",");
                sp2.edit().putString("allmyusers", allmyusers+usr+",").apply();
            }
        }
    }
}