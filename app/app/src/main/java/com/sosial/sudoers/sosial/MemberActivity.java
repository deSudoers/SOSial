package com.sosial.sudoers.sosial;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

public class MemberActivity extends AppCompatActivity{

    // UI references.
    private AutoCompleteTextView mEmailView;
    private Spinner mSpinnerlist;
    private Button mDeleteMember;
    private Button mEmailSignInButton;

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        setupActionBar();
        // Set up the login form.

        sp = getSharedPreferences("login", MODE_PRIVATE);
        mDeleteMember = findViewById(R.id.remove_member);
        mSpinnerlist = (Spinner) findViewById(R.id.spinner);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(view, "Sending Request...", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        addMember();
                        Snackbar.make(view, sp.getString("member_error", "An Error Occurred. Please Try Again."), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeList();
                            }
                        });
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        mDeleteMember.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(v, "Sending Request...", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        removeMember();
                        Snackbar.make(v, sp.getString("member_error", "An Error Occurred. Please Try Again"), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeList();
                            }
                        });
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        changeList();
    }

    public void changeList(){
        try {
            String names[] = sp.getString("email", "").split(",");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(MemberActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerlist.setAdapter(adapter);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addMember() {
        String email = mEmailView.getText().toString();

        String response = sendJson(email);
        String rid = "", rname ="", remail="";
        JSONObject json;
        try {
            json = new JSONObject(response);
            rid = json.getString("user_id");
            rname = json.getString("name");
            remail = json.getString("email");
            String old = sp.getString("userid", "");
            old += rid+",";
            sp.edit().putString("userid", old).apply();
            old = sp.getString("name", "");
            old += rname+",";
            sp.edit().putString("name", old).apply();
            old = sp.getString("email", "");
            old += remail+",";
            sp.edit().putString("email", old).apply();
            sp.edit().putString("member_error", "Added Successfully.").apply();
        }
        catch (Exception e){
            e.printStackTrace();
            sp.edit().putString("member_error", response).apply();
        }
    }

    private String sendJson(String email){
        String url = "https://sosial.azurewebsites.net/family";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("email", email);
            AddRequest sdd =  new AddRequest();
            String cookie1 = sp.getString("token2", "");
            String cookie2 = sp.getString("token", "");
            response  = sdd.execute(url, postData.toString(), cookie1, cookie2).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }


    static class AddRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            String data = "An Error Occurred. Please Try Again.";

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
                if(response == HTTP_OK){
                    data = "";
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null){
                        data += line;
                    }
                }
                else if(response == httpURLConnection.HTTP_BAD_REQUEST){
                    data = "User Already in Family.";
                }
                else if(response == httpURLConnection.HTTP_NOT_FOUND){
                    data = "User Not Found.";
                }
                else if(response == httpURLConnection.HTTP_NOT_ACCEPTABLE){
                    data = "User Cannot be Family Member.";
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
    }

    private void removeMember() {
        int selected = mSpinnerlist.getSelectedItemPosition();

        String id = sp.getString("email", "").split(",")[selected];
        if(id != "") {
            String response = sendJson2(id);

            if (response.equals("Deleted Successfully.")) {
                String old[] = sp.getString("userid", "").split(",");
                String old2[] = sp.getString("name", "").split(",");
                String old3[] = sp.getString("email", "").split(",");
                String newuser = "", newname = "", newemail = "";

                for (int i = 0; i < old.length; ++i) {
                    if (i != selected) {
                        newuser += old[i] + ",";
                        newname += old2[i] + ",";
                        newemail += old3[i] + ",";
                    }
                }

                sp.edit().putString("userid", newuser).apply();
                sp.edit().putString("name", newname).apply();
                sp.edit().putString("email", newemail).apply();
            }
            sp.edit().putString("member_error", response).apply();
        }
    }

    private String sendJson2(String id){
        String url = "https://sosial.azurewebsites.net/family";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("email", id);
            DeleteRequest sdd =  new DeleteRequest();
            String cookie1 = sp.getString("token2", "");
            String cookie2 = sp.getString("token", "");
            response  = sdd.execute(url, postData.toString(), cookie1, cookie2).get();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    static class DeleteRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String data = "An Error Occurred. Please Try Again.";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", params[2]);
                httpURLConnection.addRequestProperty("cookie", params[3]);
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();
                int response = httpURLConnection.getResponseCode();
                if(response == HTTP_OK){
                    data = "Deleted Successfully.";
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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
}

