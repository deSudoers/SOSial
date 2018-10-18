package com.sosial.sudoers.sosial;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class TriggerChecker extends Service {
    private SharedPreferences sp;
    public int counter=0;
    Context cxt;
    public TriggerChecker(Context applicationContext) {
        super();
        cxt = applicationContext;
    }

    public TriggerChecker(){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(".RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 10000, 10000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                sp = getSharedPreferences("login", MODE_PRIVATE);
                try {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(TriggerChecker.this, "notify_001");
                    Intent ii = new Intent(TriggerChecker.this, TriggerChecker.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(TriggerChecker.this, 0, ii, 0);

                    NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                    bigText.setBigContentTitle("Title");
                    bigText.setSummaryText("Text in detail");

                    mBuilder.setContentIntent(pendingIntent);
                    mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
                    mBuilder.setContentTitle("Your Title");
                    mBuilder.setContentText("Your text");
                    mBuilder.setPriority(Notification.PRIORITY_MAX);
                    mBuilder.setStyle(bigText);

                    NotificationManager mNotificationManager =
                            (NotificationManager) TriggerChecker.this.getSystemService(Context.NOTIFICATION_SERVICE);


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("notify_001",
                                "Channel human readable title",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        mNotificationManager.createNotificationChannel(channel);
                    }

                    mNotificationManager.notify(0, mBuilder.build());
                }
                catch (Exception e){
                    e.printStackTrace();
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}