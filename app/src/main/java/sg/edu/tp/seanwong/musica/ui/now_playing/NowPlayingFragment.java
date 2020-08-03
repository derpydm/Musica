package sg.edu.tp.seanwong.musica.ui.now_playing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.RepeatModeUtil;

import java.util.ArrayList;

import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class NowPlayingFragment extends Fragment {
    PlayerControlView pv;
    MusicService musicService;
    boolean isBound = false;
    TextView artistView;
    TextView songTitleView;
    TextView albumView;
    ImageView albumArtView;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // After binding to service, we attach our views and listeners to the player
            MusicService.ServiceBinder binder = (MusicService.ServiceBinder) iBinder;
            musicService = binder.getService();
            isBound = true;
            initPlayer();
            Song currentSong = musicService.getCurrentSong();
            // Reupdate song info after regenerating the fragment
            // If currentSong is null that means the service was just initialised
            if (currentSong != null) {
                updatePopupText(musicService.getCurrentSong());
            };
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    private void initPlayer() {
        final SimpleExoPlayer player = musicService.getplayerInstance();
        pv.setPlayer(player);
        pv.setShowTimeoutMs(0);
        pv.setShowShuffleButton(true);
        pv.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL | RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE);
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                // Get new song, update popup text
                int currentIndex = player.getCurrentWindowIndex();
                Song currentSong = musicService.getQueue().get(currentIndex);
                updatePopupText(currentSong);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });
        };

    private void setupBinding() {
        // We send a Intent here with the purpose of binding to the currently running service.
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_BIND);
        getContext().startService(intent);
        getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_now_playing, container, false);
        pv = root.findViewById(R.id.now_playing_playerview);
        artistView = root.findViewById(R.id.now_playing_artist);
        songTitleView = root.findViewById(R.id.now_playing_song);
        albumView = root.findViewById(R.id.now_playing_album);
        albumArtView = root.findViewById(R.id.now_playing_album_art);
        setupBinding();
        return root;
    }

    public void updatePopupText(Song song) {
        if (song != null) {
            Uri artworkUri = Uri.parse("content://media/external/audio/media/" + song.getAlbumId() + "/albumart");
            RequestBuilder<Drawable> requestBuilder = Glide.with(albumArtView).load(artworkUri);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .override(100,100)
                    .placeholder(R.drawable.ic_album_24px)
                    .error(R.drawable.ic_album_24px)
                    .priority(Priority.HIGH);
            requestBuilder
                    .load(artworkUri)
                    .apply(options)
                    .into(albumArtView);
            artistView.setText(song.getArtist());
            songTitleView.setText(song.getTitle());
        }
    }
}
