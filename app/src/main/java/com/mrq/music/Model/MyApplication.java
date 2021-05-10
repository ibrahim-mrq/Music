package com.mrq.music.Model;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mrq.music.Activity.MainActivity;
import com.mrq.music.R;
import com.orhanobut.hawk.Hawk;

import java.util.List;

public class MyApplication extends Application {

    public static final String CHANNEL_ID = R.string.app_name + "_Notification_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        Hawk.init(getApplicationContext()).build();
        crateNotification();

        Intent intent = new Intent(getApplicationContext(), MyService.class);
        ContextCompat.startForegroundService(getApplicationContext(), intent);
    }

    private void crateNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    protected boolean isAppRunning(Context context) {
        String activity = MainActivity.class.getName();
        ActivityManager activityManager = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.
                getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activity.equals(task.baseActivity.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
