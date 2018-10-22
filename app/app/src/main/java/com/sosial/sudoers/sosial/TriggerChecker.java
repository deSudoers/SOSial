package com.sosial.sudoers.sosial;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class TriggerChecker extends Service {
    private boolean trigger_done, iTurnedOn;
    private SharedPreferences sp, splocation, spmessages;
    private final IntentFilter intentFilter = new IntentFilter();
    WifiBroadcastReceiver receiver;
    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    WifiManager wifiManager;
    Context cxt;

    public TriggerChecker(Context applicationContext) {
        super();
        cxt = applicationContext;
    }

    public TriggerChecker(){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        wifiManager = (WifiManager)TriggerChecker.this.getSystemService(Context.WIFI_SERVICE);
        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        stoptimertask();
//        super.onDestroy();
//        stoptimertask();
//        Log.e("destroyed", "yes");
//        Intent broadcastIntent = new Intent(".RestartSensor");
//        sendBroadcast(broadcastIntent);
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 60000);
    }

    public void initializeTimerTask() {
        sp = getSharedPreferences("login", MODE_PRIVATE);
        splocation = getSharedPreferences("location", MODE_PRIVATE);
        spmessages = getSharedPreferences("allmessages", MODE_PRIVATE);
        timerTask = new TimerTask() {
            public void run() {
                String latitude = splocation.getString("latitude", "");
                String longitude = splocation.getString("longitude", "");
                if (!latitude.equals("") && !longitude.equals("")) {
                    if (sendJson(latitude, longitude).equals("1")) {
                        getMessages();
                        sendMessages();
                        try {
                            if (!wifiManager.isWifiEnabled()) {
                                wifiManager.setWifiEnabled(true);
                                iTurnedOn = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        sp.edit().putBoolean("trigger", true).apply();
                        if (!trigger_done) {
                            trigger_done = true;
                            try {
                                Method method1 = mManager.getClass().getMethod("enableP2p", WifiP2pManager.Channel.class);
                                method1.invoke(mManager, mChannel);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            new NotificationSender(TriggerChecker.this, "", "", "Alert", "Disaster has Occurred.");
                            receiver = new WifiBroadcastReceiver(TriggerChecker.this);
//                            registerReceiver(receiver, intentFilter);
//                            receiver.startTimer();
                        }

                    } else {
                        if (iTurnedOn) {
                            wifiManager.setWifiEnabled(false);
                            iTurnedOn = false;
                        }
                        trigger_done = false;
                        try {
//                            unregisterReceiver(receiver);
                            receiver.stoptimertask();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getMessages(){
        String users[] = spmessages.getString("allmyusers", sp.getInt("myid", 0)+",").split(",");
        String url = "https://sosial.azurewebsites.net/message";
        JSONObject postData = new JSONObject();
        try{
            for(int i = 0; i < users.length; ++i){
                postData.put(i+"", users[i]);
            }
            Query query =  new Query();
            String response  = query.execute(url, postData.toString(), "POST").get();
            addMessagetoDatabase(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String sendJson(String latitude, String longitude){
        String url = "https://sosial.azurewebsites.net/trigger";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
            Query query =  new Query();
            response  = query.execute(url, postData.toString(), "POST").get();
            try{
                JSONObject json = new JSONObject(response);
                response = json.getString("triggered");
                return response;
            }
            catch (Exception e){
                return "0";
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "0";
    }

    public void sendMessages(){
        int numOfMsgs = spmessages.getInt("allmymessagescount",0);
        String url = "https://sosial.azurewebsites.net/message";

        String msgJsonStr, msgJsonKey;
        JSONObject msgJson, sendJson = new JSONObject();
        String senderId, receiverId, message, key;
        int k = 0;
        for (int i = 0; i < numOfMsgs; i++) {
            try {
                msgJsonKey = "allmymessages" + i;
                msgJsonStr = spmessages.getString(msgJsonKey, "");
                msgJson = new JSONObject(msgJsonStr);

                senderId = msgJson.getString("sender");
                receiverId = msgJson.getString("receiver");
                message = msgJson.getString("name")+"#"+msgJson.getString("message");
                key = msgJson.getString("key");

                JSONObject curMsg = new JSONObject();
                curMsg.put("sender_id", senderId);
                curMsg.put("receiver_id", receiverId);
                curMsg.put("message", message);
                curMsg.put("unique_key", key);
                sendJson.put(k+"", curMsg);
                k++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Query query = new Query();
        try{
            query.execute(url, sendJson.toString(), "PUT");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addMessagetoDatabase(String messages){
        JSONObject jsonMsg = null;
        try {
            jsonMsg = new JSONObject(messages);
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        for(int i=0;;i++)
        {
            try {
                JSONObject msg = jsonMsg.getJSONObject(String.valueOf(i));
                String myid = msg.getString("sender_id");
                String receiver = msg.getString("receiver_id");
                String mssg = msg.getString("message");
                String key = msg.getString("unigue_key");
                addMessagetoDatabase(myid, receiver, mssg, key);
            }
            catch (Exception e){
                break;
            }
        }
    }

    public void addMessagetoDatabase(String myid, String receiver, String msg, String key){
        int count = spmessages.getInt("allmymessagescount",0);
        for(int i = 0; i < count; ++i){
            try {
                JSONObject json = new JSONObject(spmessages.getString("allmymessages" + i, ""));
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
            if(receiver.equals(sp.getInt("myid", 0)+""))
                new NotificationSender(TriggerChecker.this, "", "", name, msssg);
            mssg.put("name", name);
            mssg.put("message", msssg);
            mssg.put("key", key);
            spmessages.edit().putString("allmymessages"+count,mssg.toString()).apply();
            spmessages.edit().putInt("allmymessagescount", ++count).apply();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    class Query extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.addRequestProperty("cookie", sp.getString("token2",""));
                httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                httpURLConnection.setRequestMethod(params[2]);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                int response = httpURLConnection.getResponseCode();
                if(response == httpURLConnection.HTTP_OK){
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null){
                        data += line;
                    }
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
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }
}