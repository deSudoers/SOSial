package com.sosial.sudoers.sosial;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class Server implements Runnable{
    Context cxt;
    SharedPreferences sp, spmessages;
    static boolean running = false;

    Server(Context cxt){
        this.cxt = cxt;
        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        spmessages = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
    }

    public void run(){
        try {
            while (true) {
                running = true;
                try {
                    ServerSocket sock = new ServerSocket(5678);
                    Socket socket = sock.accept();
                    InputStream in = socket.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(in);
                    String message = (String) ois.readObject();
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                    String msg = "";
                    for (int i = 0; i < spmessages.getInt("allmymessagescount", 0); ++i) {
                        msg = msg.concat(spmessages.getString("allmymessages" + i, "") + " ## ");
                    }
                    String myusers = spmessages.getString("allmyusers", sp.getInt("myid", 0) + ",");

                    String send = myusers + "###" + msg;

                    oos.writeObject(send);
                    ois.close();
                    oos.close();
                    in.close();
                    socket.close();
                    sock.close();

                    String ss[] = message.split("###");
                    String users = ss[0];
                    String msgs = ss[1];
                    addUsers(users);
                    addMessagetoDatabase(msgs);
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            running = false;
        }
    }

//    @Override
//    protected void onPostExecute(String s) {
//        super.onPostExecute(s);
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

