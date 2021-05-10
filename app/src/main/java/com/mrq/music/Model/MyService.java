package com.mrq.music.Model;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mrq.music.Activity.MainActivity;
import com.mrq.music.Notification.NotificationActionService;
import com.mrq.music.R;
import com.orhanobut.hawk.Hawk;

import static com.mrq.music.Model.MyApplication.CHANNEL_ID;

public class MyService extends Service {

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        onTaskRemoved(intent);
        Intent intentOpen = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentOpen = PendingIntent.getBroadcast(this, 0,
                intentOpen, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.player_header_icon)
                .setContentTitle("music")
                .setContentText("Tap to open the application")
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntentOpen)
                .setShowWhen(false)
                .build();

        startForeground(1, notification);

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

}
