package com.sosial.sudoers.sosial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View.OnClickListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.sosial.sudoers.sosial.LoginActivity.*;
import static com.sosial.sudoers.sosial.R.id.trigger;

public class TriggerActivity extends AppCompatActivity {

    Button triggerButton;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.getString("token", "");
        sp.getString("token2", "");

        triggerButton =  (Button) findViewById(R.id.trigger);

        triggerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("CLK", "Button Pressed");
                initiateTrigger();
            }
        });
    }



    private void initiateTrigger() {
//        String gpsinfo="location";
        String test = "Helo World";
        String response = sendJson(test);

        JSONObject json = null;
        try {
            json = new JSONObject(response);
            response = json.getString("message");
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        catch (Exception e){
        }
        if(response.equals("Location Updated.")){
            Log.e("CHECK","Works!");
        }
    }

    private String sendJson(String test) {
        String url = "https://sosial.azurewebsites.net/location";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("location", test);
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

    class SendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    data+=line;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("U",e.toString());
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

}