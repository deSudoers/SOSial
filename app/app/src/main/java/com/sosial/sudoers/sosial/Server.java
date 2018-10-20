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
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port = 8080;
    String ids, msgs;

    Server(String ids, String msgs){
        this.ids = ids;
        this.msgs = msgs;
    }

    String get(){
        GetRequest gr = new GetRequest();
        try {
            return gr.execute().get();
        }
        catch (Exception e){
            return e.toString();
        }
    }

    class GetRequest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){
            ServerSocket sock = null;
            Socket socket = null;
            OutputStream os = null;
            try{
                sock = new ServerSocket(port);
            }
            catch (Exception e){
                Log.e("wifi_server", e.toString());
            }
            while (true) {
                try {
                    Log.e("wifi_server", "inside_try");
                    socket = sock.accept();
                    InputStream in = socket.getInputStream();

                    ObjectInputStream ois = new ObjectInputStream(in);
                    //convert ObjectInputStream object to String
                    String message = (String) ois.readObject();
                    //create ObjectOutputStream object
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    //write object to Socket
                    String send = "{ids:{" + ids + "}, messages:{" + msgs + "}}\n";
                    oos.writeObject(send);
                    //close resources
                    ois.close();
                    oos.close();
                    socket.close();
                    Log.e("wifi_server", "outside_try");
                    return message;
                } catch (Exception e) {
                    Log.e("wifi_server_catch", e.toString());
                } finally {
                    try {
                        sock.close();
                    } catch (Exception e) {
                        Log.e("wifi_server_finally", e.toString());
                    }
                }
            }
        }
    }
}
