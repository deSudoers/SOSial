package com.sosial.sudoers.sosial;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class TriggerChecker extends Service {
    private boolean trigger_done, iTurnedOn;
    private static boolean wifip2p;
    private SharedPreferences sp;
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
                if(sp.getBoolean("trigger",true)) {
                    try {
                        if(!wifiManager.isWifiEnabled()) {
                            Log.e("wifi_discoverr", "enable wifi");
                            wifiManager.setWifiEnabled(true);
                            iTurnedOn = true;
                        }
                    }
                    catch (Exception e){
                        Log.e("wifi_discoverr", e.toString());
                    }

                    sp.edit().putBoolean("trigger", true).apply();
                    new NotificationSender(TriggerChecker.this, "", "", "Alert", "Disaster has Occurred.");
                    if(!trigger_done) {
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
                }
                else {
                    if(iTurnedOn){
                        wifiManager.setWifiEnabled(false);
                        iTurnedOn = false;
                    }
                    trigger_done = false;
                    try {
                        Log.e("wifi_discover", "unregistered");
                        unregisterReceiver(receiver);
                    }
                    catch (Exception e){
                        e.printStackTrace();
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

    public static void setIsWifiP2pEnabled(boolean activated){
        wifip2p = activated;
    }
}