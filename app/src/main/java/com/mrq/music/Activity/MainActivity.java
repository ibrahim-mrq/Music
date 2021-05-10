package com.mrq.music.Activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.kennyc.bottomsheet.menu.BottomSheetMenu;
import com.mrq.music.Adapter.MusicAdapter;
import com.mrq.music.Interface.MusicInterface;
import com.mrq.music.Model.Music;
import com.mrq.music.Model.MusicAppWidget;
import com.mrq.music.Notification.CreateNotification;
import com.mrq.music.Notification.NotificationActionService;
import com.mrq.music.Notification.Playable;
import com.mrq.music.R;
import com.orhanobut.hawk.Hawk;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, Playable {

    private ArrayList<Music> list = new ArrayList<>();
    private MusicAdapter adapter;
    private RecyclerView rv;

    private ConstraintLayout playerSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageButton playBtn, next_btn, previous_btn, play_btn2, repeat_btn;
    private TextView tv_empty, playerFilename, tv_stat_time, tv_end_time;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isLoop = false;
    private String pathToPlay = null;
    private BottomSheetMenu sheetMenuSort;
    private int item;
    private int position = 0;
    private NotificationManager notificationManager;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        sheetMenuSort = new BottomSheetMenu(MainActivity.this);
        sheetMenuSort.add(R.string.sort_by_name);
        sheetMenuSort.add(R.string.sort_by_duration);
        sheetMenuSort.add(R.string.sort_by_artist);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getMusics();
                        sort();
                        onClick();
                        tv_empty.setVisibility(View.GONE);
                        playerSheet.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.VISIBLE);
                        setVisibility();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            createChannel();
                            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
                            startService(new Intent(getBaseContext(), NotificationActionService.class));
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        tv_empty.setVisibility(View.VISIBLE);
                        tv_empty.setText(R.string.permission);
                        playerSheet.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        tv_empty.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(MainActivity.this, MainActivity.class));
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, final PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();

        if (!isAppRunning(this)) {
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }

    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Music", NotificationManager.IMPORTANCE_HIGH);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sort() {
        if (Hawk.contains("item")) {
            item = Hawk.get("item");
        } else item = 0;

        if (item == 0) {
            getMusics();
            Collections.sort(list, new Comparator<Music>() {
                @Override
                public int compare(Music o1, Music o2) {
                    return o1.songTitle.compareTo(o2.songTitle);
                }
            });
        }
        if (item == 1) {
            getMusics();
            Collections.sort(list, new Comparator<Music>() {
                @Override
                public int compare(Music o1, Music o2) {
                    return o1.songDuration.compareTo(o2.songDuration);
                }
            });
        }
        if (item == 2) {
            getMusics();
            Collections.sort(list, new Comparator<Music>() {
                @Override
                public int compare(Music o1, Music o2) {
                    return o1.songArtist.compareTo(o2.songArtist);
                }
            });
        }
    }

    private void showSortBottomSheet(BottomSheetMenu sheetMenu) {
        if (Hawk.contains("item")) {
            item = Hawk.get("item");
        } else item = 0;
        new BottomSheet.Builder(MainActivity.this)
                .setMenu(sheetMenu)
                .setSelected(item)
                .setTitle(R.string.sort)
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown(@NonNull BottomSheet bottomSheet) {

                    }

                    @Override
                    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem item, int position) {
                        Hawk.put("item", position);
                        bottomSheet.dismiss();
                        if (position == 0) {
                            getMusics();
                            Collections.sort(list, new Comparator<Music>() {
                                @Override
                                public int compare(Music o1, Music o2) {
                                    return o1.songTitle.compareTo(o2.songTitle);
                                }
                            });

                        }
                        if (position == 1) {
                            getMusics();
                            Collections.sort(list, new Comparator<Music>() {
                                @Override
                                public int compare(Music o1, Music o2) {
                                    return o1.songDuration.compareTo(o2.songDuration);
                                }
                            });
                        }
                        if (position == 2) {
                            getMusics();
                            Collections.sort(list, new Comparator<Music>() {
                                @Override
                                public int compare(Music o1, Music o2) {
                                    return o1.songArtist.compareTo(o2.songArtist);
                                }
                            });
                        }

                    }

                    @Override
                    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem item) {

                    }

                    @Override
                    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, int dismissEvent) {

                    }
                })
                .show();
    }

    private void initView() {
        rv = findViewById(R.id.rv);
        playerSheet = findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);
        playBtn = findViewById(R.id.player_play_btn);
        play_btn2 = findViewById(R.id.player_play_btn2);
        repeat_btn = findViewById(R.id.player_repeat_btn);
        previous_btn = findViewById(R.id.player_previous_btn);
        next_btn = findViewById(R.id.player_next_btn);
        playerFilename = findViewById(R.id.player_filename);
        playerSeekbar = findViewById(R.id.player_seekbar);
        tv_stat_time = findViewById(R.id.player_stat_time);
        tv_end_time = findViewById(R.id.player_end_time);
        tv_empty = findViewById(R.id.main_tv_empty);
    }

    private void setVisibility() {
        if (position + 1 == list.size()) {
            next_btn.setVisibility(View.GONE);
        } else {
            next_btn.setVisibility(View.VISIBLE);
        }
        if (position == 0) {
            previous_btn.setVisibility(View.GONE);
        } else {
            previous_btn.setVisibility(View.VISIBLE);
        }
    }

    private void onClick() {

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (isPlaying) {
                        onTrackPause();
                    } else {
                        if (pathToPlay != null) {
                            resumeAudio();
                        } else
                            onTrackPlay();
                    }
                }
            }
        });

        play_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (isPlaying) {
                        onTrackPause();
                    } else {
                        if (pathToPlay != null) {
                            resumeAudio();
                        } else
                            onTrackPlay();
                    }
                }
            }
        });

        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoop) {
                    isLoop = false;
                    repeat_btn.setImageResource(R.drawable.ic_repeat);
                } else {
                    isLoop = true;
                    repeat_btn.setImageResource(R.drawable.ic_repeat_one);
                }
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position++;
                next();
                setVisibility();
            }
        });

        previous_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position--;
                back();
                setVisibility();
            }
        });

        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                tv_stat_time.setText(setCorrectDuration(progress + "") + "");
                resumeAudio();
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    play_btn2.animate().alpha(1.0f).setDuration(1000);
                } else {
                    play_btn2.animate().alpha(0.0f).setDuration(1000);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    public void getMusics() {
        list.clear();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int songId = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songCount = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            do {
                long img = cursor.getLong(albumId);
                long thisId = cursor.getLong(songId);
                String cursorTitle = cursor.getString(songTitle);
                String cursorArtist = cursor.getString(songArtist);
                String cursorSize = cursor.getString(songCount);
                String cursorData = cursor.getString(songData);

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, img);

                Music music = new Music();
                music.setSongId(thisId);
                music.setSongDuration(setCorrectDuration(cursorSize));
                music.setSongTitle(cursorTitle);
                music.setSongArtist(cursorArtist);
                music.setSongPath(cursorData);
                music.setImage(albumArtUri);
                list.add(music);
            } while (cursor.moveToNext());
        }
        if (list.isEmpty()) {
            tv_empty.setVisibility(View.VISIBLE);
            tv_empty.setText(R.string.empty);
            rv.setVisibility(View.GONE);
            playerSheet.setVisibility(View.GONE);
        } else {
            playerSheet.setVisibility(View.VISIBLE);
            rv.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
            adapter = new MusicAdapter(list, this, new MusicInterface() {
                @Override
                public void onClick(Music model) {
                    position = adapter.getSelectedPosition();
                    playerFilename.setText(model.getSongTitle());
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    pathToPlay = model.getSongPath();
                    CreateNotification.createNotification(MainActivity.this, list.get(position),
                            R.drawable.player_pause_btn, position, list.size() - 1);
                    stopAudio();
                    updateWidget(R.drawable.player_pause_btn, String.valueOf(list.get(position).getImage()), position, list.size() - 1);
                    playAudio(pathToPlay);
                    tv_end_time.setText(model.getSongDuration());
                    setVisibility();
                }
            });
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setHasFixedSize(true);
            rv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private String setCorrectDuration(String songs_duration) {
        int time = Integer.parseInt(songs_duration);
        int seconds = time / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        if (seconds < 10) {
            songs_duration = minutes + ":0" + seconds;
        } else {
            songs_duration = minutes + ":" + seconds;
        }
        return songs_duration;
    }

    private void stopAudio() {
        playBtn.setImageDrawable(getDrawable(R.drawable.player_play_btn));
        play_btn2.setImageDrawable(getDrawable(R.drawable.player_play_btn));
        isPlaying = false;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            seekbarHandler.removeCallbacks(updateSeekbar);
        }
        adapter.notifyDataSetChanged();
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_play_btn, null));
        play_btn2.setImageDrawable(getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
        adapter.selectedPosition = -1;
        adapter.notifyDataSetChanged();
    }

    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_pause_btn, null));
        play_btn2.setImageDrawable(getResources().getDrawable(R.drawable.player_pause_btn, null));
        CreateNotification.createNotification(MainActivity.this, list.get(position),
                R.drawable.player_pause_btn, position, list.size() - 1);
        updateWidget(R.drawable.player_pause_btn, String.valueOf(list.get(position).getImage()), position, list.size() - 1);
        isPlaying = true;
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
        adapter.selectedPosition = adapter.selectedPosition2;
        adapter.notifyDataSetChanged();
    }

    private void playAudio(String fileToPlay) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(fileToPlay));
            mediaPlayer.prepare();
            isPlaying = true;
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "لا يمكن الوصول للملفات !", Toast.LENGTH_SHORT).show();
        }
        playBtn.setImageDrawable(getResources().getDrawable(R.drawable.player_pause_btn, null));
        play_btn2.setImageDrawable(getResources().getDrawable(R.drawable.player_pause_btn, null));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isLoop) {
                    next();
                } else {
                    position++;
                    next();
                }
            }
        });
        playerSeekbar.setMax(mediaPlayer.getDuration());
        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
                tv_stat_time.setText(setCorrectDuration(playerSeekbar.getProgress() + "") + "");
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.tool_search);
        MenuItemCompat.setShowAsAction(searchItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tool_sort:
                showSortBottomSheet(sheetMenuSort);
                break;
            case R.id.tool_search:
                break;
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }

    // notification

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action) {
                case CreateNotification.ACTION_PREVIUOS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying) {
                        onTrackPause();
                    } else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
            }
        }
    };

    @Override
    public void onTrackPrevious() {
        position--;
        back();
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(MainActivity.this, list.get(position),
                R.drawable.player_pause_btn, position, list.size() - 1);
        updateWidget(R.drawable.player_pause_btn, String.valueOf(list.get(position).getImage()), position, list.size() - 1);
        if (isPlaying) {
            onTrackPause();
        } else {
            if (pathToPlay != null) {
                resumeAudio();
            } else
                onTrackPlay();
        }
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(MainActivity.this, list.get(position),
                R.drawable.player_play_btn, position, list.size() - 1);
        updateWidget(R.drawable.player_play_btn, String.valueOf(list.get(position).getImage()), position, list.size() - 1);
        pauseAudio();
        isPlaying = false;
    }

    @Override
    public void onTrackNext() {
        position++;
        next();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
        if (!isAppRunning(this)) {
            ActivityCompat.finishAffinity(this);
            System.exit(0);
        }
    }

    private void next() {
        CreateNotification.createNotification(MainActivity.this, list.get(position),
                R.drawable.player_pause_btn, position, list.size() - 1);
        updateWidget(R.drawable.player_pause_btn, String.valueOf(list.get(position).getImage()), position, list.size() - 1);
        Log.d("img", list.get(position).getImage() + "");
        playerFilename.setText(list.get(position).getSongTitle());
        stopAudio();
        playAudio(list.get(position).getSongPath());
        tv_end_time.setText(list.get(position).getSongDuration());
        adapter.setSelectedPosition(position);
        adapter.notifyItemChanged(position);
        adapter.notifyDataSetChanged();
    }

    private void back() {
        CreateNotification.createNotification(MainActivity.this, list.get(position),
                R.drawable.player_pause_btn, position, list.size() - 1);
        updateWidget(R.drawable.player_pause_btn, String.valueOf(list.get(position).getImage()), position, list.size() - 1);
        playerFilename.setText(list.get(position).getSongTitle());
        stopAudio();
        playAudio(list.get(position).getSongPath());
        tv_end_time.setText(list.get(position).getSongDuration());
        adapter.setSelectedPosition(position);
        adapter.notifyItemChanged(position);
        adapter.notifyDataSetChanged();
    }

    public void updateWidget(@DrawableRes int isPlay, String image, int pos, int size) {
        Intent intent = new Intent(this, MusicAppWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), MusicAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        Hawk.put("name", list.get(position).getSongTitle());
        Hawk.put("isPlay", isPlay);
        Hawk.put("image", image);
        Hawk.put("pos", pos);
        Hawk.put("size", size);
        sendBroadcast(intent);
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

//    private Toast backToasty;
//    private long backPressedTime;
//
//    @Override
//    public void onBackPressed() {
//        if (backPressedTime + 2000 > System.currentTimeMillis()) {
//            backToasty.cancel();
//            super.onBackPressed();
//            return;
//        } else {
//            backToasty = Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT);
//            backToasty.show();
//        }
//        backPressedTime = System.currentTimeMillis();
//    }


}