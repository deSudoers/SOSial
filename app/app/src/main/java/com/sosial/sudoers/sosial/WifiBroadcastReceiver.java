package com.sosial.sudoers.sosial;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Looper.getMainLooper;

public class WifiBroadcastReceiver extends Thread{
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiManager wifimanager;
    private Context cxt;
    private SharedPreferences sp, sp2;
    int count, count2;
    List<WifiP2pDevice> lists = new ArrayList<>();
    public WifiBroadcastReceiver(){

    }

    WifiBroadcastReceiver(Context cxt, WifiManager wifiManager){
        this.cxt = cxt;

        manager = (WifiP2pManager) cxt.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(cxt, getMainLooper(), null);
        wifimanager = wifiManager;

        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        sp2 = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
        count = 0;
        count2 = 0;
        startTimer();
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 30000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
//                count++;
                if (manager != null) {
//                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(cxt, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//                        try {
//                            act.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
//                        }
//                        catch (Exception e){
//                            Log.e("wifi_test", e.toString());
//                        }
//                        //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
//                    }
                    manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            Collection<WifiP2pDevice> wds = peers.getDeviceList();
                            List<WifiP2pDevice> peerlist = new ArrayList(wds);
                            Collections.shuffle(peerlist);
                            lists.clear();
                            lists = new ArrayList<>(peerlist);
                        }
                    });
                }
                int count = 0;
                try {

                    for (final WifiP2pDevice wd : lists) {
                        if(wd.deviceName.equals(sp2.getString("connected", ""))){
                            sp2.edit().putString("connected", "").apply();
                            continue;
                        }
                        if(wd.deviceName.contains("SOSIAL")) {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    connectToPeer(wd);
                                }
                            };
                            Thread thread = new Thread(runnable);
                            thread.start();
                            count++;
//                            new NotificationSender(cxt, wd.deviceName, wd.deviceName, wd.deviceName, wd.deviceName);
                        }
                    }
                }
                catch (ConcurrentModificationException cme){
                    cme.printStackTrace();
                }

                if(count == 0){
                    try {
                        for (WifiP2pDevice wd : lists) {
                            connectToPeer(wd);
                            count++;
                            new NotificationSender(cxt, wd.deviceName, wd.deviceName, wd.deviceName, wd.deviceName);
                        }
                    }
                    catch (ConcurrentModificationException cme){
                        cme.printStackTrace();
                    }
                }
//
//                String msg = "";
//                for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
//                    msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
//                }
//
//                final String mymsg = msg;
//                final String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");
//
//                if (count++%3 == 0) {
//                    Log.e("wifi_test", "broadcast");
//                    count = 1;"connected"
//                    try {
//                        Runnable runnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                Server s = new Server(cxt);
//                                s.execute(myusers, mymsg);
//                            }
//                        };
//                        Thread mythread = new Thread(runnable);
//                        mythread.start();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Log.e("wifi_test", "receiver");
//                    try {
//                        try {
//                            Runnable runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    Client c = new Client(cxt);
//                                    c.execute(myusers, mymsg);
//                                }
//                            };
//                            Thread mythread = new Thread(runnable);
//                            mythread.start();
//                        }
//                        catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                if(count2++%5 == 0) {
//                    count2 = 1;
//                    wifimanager.setWifiEnabled(false);
//                    wifimanager.setWifiEnabled(true);
//                }
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

    public void connectToPeer(final WifiP2pDevice peer){
        // Picking the first device found on the network.;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {

                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        Log.e("wifi_test", info.toString());
                        sp2.edit().putString("connected", peer.deviceName).apply();
                        String msg = "";
                        for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
                            msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
                        }
                        final String mymsg = msg;
                        final String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");
//                        if(info.groupFormed) {
                            try {
                                if (info.isGroupOwner) {
                                    try {
                                        Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                Server s = new Server(cxt);
                                                try {
                                                    s.execute(myusers, mymsg, peer.deviceName);
                                                }
                                                catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        };
                                        Thread mythread = new Thread(runnable);
                                        mythread.start();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        Runnable runnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Client c = new Client(cxt);
                                                    c.execute(myusers, mymsg, peer.deviceName);
                                                }
                                                catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        };
                                        Thread mythread = new Thread(runnable);
                                        mythread.start();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                        }
                    }
                });
            }

            @Override
            public void onFailure(int reason) {}
        });
    }
}