package com.mrq.music.Model;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.mrq.music.Activity.MainActivity;
import com.mrq.music.Model.Music;
import com.mrq.music.Notification.NotificationActionService;
import com.mrq.music.R;
import com.orhanobut.hawk.Hawk;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.mrq.music.Notification.CreateNotification.ACTION_NEXT;
import static com.mrq.music.Notification.CreateNotification.ACTION_PLAY;
import static com.mrq.music.Notification.CreateNotification.ACTION_PREVIUOS;

public class MusicAppWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        Intent intentPlay = new Intent(context, NotificationActionService.class).setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Hawk.contains("image")) {
            String uri = Hawk.get("image");
//            if (uri != null)
//                views.setImageViewUri(R.id.customWidgetMusic_img, Uri.parse(uri));
            views.setImageViewBitmap(R.id.customWidgetMusic_img, loadBitmap(context, uri));
        }

        if (Hawk.contains("name")) {
            String name = Hawk.get("name", "music");
            if (name != null)
                views.setTextViewText(R.id.customWidgetMusic_tv_title, name + " ");
        }

        if (Hawk.contains("isPlay")) {
            int isPlay = Hawk.get("isPlay", 0);
            if (isPlay != 0)
                views.setImageViewResource(R.id.customWidgetMusic_player_play_btn, isPlay);
        }

        int pos = Hawk.get("pos", 0);
        int size = Hawk.get("size", 0);

        PendingIntent pendingIntentNext;
        if (pos == size) {
            pendingIntentNext = null;
        } else {
            Intent intentNext = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_NEXT);
            pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                    intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        PendingIntent pendingIntentPrevious;
        if (pos == 0) {
            pendingIntentPrevious = null;
        } else {
            Intent intentPrevious = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PREVIUOS);
            pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                    intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        views.setOnClickPendingIntent(
                R.id.customWidgetMusic_player_play_btn, pendingIntentPlay);
        views.setOnClickPendingIntent(
                R.id.customWidgetMusic_player_previous_btn, pendingIntentPrevious);
        views.setOnClickPendingIntent(
                R.id.customWidgetMusic_player_next_btn, pendingIntentNext);

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static Bitmap loadBitmap(Context context, String uri) {
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_header_icon);
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

}

