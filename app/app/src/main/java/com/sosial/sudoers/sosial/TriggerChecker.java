package com.sosial.sudoers.sosial;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class TriggerChecker extends Service {
    private static volatile boolean exit;
    private static volatile boolean trigger_done;

    public TriggerChecker(){
        super();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        checker c = new checker(this);
        Thread t = new Thread(c);
        t.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        stoptimertask();
//        super.onDestroy();
//        Log.e("wifi_d", "destroy");
//        Intent broadcastIntent = new Intent(".RestartSensor");
//        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static void end(){
        exit = true;
        trigger_done = false;
    }


    class checker implements Runnable {
        WifiManager wifiManager;
        private boolean iTurnedOn;
        private SharedPreferences sp, splocation, spmessages;
        WifiBroadcastReceiver receiver;
        WifiP2pManager.Channel mChannel;
        WifiP2pManager mManager;
        private Timer timer;
        private TimerTask timerTask;
        private Context cxt;

        checker(Context cxt) {
            this.cxt = cxt;
            wifiManager = (WifiManager) cxt.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        public void run() {
            timer = new Timer();
            initializeTimerTask();
            timer.schedule(timerTask, 0, 30000);
        }

        private void initializeTimerTask() {
            sp = cxt.getSharedPreferences("login", MODE_PRIVATE);
            splocation = cxt.getSharedPreferences("location", MODE_PRIVATE);
            spmessages = cxt.getSharedPreferences("allmessages", MODE_PRIVATE);
            timerTask = new TimerTask() {
                public void run() {
                    if (!exit) {
                        String latitude = splocation.getString("latitude", "");
                        String longitude = splocation.getString("longitude", "");
                        if ((!latitude.equals("") && !longitude.equals("")) || sp.getBoolean("trigger", false)) {
                            if (sp.getBoolean("trigger", false) || sendJson(latitude, longitude).equals("1")) {
                                getMessages();
                                sendMessages();
                                try {
                                    if (!wifiManager.isWifiEnabled()) {
                                        wifiManager.setWifiEnabled(true);
                                        iTurnedOn = true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                sp.edit().putBoolean("trigger", true).apply();
                                if (!trigger_done) {
                                    trigger_done = true;
                                    try {
                                        Method method1 = mManager.getClass().getMethod("enableP2p", WifiP2pManager.Channel.class);
                                        method1.invoke(mManager, mChannel);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    new NotificationSender(cxt, "", "", "Alert", "Disaster has Occurred.");
                                    receiver = new WifiBroadcastReceiver(cxt, wifiManager);

//                                    Server s = new Server(cxt);
//                                    new Thread(s).start();
//                                    Client c = new Client(cxt);
//                                    new Thread(c).start();
                                }

                                try {
                                    mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
                                    mChannel = mManager.initialize(cxt, getMainLooper(), new WifiP2pManager.ChannelListener() {
                                        @Override
                                        public void onChannelDisconnected() {
                                            mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
                                        }
                                    });
                                    Class[] paramTypes = new Class[3];
                                    paramTypes[0] = WifiP2pManager.Channel.class;
                                    paramTypes[1] = String.class;
                                    paramTypes[2] = WifiP2pManager.ActionListener.class;
                                    Method setDeviceName = mManager.getClass().getMethod(
                                            "setDeviceName", paramTypes);
                                    setDeviceName.setAccessible(true);

                                    Object arglist[] = new Object[3];
                                    arglist[0] = mChannel;
                                    arglist[1] = "SOSIAL"+sp.getInt("myid", 0);
                                    arglist[2] = new WifiP2pManager.ActionListener() {

                                        @Override
                                        public void onSuccess() {}

                                        @Override
                                        public void onFailure(int reason) {}
                                    };
                                    setDeviceName.invoke(mManager, arglist);

                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (iTurnedOn) {
                                    wifiManager.setWifiEnabled(false);
                                    iTurnedOn = false;
                                }
                                trigger_done = false;
                                try {
    //                            unregisterReceiver(receiver);
                                    receiver.stoptimertask();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        exit = false;
                        receiver.stoptimertask();
                        stoptimertask();
                    }
                }
            };
        }

        public void stoptimertask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        public void getMessages() {
            String users[] = spmessages.getString("allmyusers", sp.getInt("myid", 0) + ",").split(",");
            String url = "https://sosial.azurewebsites.net/message";
            JSONObject postData = new JSONObject();
            try {
                for (int i = 0; i < users.length; ++i) {
                    postData.put(i + "", users[i]);
                }
                Query query = new Query();
                String response = query.execute(url, postData.toString(), "POST", sp.getString("token2", ""), sp.getString("token", "")).get();
                addMessagetoDatabase(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String sendJson(String latitude, String longitude) {
            String url = "https://sosial.azurewebsites.net/trigger";
            String response = "";
            JSONObject postData = new JSONObject();
            try {
                postData.put("latitude", latitude);
                postData.put("longitude", longitude);
                Query query = new Query();
                response = query.execute(url, postData.toString(), "POST", sp.getString("token2", ""), sp.getString("token", "")).get();
                try {
                    JSONObject json = new JSONObject(response);
                    response = json.getString("triggered");
                    return response;
                } catch (Exception e) {
                    return "0";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "0";
        }

        public void sendMessages() {
            int numOfMsgs = spmessages.getInt("allmymessagescount", 0);
            String url = "https://sosial.azurewebsites.net/message";

            String msgJsonStr, msgJsonKey;
            JSONObject msgJson, sendJson = new JSONObject();
            String senderId, receiverId, message, key;
            int k = 0;
            for (int i = 0; i < numOfMsgs; i++) {
                try {
                    msgJsonKey = "allmymessages" + i;
                    msgJsonStr = spmessages.getString(msgJsonKey, "");
                    msgJson = new JSONObject(msgJsonStr);

                    senderId = msgJson.getString("sender");
                    receiverId = msgJson.getString("receiver");
                    message = msgJson.getString("message");
                    key = msgJson.getString("key");

                    JSONObject curMsg = new JSONObject();
                    curMsg.put("sender_id", senderId);
                    curMsg.put("receiver_id", receiverId);
                    curMsg.put("message", message);
                    curMsg.put("unique_key", key);
                    sendJson.put(k + "", curMsg);
                    k++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Query query = new Query();
            try {
                query.execute(url, sendJson.toString(), "PUT", sp.getString("token2", ""), sp.getString("token", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void addMessagetoDatabase(String messages) {
            JSONObject jsonMsg = null;
            try {
                jsonMsg = new JSONObject(messages);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (int i = 0; ; i++) {
                try {
                    JSONObject msg = jsonMsg.getJSONObject(String.valueOf(i));
                    String myid = msg.getInt("sender_id")+"";
                    String receiver = msg.getInt("receiver_id") + "";
                    String mssg = msg.getString("message");
                    String key = msg.getString("unique_id");
                    String sendername = msg.getString("sender_name");
                    addMessagetoDatabase(myid, sendername, receiver, mssg, key);
                } catch (Exception e) {
                    break;
                }
            }
        }

        public void addMessagetoDatabase(String myid, String sendername, String receiver, String msg, String key) {
            int count = spmessages.getInt("allmymessagescount", 0);
            for (int i = 0; i < count; ++i) {
                try {
                    JSONObject json = new JSONObject(spmessages.getString("allmymessages" + i, ""));
                    String k = json.getString("key");
                    if (key.equals(k)) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JSONObject mssg = new JSONObject();
            try {
                mssg.put("sender", myid);
                mssg.put("receiver", receiver);
                mssg.put("name", sendername);
                mssg.put("message", msg);
                mssg.put("key", key);
                spmessages.edit().putString("allmymessages" + count, mssg.toString()).apply();
                spmessages.edit().putInt("allmymessagescount", ++count).apply();
                if (receiver.equals(sp.getInt("myid", 0) + ""))
                    new NotificationSender(cxt, "", "", sendername, msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        class Query extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                String data = "";

                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                    httpURLConnection.addRequestProperty("cookie", params[3]);
                    httpURLConnection.addRequestProperty("cookie", params[4]);
                    httpURLConnection.setRequestMethod(params[2]);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(params[1]);
                    wr.flush();
                    wr.close();

                    int response = httpURLConnection.getResponseCode();
                    if (response == httpURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            data += line;
                        }
                    } else {
                        data = "An Error Occurred. Please Try Again.";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }

                return data;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }
    }
}