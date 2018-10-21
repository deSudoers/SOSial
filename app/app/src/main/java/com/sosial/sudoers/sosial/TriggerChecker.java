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
    private SharedPreferences sp, location;
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
        Log.e("destroyed", "no");
        super.onStartCommand(intent, flags, startId);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiManager = (WifiManager)TriggerChecker.this.getSystemService(Context.WIFI_SERVICE);
        stoptimertask();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.e("destroyed", "yes");
        startTimer();
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
        timer.schedule(timerTask, 10000, 10000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                sp = getSharedPreferences("login", MODE_PRIVATE);
                location = getSharedPreferences("mylocation", MODE_PRIVATE);
                String latitude = location.getString("latitude", "");
                String longitude = location.getString("longitude", "");
                if (!latitude.equals("") && !longitude.equals("")) {
                    if (sendJson(latitude, longitude).equals("1")) {
                        try {
                            if (!wifiManager.isWifiEnabled()) {
                                Log.e("wifi_discoverr", "enable wifi");
                                wifiManager.setWifiEnabled(true);
                                iTurnedOn = true;
                            }
                        } catch (Exception e) {
                            Log.e("wifi_discoverr", e.toString());
                        }

                        sp.edit().putBoolean("trigger", true).apply();
                        new NotificationSender(TriggerChecker.this, "", "", "Alert", "Disaster has Occurred.");
                        if (!trigger_done) {
                            trigger_done = true;
                            try {
                                Method method1 = mManager.getClass().getMethod("enableP2p", WifiP2pManager.Channel.class);
                                method1.invoke(mManager, mChannel);
                                Log.e("wifi_discover", "passed");
                                //Toast.makeText(getActivity(), "method found",
                                //       Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                //                            Log.e("wifi_discover", e.toString());
                                //Toast.makeText(getActivity(), "method did not found",
                                //   Toast.LENGTH_SHORT).show();
                            }

                            receiver = new WifiBroadcastReceiver(mManager, mChannel, TriggerChecker.this);
                            registerReceiver(receiver, intentFilter);
                            Log.e("wifi_discover", "registered");
                        }
                        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(int reasonCode) {
                            }
                        });
                    } else {
                        if (iTurnedOn) {
                            wifiManager.setWifiEnabled(false);
                            iTurnedOn = false;
                        }
                        trigger_done = false;
                        try {
                            Log.e("wifi_discover", "unregistered");
                            unregisterReceiver(receiver);
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

    private String sendJson(String latitude, String longitude){
        String url = "https://sosial.azurewebsites.net/trigger";
        String response = "";
        JSONObject postData = new JSONObject();
        try{
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
            Query query =  new Query();
            response  = query.execute(url, postData.toString()).get();
            try{
                JSONObject json = new JSONObject(response);
                response = json.getString("triggered");
                return response;
            }
            catch (Exception e){
                return "0";
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "0";
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
                httpURLConnection.setRequestMethod("POST");
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