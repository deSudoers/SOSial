package com.sosial.sudoers.sosial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
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

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context cxt;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private SharedPreferences sp, sp2;
    int count;
    WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Context cxt){
        this.manager = manager;
        this.channel = channel;
        this.cxt = cxt;
        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        sp2 = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
        count = 0;
        Log.e("wifi_discover", "wifi_broadcaser");
    }

    public void onReceive(Context context, Intent intent) {
        Log.e("wifi_discover", "onreceiver");
        if (manager != null) {
            manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    for(WifiP2pDevice wd: peers.getDeviceList()){
                        connectToPeer(wd);
                    }
                    Log.e("wifi_peers", "peer_size"+peers.getDeviceList().size());
                }
            });
        }

        String msg = "";
        for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
            msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
        }
        String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");

        if (count++%10 == 0) {
            try {
                Server s = new Server(myusers, msg);
                String get = s.get();
                Log.e("wifi_message_server", get);
                addUsers(get.split("###")[0]);
                addMessagetoDatabase(get.split("###")[1]);
            } catch (Exception e) {
                Log.e("wifi_discover_except", e.toString());
            }
        } else {
            try {
                Client c = new Client(myusers, msg);
                String get = c.get();
                Log.e("wifi_message_client", get);
                addUsers(get.split("###")[0]);
                addMessagetoDatabase(get.split("###")[1]);
            } catch (Exception e) {
                Log.e("wifi_discover_client_e", e.toString());
            }
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
            }

            @Override
            public void onFailure(int reason) {
                Log.e("wifi_discover","Connection Failed");
            }
        });
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