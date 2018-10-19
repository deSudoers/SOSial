package com.sosial.sudoers.sosial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context cxt;
    private WifiP2pManager.PeerListListener peerListListener;

    WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Context cxt){
        this.manager = manager;
        this.channel = channel;
        this.cxt = cxt;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                activity.setIsWifiP2pEnabled(true);
                Log.e("wifi_discoverr", "on");
            } else {
//                activity.setIsWifiP2pEnabled(false);
                Log.e("wifi_discoverr", "off");
            }

            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    // Code for when the discovery initiation is successful goes here.
                    // No services have actually been discovered yet, so this method
                    // can often be left blank. Code for peer discovery goes in the
                    // onReceive method, detailed below.
                    Log.e("wifi_discoverr", "success");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.e("wifi_discoverr", "failed");
                    // Code for when the discovery initiation fails goes here.
                    // Alert the user that something went wrong.
                }
            });

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    // Code for when the discovery initiation is successful goes here.
                    // No services have actually been discovered yet, so this method
                    // can often be left blank. Code for peer discovery goes in the
                    // onReceive method, detailed below.
                    Log.e("wifi_discoverr", "successd");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.e("wifi_discoverr", "failedd");
                    // Code for when the discovery initiation fails goes here.
                    // Alert the user that something went wrong.
                }
            });
            // The peer list has changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }

    }

//    @Override
//    public void connect(){
//        // Picking the first device found on the network.
////        WifiP2pDevice device = peers.get(0);
//
////        WifiP2pConfig config = new WifiP2pConfig();
////        config.deviceAddress = device.deviceAddress;
////        config.wps.setup = WpsInfo.PBC;
////
//        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
//            }
//
//            @Override
//            public void onFailure(int reason) {
//                Toast.makeText(cxt, "Connect failed. Retry.",
//                        Toast.LENGTH_SHORT).show();
//            }
////        });
//    }
}
