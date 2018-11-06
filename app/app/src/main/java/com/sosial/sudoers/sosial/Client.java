package com.sosial.sudoers.sosial;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable{
    private int port = 5678;
    Context cxt;
    SharedPreferences sp, spmessages;
    static boolean running = false;

    Client(Context cxt){
        this.cxt = cxt;
        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        spmessages = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
    }

    public void run(){
//        int count = 1;
        InetAddress targetIP = null;
        try {
            targetIP = InetAddress.getByName("192.168.49.1");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            while (true) {
                running = true;
//            if(count++>60)
//                break;
                try {
                    Socket sock = new Socket();
                    sock.connect(new InetSocketAddress(targetIP, port));
                    OutputStream out = sock.getOutputStream();
                    ObjectOutputStream output = new ObjectOutputStream(out);

                    String msg = "";
                    for (int i = 0; i < spmessages.getInt("allmymessagescount", 0); ++i) {
                        msg = msg.concat(spmessages.getString("allmymessages" + i, "") + " ## ");
                    }
                    String myusers = spmessages.getString("allmyusers", sp.getInt("myid", 0) + ",");

                    String send = myusers + "###" + msg;
                    output.writeObject(send);
                    InputStream in = sock.getInputStream();
                    ObjectInputStream input = new ObjectInputStream(in);

                    String message = (String) input.readObject();
                    input.close();
                    in.close();
                    output.close();
                    out.close();
                    sock.close();

                    String ss[] = message.split("###");
                    String users = ss[0];
                    String msgs = ss[1];
                    addUsers(users);
                    addMessagetoDatabase(msgs);
                    Thread.sleep(10000);
                } catch (Exception e) {
                    try{
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException ie){
                        ie.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
        finally {
            running = false;
        }
//        return message;
    }

//    @Override
//    protected void onPostExecute(String s){
//        try {
//            if (!s.equals("finally")) {
//                String ss[] = s.split("###");
//                String users = ss[0];
//                String msgs = ss[1];
//                addUsers(users);
//                addMessagetoDatabase(msgs);
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    public void addMessagetoDatabase(String myid, String name, String receiver, String msg, String key){
        int count = spmessages.getInt("allmymessagescount",0);
        for(int i = 0; i < count; ++i){
            try {
                JSONObject json = new JSONObject(spmessages.getString("allmymessages" + i, ""));
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
            mssg.put("name", name);
            mssg.put("message", msg);
            mssg.put("key", key);
            if (receiver.equals(sp.getInt("myid", 0) + ""))
                new NotificationSender(cxt, "", "", name, msg);
            spmessages.edit().putString("allmymessages"+count,mssg.toString()).apply();
            spmessages.edit().putInt("allmymessagescount", ++count).apply();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void addMessagetoDatabase(String allmsgs){
        String msgs[] = allmsgs.split("##");
        for(String msg: msgs){
            try{
                JSONObject json = new JSONObject(msg);
                String myid = json.getString("sender");
                String receiver = json.getString("receiver");
                String name = json.getString("name");
                String mssg = json.getString("message");
                String key = json.getString("key");
                addMessagetoDatabase(myid, name, receiver, mssg, key);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void addUsers(String allusers){
        String users[] = allusers.split(",");
        String allmyusers = spmessages.getString("allmyusers", sp.getInt("myid", 0)+",");
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
                allmyusers = spmessages.getString("allmyusers", sp.getInt("myid", 0)+",");
                spmessages.edit().putString("allmyusers", allmyusers+usr+",").apply();
            }
        }
    }
}
