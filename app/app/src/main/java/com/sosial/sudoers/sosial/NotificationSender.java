package com.sosial.sudoers.sosial;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationSender {
    static int counter = 0;
    NotificationSender(Context cxt, String bigTitle, String bigDetails, String title, String text){
        try {
            Intent ii = new Intent(cxt.getApplicationContext(), cxt.getClass());
            PendingIntent pendingIntent = PendingIntent.getActivity(cxt, 0, ii, 0);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(cxt, "notify_001")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setPriority(android.app.Notification.PRIORITY_MAX);

            NotificationManager mNotificationManager =
                    (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("notify_001",
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(channel);
            }

            mNotificationManager.notify(counter++, mBuilder.build());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
