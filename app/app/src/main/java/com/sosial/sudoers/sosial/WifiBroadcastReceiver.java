package com.sosial.sudoers.sosial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context cxt;
    private List<WifiP2pDevice> peers = new ArrayList<>();


    WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Context cxt){
        this.manager = manager;
        this.channel = channel;
        this.cxt = cxt;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                activity.setIsWifiP2pEnabled(true);
                Log.e("wifi_discover", "on");
            } else {
//                activity.setIsWifiP2pEnabled(false);
                Log.e("wifi_discover", "off");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

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
            // The peer list has changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            try {
                WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.e("wifi_discoverline69", wifiInfo.toString());

                if (manager == null) {
                    return;
                }
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                Log.e("wifi_discover", "After network info");

                if (networkInfo.isConnected()) {
                    manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {

                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo info) {
                            Log.e("wifi_discover", "network info avail");
                            Log.e("wifi_discover_86", info.toString());
                            if (info.isGroupOwner) {
                                Server s = new Server("11", "22");
                                Log.e("wifi_server", s.get() + "extra");
                            } else {
                                Client c = new Client(new WifiP2pInfo(), "1", "2");
                                Log.e("wifi_client", c.get() + "extra");
                            }
                        }
                    });
                }
            }
            catch (Exception e){
                Log.e("wifi_discover", e.toString());
            }
            Log.e("wifi_discover", "conn changed");
            // Connection state changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            Log.e("wifi_discover", "device changed");

        }
    }

    public void connectToPeer(WifiP2pDevice peer){
        // Picking the first device found on the network.;
        WifiP2pInfo info = new WifiP2pInfo();
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
}

