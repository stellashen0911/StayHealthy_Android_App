package com.example.stayhealthy_android_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "this.is.my.channelId";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, WorkoutActivity.class);//on tap this activity will open

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(WorkoutActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);//getting the pendingIntent

        Notification.Builder builder = new Notification.Builder(context);//building the notification

        Notification notification = builder.setContentTitle("StayHealthy Notification")
                .setContentText("Remember to do the work-out")
                .setTicker("Finish your work-out target and STAY HEALTHY!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//below creating notification channel, because of androids latest update, O is Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Work-out",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notification);
    }
}