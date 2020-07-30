package sg.edu.tp.seanwong.musica.ui.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerControlView;

import java.util.ArrayList;

import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicFragment extends Fragment implements MusicAdapter.OnUpdateListener {
    public static MusicFragment newInstance() {
        return new MusicFragment();
    }
    public static final int EXTERNAL_STORAGE_REQUEST = 1;
    RecyclerView rv;
    TextView popupTitle;
    TextView popupArtist;
    ImageView popupAlbumArt;
    PlayerControlView playOrPauseButton;
    MusicAdapter musicAdapter;
    ArrayList<Song> songs = new ArrayList<>();
    private boolean isBound = false;
    MusicService musicService;
    public boolean hasPermission() {
        // Check for external storage write perms
        if ((ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        } else {
            return false;
        }
    }

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

    private void initPlayer() {
        final SimpleExoPlayer player = musicService.getplayerInstance();
        playOrPauseButton.setPlayer(player);
        Player.EventListener el = new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                // Get new song, update popup text
                int currentIndex = player.getCurrentWindowIndex();
                Song currentSong = musicAdapter.getmSongs().get(currentIndex);
                updatePopupText(currentSong);
            }
        };
        player.addListener(el);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music, container, false);

        // Start music service and initialise notification for persistence
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_INIT);
        getContext().startService(intent);
        getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // Get references to views
        rv = root.findViewById(R.id.musicRecyclerView);
        popupAlbumArt = root.findViewById(R.id.popupAlbumImage);
        popupArtist = root.findViewById(R.id.popupArtist);
        popupTitle = root.findViewById(R.id.popupTitle);
        playOrPauseButton = root.findViewById(R.id.now_playing_playerview);
        playOrPauseButton.setShowTimeoutMs(0);

        // Register touch listener for button
        playOrPauseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Play/pause only when user releases the button
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    Log.d("music player", "button touch event triggered!");
                    SimpleExoPlayer player = musicService.getplayerInstance();
                    ImageButton bt = (ImageButton) v;
                }
                return false;
            }
        });

        // Check for permissions
        if (hasPermission()) {
            // Load songs if permissions are available
            songs = Song.getAllAudioFromDevice(getContext());
            setupRecyclerView(songs);
        } else {
            // Ask for external fs r/w and foreground service permission (if needed)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.FOREGROUND_SERVICE}, MusicFragment.EXTERNAL_STORAGE_REQUEST);
        }

        return root;
    }

    public void setupRecyclerView(ArrayList<Song> songList) {
        // Create adapter using songs, set adapter
        musicAdapter = new MusicAdapter(songList, getContext(), this);
        rv.setAdapter(musicAdapter);
        // Set layout manager
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // OVERRIDES
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == MusicFragment.EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions were granted
                Toast.makeText(getActivity(),
                        "External storage permissions granted",
                        Toast.LENGTH_SHORT)
                        .show();
                songs = Song.getAllAudioFromDevice(getContext());
                setupRecyclerView(songs);
                TextView missingLibraryView = getView().findViewById(R.id.musicSongMissingText);
                missingLibraryView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Show message in the event of missing library or no permission
        if (!hasPermission()) {
            TextView missingLibraryView = getView().findViewById(R.id.musicSongMissingText);
            missingLibraryView.setVisibility(View.VISIBLE);
        } else {
            TextView missingLibraryView = getView().findViewById(R.id.musicSongMissingText);
            missingLibraryView.setVisibility(View.INVISIBLE);
        }

    }

    public void updatePopupText(Song song) {
        if (song != null) {
            Uri artworkUri = Uri.parse("content://media/external/audio/media/" + song.getAlbumId() + "/albumart");
            RequestBuilder<Drawable> requestBuilder = Glide.with(popupAlbumArt).load(artworkUri);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .override(100,100)
                    .placeholder(R.drawable.ic_album_24px)
                    .error(R.drawable.ic_album_24px)
                    .priority(Priority.HIGH);
            requestBuilder
                    .load(artworkUri)
                    .apply(options)
                    .into(popupAlbumArt);
            popupArtist.setText(song.getArtist());
            popupTitle.setText(song.getTitle());
        }
    }






}
