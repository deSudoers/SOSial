package com.sosial.sudoers.sosial;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
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

        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(".RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                sp = getSharedPreferences("login", MODE_PRIVATE);
                if(sp.getBoolean("trigger",true)) {

                    try {
                        if(!wifiManager.isWifiEnabled()) {
                            Log.e("wifi_discoverr", "alreadY");
                            wifiManager.setWifiEnabled(true);
                            iTurnedOn = true;
                        }
                    }
                    catch (Exception e){
                        Log.e("wifi_discoverr", e.toString());
                    }

                    sp.edit().putBoolean("trigger", true).apply();
                    new NotificationSender(TriggerChecker.this, "", "", "Alert", "Disaster has Occurred");
                    if(!trigger_done) {

                        receiver = new WifiBroadcastReceiver(mManager, mChannel, TriggerChecker.this);
                        registerReceiver(receiver, intentFilter);

                    }
                }
                else {
                    if(iTurnedOn){
                        wifiManager.setWifiEnabled(false);
                        iTurnedOn = false;
                    }
                    trigger_done = false;
                    try {
                        unregisterReceiver(receiver);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

//                try {
//
//
//
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                    Log.e("try_catch", e.toString());
//                }
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

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();
            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);

                // If an AdapterView is backed by this data, notify it
                // of the change. For instance, if you have a ListView of
                // available peers, trigger an update.
//                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
            }

            if (peers.size() == 0) {
                Log.d("wifidirection", "No devices found");
                return;
            }
        }
    };

    public static void setIsWifiP2pEnabled(boolean activated){
        wifip2p = activated;
    }
}