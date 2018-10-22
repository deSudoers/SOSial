package com.sosial.sudoers.sosial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Looper.getMainLooper;

public class WifiBroadcastReceiver extends Thread{
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context cxt;
    private IntentFilter intentFilter = new IntentFilter();
    private SharedPreferences sp, sp2;
    int count;
    public WifiBroadcastReceiver(){

    }

    WifiBroadcastReceiver(Context cxt){
        this.cxt = cxt;
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//
//        // Indicates a change in the list of available peers.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//
//        // Indicates the state of Wi-Fi P2P connectivity has changed.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//
//        // Indicates this device's details have changed.
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) cxt.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(cxt, getMainLooper(), null);

        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        sp2 = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
        count = 0;
        Log.e("wifi_discover", "wifi_broadcaster");
        startTimer();
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 6000, 20000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if (manager != null) {
                    manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            for(WifiP2pDevice wd: peers.getDeviceList()){
                                connectToPeer(wd);
                            }
                            Log.e("wifi_deers", "peer_size"+peers.getDeviceList().size());
                        }
                    });
                }

                String msg = "";
                for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
                    msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
                }
                String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");

                if (count++%5 == 0) {
                    count = 1;
                    try {
                        Server s = new Server(myusers, msg);
                        String get = s.get();
                        Log.e("wifi_d_message_server", get);
                        if(!get.equals("finally")) {
                            addUsers(get.split("###")[0]);
                            addMessagetoDatabase(get.split("###")[1]);
                        }
                    } catch (Exception e) {
                        Log.e("wifi_discover_except", e.toString());
                    }
                } else {
                    try {
                        Client c = new Client(myusers, msg);
                        String get = c.get();
                        Log.e("wifi_message_client", get);
                        if(!get.equals("finally")) {
                            addUsers(get.split("###")[0]);
                            addMessagetoDatabase(get.split("###")[1]);
                        }
                    } catch (Exception e) {
                        Log.e("wifi_discover_client_e", e.toString());
                    }
                }
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                    }
                });
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void connectToPeer(WifiP2pDevice peer){
        // Picking the first device found on the network.;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        Log.e("wifi_discover_master","address"+config.deviceAddress);

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                Log.e("wifi_discover","Connected");
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        String msg = "";
                        for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
                            msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
                        }
                        String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");
                        try {
                            if (info.isGroupOwner) {
                                Server s = new Server(myusers, msg);
                                String get = s.get();
                                Log.e("wifi_d_message_server", get);
                                if (!get.equals("finally")) {
                                    addUsers(get.split("###")[0]);
                                    addMessagetoDatabase(get.split("###")[1]);
                                }
                            } else {
                                Client c = new Client(myusers, msg);
                                String get = c.get();
                                Log.e("wifi_message_client", get);
                                if (!get.equals("finally")) {
                                    addUsers(get.split("###")[0]);
                                    addMessagetoDatabase(get.split("###")[1]);
                                }
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.e("wifi_discover","Connection Failed");
            }
        });
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
            if (receiver.equals(sp.getInt("myid", 0) + ""))
                new NotificationSender(cxt, "", "", name, msg);
            sp2.edit().putString("allmymessages"+count,mssg.toString()).apply();
            sp2.edit().putInt("allmymessagescount", ++count).apply();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void addMessagetoDatabase(String allmsgs){
        String msgs[] = allmsgs.split("##");
        for(String msg: msgs){
            try{
                Log.e("addusersmsg_", msg);
                JSONObject json = new JSONObject(msg);
                String myid = json.getString("sender");
                String receiver = json.getString("receiver");
                String name = json.getString("name");
                String mssg = json.getString("message");
                String key = json.getString("key");
                addMessagetoDatabase(myid, name, receiver, mssg, key);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void addUsers(String allusers){
        Log.e("addusers", allusers);
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