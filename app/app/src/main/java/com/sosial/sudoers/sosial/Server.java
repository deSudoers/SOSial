package com.sosial.sudoers.sosial;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class Server extends AsyncTask<String, Void, String>{
    Context cxt;
    SharedPreferences sp, spmessages;
    Server(Context cxt){
        this.cxt = cxt;
        sp = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
        spmessages = cxt.getSharedPreferences("allmessages", Context.MODE_PRIVATE);
    }
    @Override
    protected String doInBackground(String... params){
        ServerSocket sock = null;
        Socket socket = null;
        String message = "finally";
        try{
            sock = new ServerSocket(5678);
            sock.setSoTimeout(20000);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            Log.e("wifi_server", "inside_try");
            socket = sock.accept();
            InputStream in = socket.getInputStream();

            ObjectInputStream ois = new ObjectInputStream(in);
            message = (String) ois.readObject();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            String send = params[0]+"###"+ params[1];
            oos.writeObject(send);
            ois.close();
            oos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return message;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!s.equals("finally")) {
            addUsers(s.split("###")[0]);
            addMessagetoDatabase(s.split("###")[1]);
        }
    }

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
                Log.e("addusersmsg_", msg);
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
        Log.e("addusers", allusers);
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

