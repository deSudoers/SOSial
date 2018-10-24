package com.sosial.sudoers.sosial;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private int port = 5678;
    String ids, msgs;

    Client(String ids, String msgs){
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
            String message = "finally";
            try {
                targetIP = InetAddress.getByName("192.168.49.1");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Socket sock = null;
            try {
                sock = new Socket();
                sock.connect(new InetSocketAddress(targetIP, port), 10000);
                if (sock.isConnected()) {
                    OutputStream out = sock.getOutputStream();
                    ObjectOutputStream output = new ObjectOutputStream(out);
                    String send = ids+"###"+msgs;
                    output.writeObject(send);
                    InputStream in = sock.getInputStream();
                    ObjectInputStream input = new ObjectInputStream(in);
                    message = (String) input.readObject();
                    out.close();
                    output.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                try {
                    sock.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return message;
            }
        }
    }
}
