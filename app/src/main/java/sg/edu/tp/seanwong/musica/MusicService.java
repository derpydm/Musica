package sg.edu.tp.seanwong.musica;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import sg.edu.tp.seanwong.musica.util.CustomShuffleOrder;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicService extends Service implements PlayerNotificationManager.MediaDescriptionAdapter {
    private SimpleExoPlayer mp;
    private final IBinder binder = new ServiceBinder();
    private PlayerNotificationManager playerNotificationManager;
    int currentIndex = 0;

    // Some constants
    public static final String CHANNEL_ID = "Musica_Notification_Channel";
    public static final int NOTIFICATION_ID = 123477;
    public static final String ACTION_BIND = "sg.edu.tp.seanwong.musica.MusicService.ACTION_BIND";
    public static final String ACTION_INIT = "sg.edu.tp.seanwong.musica.MusicService.ACTION_INIT";
    public static final String ACTION_START_PLAY = "sg.edu.tp.seanwong.musica.MusicService.ACTION_START_PLAY";

    // Queue and mediasource for playing music
    ArrayList<Song> queue;
    ConcatenatingMediaSource cms;

    // Get binder for service
    // We use this binder to attach to the service from other views
    public class ServiceBinder extends Binder {
        public MusicService getService()
        {
            return MusicService.this;
        }
    }

    public SimpleExoPlayer getplayerInstance() {
        if (mp == null) {
            startPlayer();
        }
        return mp;
    }

    public Song getCurrentSong() {
        try {
            return queue.get(currentIndex);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public ArrayList<Song> getQueue() {
        return queue;
    }

    private void startPlayer() {
        final Context context = this;
        mp = new SimpleExoPlayer.Builder(context).build();
    }

    private void loadMusic() {
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Musica"));
        // get the first song, ConcatenatingMediaSource asserts that media list is not null
        Song first = queue.get(0);
        MediaSource firstMS = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(new File(first.getPath())));
        cms = new ConcatenatingMediaSource(false, false, new CustomShuffleOrder(queue.size(),currentIndex,queue.size()), firstMS);
        for (Song song: queue.subList(1, queue.size())) {
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(new File(song.getPath())));
            cms.addMediaSource(mediaSource);
        }
    }


    private void startListener() {
        SimpleExoPlayer.EventListener el = new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {

            }
            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }
            @Override
            public void onPositionDiscontinuity(int reason) {
                // Update notification upon track change for whatever reason
                currentIndex = mp.getCurrentWindowIndex();
                playerNotificationManager.invalidate();
            }
        };
        mp.addListener(el);
    }

    // Release player resources for memory management
    private void freePlayer() {
        playerNotificationManager.setPlayer(null);
        playerNotificationManager = null;
        mp.stop();
        mp.release();
        stopForeground(true);
    }

    private void startNotification() {
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this, CHANNEL_ID, R.string.notification_name, R.string.notification_desc, NOTIFICATION_ID, this, new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                startForeground(notificationId, notification);
            }
            @Override
            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                stopForeground(true);
                stopSelf();
            }
        });
        playerNotificationManager.setUseNavigationActions(true);
        playerNotificationManager.setUseNavigationActionsInCompactView(true);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setPlayer(mp);
    }

    private void playMusic() {
        // Play the music here, regardless of whether any music was playing beforehand
        // Stop any other tracks beforehand
        mp.prepare(cms);
        mp.seekTo(currentIndex, 0);
        mp.setPlayWhenReady(true);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
        freePlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        // init player if it isn't already initialised
        if (mp == null) {
            startPlayer();
        }
        switch (action) {
            case ACTION_BIND:
                // Do nothing, a view is just binding
                break;
            case ACTION_INIT:
                // Start notifications and listeners
                startNotification();
                startListener();
                break;
            case ACTION_START_PLAY:
                // Indicates that the queue needs to be reset and there is a new queue bundled with the extras.
                queue = intent.getParcelableArrayListExtra("queue");
                currentIndex = intent.getIntExtra("currentIndex",0);
                loadMusic();
                playMusic();
                break;
            default:
                // Something screwed up occured.
                Log.e("Musica", "Undefined behaviour while service was called");
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return binder instance
        return binder;
    }

    // Notification control overrides
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
        Song song = queue.get(currentIndex);
        MediaMetadataRetriever metaData = new MediaMetadataRetriever();
        metaData.setDataSource(song.getPath());
        // Encode the artwork into a byte array and then use BitmapFactory to turn it into a Bitmap to load
        RequestOptions options = new RequestOptions()
                // Means there's no album art, use default album icon
                .error(R.drawable.ic_album_24px)
                .fitCenter();
        byte art[] = metaData.getEmbeddedPicture();
        if (art != null) {
            // Album art exists, we grab the artwork
            Bitmap img = BitmapFactory.decodeByteArray(art,0,art.length);
            return img;
        }
        return null;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        // create pending intent with MainActivity to start the app again
        Intent intent = new Intent(MusicService.this, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
            (MusicService.this, 0, intent, 0);
                        return contentPendingIntent;
    }
}
