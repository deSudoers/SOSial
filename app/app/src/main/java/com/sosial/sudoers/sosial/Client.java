package com.sosial.sudoers.sosial;

import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private int port = 8080;
    private WifiP2pInfo info;
    String ids, msgs;

    Client(WifiP2pInfo info, String ids, String msgs){
        this.info = info;
        this.ids = ids;
        this.msgs = msgs;
    }

    public String get(){
        try{
            SendRequest sr = new SendRequest();
            return  sr.execute().get();
        }
        catch (Exception e){
            return e.toString();
        }
    }

    class SendRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){
            InetAddress targetIP = null;
            try {
                targetIP = InetAddress.getByName("192.168.49.20");
            }
            catch (Exception e){
                Log.e("wifi_ip", e.toString());
            }
            Socket sock = null;
            OutputStream os = null;
            try {
                sock = new Socket();
                sock.connect(new InetSocketAddress(targetIP, port), 10000);
                if (sock.isConnected()) {
                    OutputStream out = sock.getOutputStream();
                    ObjectOutputStream output = new ObjectOutputStream(out);
                    String send = "{ids:{"+ids+"}, messages:{"+msgs+"}}\n";
                    output.writeObject(send);
                    InputStream in = sock.getInputStream();
                    ObjectInputStream input = new ObjectInputStream(in);
                    String message = (String) input.readObject();
                    out.close();
                    output.close();
                    return message;
                }
            }
            catch (Exception e){
                Log.e("wifi_client_catch", e.toString());
            }
            finally {
                try {
                    sock.close();
                }
                catch (Exception e){
                    Log.e("wifi_client_finally", e.toString());
                }
                return "finally";
            }
        }
    }
}
