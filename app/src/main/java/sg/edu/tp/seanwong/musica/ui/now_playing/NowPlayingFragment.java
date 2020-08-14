package sg.edu.tp.seanwong.musica.ui.now_playing;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.RepeatModeUtil;
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
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    // Disable search
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    // Unbind from service to prevent memory leaks
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            getContext().unbindService(connection);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Clear the menu as we don't want to enable search
        menu.clear();
    }

    private void initPlayer() {
        final SimpleExoPlayer player = musicService.getplayerInstance();
        pv.setPlayer(player);
        pv.setShowTimeoutMs(0);
        pv.setShowShuffleButton(true);
        pv.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL | RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPositionDiscontinuity(int reason) {
                // Get new song, update popup text
                if (reason != Player.DISCONTINUITY_REASON_SEEK) {
                    int currentIndex = player.getCurrentWindowIndex();
                    Song currentSong = musicService.getQueue().get(currentIndex);
                    updatePopupText(currentSong);
                }
            }
        });
        }

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
            // Init metadata retriever to get album art in bytes
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
                if (getContext() != null) {
                    Glide.with(getContext())
                            .load(img)
                            .apply(options)
                            .into(albumArtView);
                }
            } else {
                // This may be called when the listener is still registered but context will be null because it isn't on screen
                // Null check for context, same as above
                if (getContext() != null) {
                    albumArtView.setImageDrawable(getResources().getDrawable(R.drawable.ic_album_24px, getContext().getTheme()));
                }
            }
            albumView.setText(song.getAlbum());
            artistView.setText(song.getArtist());
            songTitleView.setText(song.getTitle());
        }
    }
}
