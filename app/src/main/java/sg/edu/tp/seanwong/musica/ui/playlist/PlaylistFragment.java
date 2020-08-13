package sg.edu.tp.seanwong.musica.ui.playlist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.Edits;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerControlView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.ui.playlist_creation.PlaylistCreationActivity;
import sg.edu.tp.seanwong.musica.util.Playlist;
import sg.edu.tp.seanwong.musica.util.Song;

public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnUpdateListener {
    public static sg.edu.tp.seanwong.musica.ui.music.MusicFragment newInstance() {
        return new sg.edu.tp.seanwong.musica.ui.music.MusicFragment();
    }
    RecyclerView rv;
    TextView popupTitle;
    TextView popupArtist;
    ImageView popupAlbumArt;
    PlayerControlView playOrPauseButton;
    PlaylistAdapter playlistAdapter;
    ImageButton addPlaylistButton;
    private boolean isBound = false;
    MusicService musicService;
    SimpleExoPlayer player;

    // TODO update adapter once new playlist is created
    // TODO actually implement playlist addition transition

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
        player = musicService.getplayerInstance();
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
                if (playlistAdapter != null) {
                    // Get new song, update popup text
                    Song currentSong = musicService.getCurrentSong();
                    updatePopupText(currentSong);
                }
            }
        };
        player.addListener(el);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_playlists, container, false);

        // Bind to music service if possible
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_BIND);
        getContext().startService(intent);
        getContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // Get references to views
        rv = root.findViewById(R.id.playlistRecyclerView);
        popupAlbumArt = root.findViewById(R.id.playlist_popupAlbumImage);
        popupArtist = root.findViewById(R.id.playlist_popupArtist);
        popupTitle = root.findViewById(R.id.playlist_popupTitle);
        playOrPauseButton = root.findViewById(R.id.playlist_now_playing_playerview);
        TextView missingText = root.findViewById(R.id.playlist_missingPlaylistText);
        addPlaylistButton = root.findViewById(R.id.addPlaylistButton);
        playOrPauseButton.setShowTimeoutMs(0);
        // Register touch listener for play/pause button
        playOrPauseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Play/pause only when user releases the button
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    Log.d("music player", "button touch event triggered!");
                    SimpleExoPlayer player = musicService.getplayerInstance();
                    playOrPauseButton.setPlayer(player);
                    return true;
                }
                return false;
            }
        });

        // Register touch listener for add playlist button
        addPlaylistButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    Intent intent = new Intent(getContext(), PlaylistCreationActivity.class);
                    startActivityForResult(intent, 12345);
                    return true;
                }
                return false;
            }
        });

        // Attempt to load playlist names
        ArrayList<String> playlistNames = new ArrayList<>();
        try {
            File origPlaylists = new File(getContext().getExternalFilesDir(null), "playlists");
            FileInputStream fileInputStream = new FileInputStream(origPlaylists);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            playlistNames = (ArrayList<String>) objectInputStream.readObject();
            objectInputStream.close();
            // If there's no playlists loaded in we set the text to be visible
            // This is because master playlists file will exist even if there are no playlists
            if (playlistNames.size() > 0) {
                missingText.setVisibility(View.INVISIBLE);
            } else {
                missingText.setVisibility(View.VISIBLE);
            }
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            // No playlists were found
            e.printStackTrace();
            missingText.setVisibility(View.VISIBLE);
        }

        // Load the playlists
        ArrayList<Playlist> playlists = new ArrayList<>();
        for (String playlistName: playlistNames) {
            Playlist playlist = Playlist.readFromFile(getContext(), playlistName);
            if (playlist != null) {
                playlists.add(playlist);
            }
        }
        setupRecyclerView(playlists);
        return root;
    }


    public void setupRecyclerView(ArrayList<Playlist> playlists) {
        // Get list
        // Create adapter using songs, set adapter
        playlistAdapter = new PlaylistAdapter(playlists, getContext(), this);
        rv.setAdapter(playlistAdapter);
        // Set layout manager
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12345 && resultCode == Activity.RESULT_OK) {
            // Playlist created successfully, load new playlist using name from Intent
            String newPlaylistName = data.getStringExtra("playlistName");
            Playlist newPlaylist = Playlist.readFromFile(getContext(), newPlaylistName);
            ArrayList<Playlist> playlists = playlistAdapter.getmPlaylists();
            playlists.add(newPlaylist);
            playlistAdapter.setmPlaylists(playlists);
            playlistAdapter.notifyDataSetChanged();
            if (playlists.size() > 0) {
                TextView missingText = getActivity().findViewById(R.id.playlist_missingPlaylistText);
                missingText.setVisibility(View.INVISIBLE);
            }
        }
    }

    // OVERRIDES
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(playlistAdapter != null) {
            playlistAdapter = null;
            rv.setAdapter(null);
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void updatePopupText(Song song) {
        if (song != null) {
            // Init metadata retriever to get album art in bytes
            MediaMetadataRetriever metaData = new MediaMetadataRetriever();
            metaData.setDataSource(getContext(), Uri.parse(song.getPath()));
            // Set up options
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_album_24px)
                    // Means there's no album art, use default album icon
                    .error(R.drawable.ic_album_24px)
                    .fitCenter();
            // Encode the artwork into a byte array and then use BitmapFactory to turn it into a Bitmap to load
            byte art[] = metaData.getEmbeddedPicture();
            if (art != null) {
                // Album art exists, we grab the artwork
                Bitmap img = BitmapFactory.decodeByteArray(art,0,art.length);
                if (getContext() != null) {
                    Glide.with(getContext())
                            .load(img)
                            .apply(options)
                            .into(popupAlbumArt);
                }
            } // We don't change the artwork so the default image doesn't get converted into a bitmap and decreases in quality
            popupArtist.setText(song.getArtist());
            popupTitle.setText(song.getTitle());
        }
    }

    // Make the missing text pop up
    public void makeMissingTextVisible() {
        TextView missingText = getActivity().findViewById(R.id.playlist_missingPlaylistText);
        missingText.setVisibility(View.VISIBLE);
    }
}
