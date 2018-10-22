package com.sosial.sudoers.sosial;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int port = 5678;
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
            String message = "finally";
            try{
                sock = new ServerSocket(port);
                sock.setSoTimeout(20000);
            }
            catch (Exception e){
                Log.e("wifi_server", e.toString());
            }
            try {
                Log.e("wifi_server", "inside_try");
                socket = sock.accept();
                InputStream in = socket.getInputStream();

                ObjectInputStream ois = new ObjectInputStream(in);
                message = (String) ois.readObject();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                String send = ids+"###"+ msgs;
                oos.writeObject(send);
                ois.close();
                oos.close();
                socket.close();
                Log.e("wifi_server", "outside_try");
            } catch (Exception e) {
                Log.e("wifi_server_catch", e.toString());
            } finally {
                try {
                    sock.close();
                } catch (Exception e) {
                    Log.e("wifi_server_finally", e.toString());
                }
                return message;
            }
        }
    }
}
