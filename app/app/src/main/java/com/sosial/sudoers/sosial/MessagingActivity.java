package com.sosial.sudoers.sosial;

import android.content.SharedPreferences;
import android.inputmethodservice.ExtractEditText;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MessagingActivity extends AppCompatActivity {

    String receiver,msg;
    EditText msgTo;
    EditText msgText;
    Button msgSendButton;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.getString("token", "");

        msgTo = (EditText) findViewById(R.id.msgto);
        msgText = (EditText) findViewById(R.id.msgtext);
        msgSendButton = (Button) findViewById(R.id.msgsend);
        msgSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiver = msgTo.getText().toString();
                msg = msgText.getText().toString();
                sendMessage(receiver,msg);
            }
        });
    }

    private void sendMessage(String receiver, String msg) {
        JSONObject json = null;
        String response = sendJson(receiver,msg);
        try {
            json = new JSONObject(response);
            response = json.getString("message");
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            if(response.equals("Success")) {

            }
        }
    }

    private String sendJson(String receiver,String msg) {
        String url = "http://192.168.43.66:5000/location";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("location", receiver);
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
