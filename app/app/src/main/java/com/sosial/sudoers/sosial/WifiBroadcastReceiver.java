package com.sosial.sudoers.sosial;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Timer;
import java.util.TimerTask;

import static android.os.Looper.getMainLooper;

public class WifiBroadcastReceiver extends Thread{
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context cxt;
    private SharedPreferences sp, sp2;
    int count;
    public WifiBroadcastReceiver(){

    }

    WifiBroadcastReceiver(Context cxt){
        this.cxt = cxt;

        manager = (WifiP2pManager) cxt.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(cxt, getMainLooper(), null);

        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        sp2 = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
        count = 0;
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
                if (manager != null) {
                    manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            for(WifiP2pDevice wd: peers.getDeviceList()){
                                connectToPeer(wd);
                            }
                        }
                    });
                }

                String msg = "";
                for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
                    msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
                }

                final String mymsg = msg;
                final String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");

                if (count++%5 == 0) {
                    count = 1;
                    try {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Server s = new Server(cxt);
                                s.execute(myusers, mymsg);
                            }
                        };
                        Thread mythread = new Thread(runnable);
                        mythread.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        try {
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    Client c = new Client(cxt);
                                    c.execute(myusers, mymsg);
                                }
                            };
                            Thread mythread = new Thread(runnable);
                            mythread.start();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
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

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        String msg = "";
                        for (int i = 0; i < sp2.getInt("allmymessagescount", 0); ++i) {
                            msg = msg.concat(sp2.getString("allmymessages" + i, "") + " ## ");
                        }
                        final String mymsg = msg;
                        final String myusers = sp2.getString("allmyusers", sp.getInt("myid", 0) + ",");
                        try {
                            if (info.isGroupOwner) {
                                try {
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            Server s = new Server(cxt);
                                            s.execute(myusers, mymsg);
                                        }
                                    };
                                    Thread mythread = new Thread(runnable);
                                    mythread.start();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            Client c = new Client(cxt);
                                            c.execute(myusers, mymsg);
                                        }
                                    };
                                    Thread mythread = new Thread(runnable);
                                    mythread.start();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
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
            public void onFailure(int reason) {}
        });
    }
}