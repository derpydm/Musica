package sg.edu.tp.seanwong.musica;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import sg.edu.tp.seanwong.musica.util.Song;

public class MusicService extends Service implements
        PlayerNotificationManager.MediaDescriptionAdapter {
    //TODO potential reimplementation using ExoPlayer which looks to be easier
    private SimpleExoPlayer mp;
    private final IBinder binder = new ServiceBinder();
    private PlayerNotificationManager playerNotificationManager;
    int currentIndex = 0;

    public static final String CHANNEL_ID = "Musica_Notification_Channel";
    public static final int NOTIFICATION_ID = 123477;
    public static final String ACTION_INIT = "sg.edu.tp.seanwong.musica.MusicService.ACTION_INIT";
    public static final String ACTION_START_PLAY = "sg.edu.tp.seanwong.musica.MusicService.ACTION_START_PLAY";
    public static final String ACTION_PLAY = "sg.edu.tp.seanwong.musica.MusicService.ACTION_PLAY";
    public static final String ACTION_PAUSE = "sg.edu.tp.seanwong.musica.MusicService.ACTION_PAUSE";
    public static final String ACTION_SKIP = "sg.edu.tp.seanwong.musica.MusicService.ACTION_SKIP";
    public static final String ACTION_PREVIOUS = "sg.edu.tp.seanwong.musica.MusicService.ACTION_PREVIOUS";
    public static final String ACTION_SHUFFLE = "sg.edu.tp.seanwong.musica.MusicService.ACTION_SHUFFLE";
    public static final String ACTION_REPEAT = "sg.edu.tp.seanwong.musica.MusicService.ACTION_REPEAT";
    public static final String MUSIC_NEXT_SONG = "sg.edu.tp.seanwong.musica.MusicService.MUSIC_NEXT_SONG";
    public static final String MUSIC_ENDED = "sg.edu.tp.seanwong.musica.MusicService.MUSIC_ENDED";
    ArrayList<Song> queue;

    // Get binder for service
    // Not sure if needed as of right now
    public class ServiceBinder extends Binder {
        public MusicService getService()
        {
            return MusicService.this;
        }
    }

    private void freePlayer() {
        // Null out player
        if (mp != null) {
            playerNotificationManager.setPlayer(null);
            mp.release();
            mp = null;
        }
    }

    public SimpleExoPlayer getplayerInstance() {
        if (mp == null) {
            startPlayer();
        }
        return mp;
    }

    private void startPlayer() {
        final Context context = this;
        mp = new SimpleExoPlayer.Builder(context).build();
        mp.setPlayWhenReady(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (mp == null) {
            startPlayer();
        }
        Log.d("Action", action);

        switch (action) {
            case ACTION_INIT:
                startNotification();
                break;
            case ACTION_START_PLAY:
                // Indicates that the queue needs to be reset and it is bundled with the extras.
                queue = intent.getParcelableArrayListExtra("queue");
                currentIndex = intent.getIntExtra("currentValue",0);
                playMusic();
                break;
            case ACTION_PLAY:
                // Behaviour for starting playback with queue loaded already (i.e from pause)
                if (mp != null) {
                    Log.d("music player", "play!");
                    playMusic();
                }
                break;
            case ACTION_PAUSE:
                // Behaviour to pause music (assume music is playing)
                if (mp != null) {
                    Log.d("music player", "paused!");
                }
                break;
            case ACTION_PREVIOUS:
                // Behaviour to go to the previous song in the queue
                break;
            case ACTION_SKIP:
                // Behaviour to go to the next song in the queue
                break;
            case ACTION_SHUFFLE:
                // Behaviour to shuffle the entire queue
                break;
            case ACTION_REPEAT:
                // Behaviour to repeat the current song
                break;
            default:
                // Something screwed up occured.
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return binder;
    }

    private void startNotification() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this, CHANNEL_ID, R.string.notification_name, R.string.notification_desc, NOTIFICATION_ID, this, new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {

                startForeground(notificationId, notification);
            }
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopSelf();
            }
        });
        playerNotificationManager.setUseNavigationActions(true);
        playerNotificationManager.setUseNavigationActionsInCompactView(true);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setPlayer(mp);
    }
    private void pauseMusic() {
        // Pause the current song, we assume that music is playing beforehand
    }
    // TODO: Make this work with the service
    // TODO: Handle pausing and resuming at the timestamp
    private void playMusic() {
        // Get the song that should be playing
        Song song = queue.get(currentIndex);
        Uri songUri = Uri.parse(song.getPath());
        // Play the music here, regardless of whether any music was playing beforehand
        // Stop any other tracks beforehand
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Musica"));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(songUri);
        mp.prepare(mediaSource);

    }

    @Override
    public String getCurrentContentTitle(Player player) {
        return queue.get(currentIndex).getTitle();
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return queue.get(currentIndex).getArtist();
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player,
                                      PlayerNotificationManager.BitmapCallback callback) {
        return null;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }
}
