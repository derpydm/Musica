package sg.edu.tp.seanwong.musica;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sg.edu.tp.seanwong.musica.util.Song;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    //TODO potential reimplementation using ExoPlayer which looks to be easier
    MediaPlayer mp;
    int currentIndex = 0;
    int progress = 0;



    public enum State {
        // Possible states of starting this service
        // The first two are self explanatory, OTHER refers to other actions such as playing and pausing
        STARTED_FROM_MUSIC,
        STARTED_FROM_PLAYLIST,
        OTHER
    }

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
        MusicService getService()
        {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (action) {
            case ACTION_START_PLAY:
                // Indicates that the queue needs to be reset and it is bundled with the extras.
                queue = intent.getParcelableArrayListExtra("queue");
                currentIndex = 0;
                progress = 0;
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
                    mp.pause();
                    progress = mp.getCurrentPosition();
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

        // Use state instead in this approach
        // Actions through intent looks nicer though
//        MusicService.State state = (MusicService.State) intent.getSerializableExtra("state");
//        if (intent == null) {
//            // Undefined behaviour here because this service is not sticky
//            // Intent should never be null.
//        } else if (state == State.STARTED_FROM_MUSIC) {
//
//        } else if (state == State.STARTED_FROM_PLAYLIST) {
//            // Handle if started from playlist
//        } else if (state == State.OTHER) {
//
//        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    private void pauseMusic() {
        // Pause the current song, we assume that music is playing beforehand
        progress = mp.getCurrentPosition();
        mp.pause();
    }
    // TODO: Make this work with the service
    // TODO: Handle pausing and resuming at the timestamp
    private void playMusic() {
        // Get the song that should be playing
        Song song = queue.get(currentIndex);
        // Play the music here, regardless of whether any music was playing beforehand
        // Stop any other tracks beforehand

        if (mp == null) {
            mp = new MediaPlayer();
        }
        mp.reset();
        // Prepare path for current song
        try {
            mp.setDataSource(song.getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Handle if music is paused
        if (progress > 0) {
            mp.seekTo(progress);
        }
        mp.start();
        // Handler for moving to next track
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mpa) {
        // Check if the queue contains a new song
        // If so we play it
        if (currentIndex + 1 != queue.size()) {
            currentIndex += 1;
            informFragmentOfNextSong(queue.get(currentIndex));
            playMusic();
        } else {
            currentIndex = 0;
            mp.reset();
            informFragmentOfMusicEnd();
        }
    }

    private void informFragmentOfNextSong(Song song) {
        // Send broadcast that a next song is about to be played.
        Intent nextSongIntent = new Intent(MUSIC_NEXT_SONG);
        nextSongIntent.putExtra("nextSong", song);
        // Necessary flag for broadcast to not be delayed.
        nextSongIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(nextSongIntent);
    }

    private void informFragmentOfMusicEnd() {
        // Send broadcast that the queue has ended.
        Intent musicEndIntent = new Intent(MUSIC_ENDED);
        // Necessary flag for broadcast to not be delayed.
        musicEndIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(musicEndIntent);
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

}
