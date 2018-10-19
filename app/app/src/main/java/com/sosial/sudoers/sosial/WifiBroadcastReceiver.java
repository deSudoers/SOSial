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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WifiBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Context cxt;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pManager.PeerListListener peerListListener;


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
                manager.requestPeers(channel, peerListListener);
                Log.e("wifi_discover","request_peers");
            }


            peerListListener = new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peerList) {
                    Log.e("wifi_discover", "onPeers");
                    Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
                    if (!refreshedPeers.equals(peers)) {
                        peers.clear();
                        peers.addAll(refreshedPeers);

                        // If an AdapterView is backed by this data, notify it
                        // of the change. For instance, if you have a ListView of
                        // available peers, trigger an update.
//                        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                        // Perform any other updates needed based on the new list of
                        // peers connected to the Wi-Fi P2P network.
                    }

                    if (peers.size() == 0) {
                        Log.d("wifi_discover", "No devices found");
                        return;
                    }
                }
            };

            connectall();

            Log.e("wifi_discover", "peer_size"+peers.size());
            // The peer list has changed! We should probably do something about
            // that.

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            try {
                NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.e("wifi_discover", wifiInfo.toString());
                Log.e("wifi_discover", device.deviceAddress);
                if (networkState.isConnected()) {
                    //set client state so that all needed fields to make a transfer are ready

                    //activity.setTransferStatus(true);
//                cxt.setNetworkToReadyState(true, wifiInfo, device);
//                activity.setClientStatus("Connection Status: Connected");
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

    public void connectall(){
        for(int i = 0; i < peers.size(); ++i) {
            // Picking the first device found on the network.
            WifiP2pDevice device = peers.get(i);
            WifiP2pInfo info = new WifiP2pInfo();
            Log.e("wifi_discover", device.isGroupOwner()+"");

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            Log.e("wifi_discover","address"+config.deviceAddress);
            config.wps.setup = WpsInfo.PBC;

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
}
