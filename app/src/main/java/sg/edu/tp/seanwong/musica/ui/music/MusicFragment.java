package sg.edu.tp.seanwong.musica.ui.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerControlView;

import java.util.ArrayList;

import sg.edu.tp.seanwong.musica.MainActivity;
import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicFragment extends Fragment implements MusicAdapter.OnUpdateListener {
    public static final int EXTERNAL_STORAGE_REQUEST = 387;
    RecyclerView rv;
    TextView popupTitle;
    TextView popupArtist;
    ImageView popupAlbumArt;
    SearchView searchView;
    PlayerControlView playOrPauseButton;
    MusicAdapter musicAdapter;
    ArrayList<Song> songs = new ArrayList<>();
    private boolean isBound = false;
    MusicService musicService;

    public boolean hasPermission() {
        // Check for external storage read/write perms
        return ((ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
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
            public void onPositionDiscontinuity(int reason) {
                if (musicAdapter != null) {
                    // Get new song, update popup text
                    int currentIndex = player.getCurrentWindowIndex();
                    Song currentSong = musicAdapter.getmSongs().get(currentIndex);
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
        View root = inflater.inflate(R.layout.fragment_music, container, false);

        // Start music service with action to ready notification for persistence
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

        // Check for permissions
        if (hasPermission()) {
            // Load songs if permissions are available
            songs = Song.getAllAudioFromDevice(getContext());
            setupRecyclerView(songs);
        } else {
            // Ask for external fs r/w and foreground service permission (if needed)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_EXTERNAL_STORAGE}, MusicFragment.EXTERNAL_STORAGE_REQUEST);
        }

        // Retain view instance in memory so that we don't have problems with fragment being unattached but the adapter is destroyed

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    // Unbind from service to prevent memory leaks
    @Override
    public void onDestroy() {
        if(musicAdapter != null) {
            musicAdapter = null;
            rv.setAdapter(null);
        }
        if (isBound) {
            getContext().unbindService(connection);
        }
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == MusicFragment.EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions were granted, show toast
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        searchView = (SearchView) menu.findItem(R.id.search_action).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                musicAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                musicAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.search_action) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void updatePopupText(Song song) {
        if (song != null) {
            // Init metadata retriever to get album art in bytes
            MediaMetadataRetriever metaData = new MediaMetadataRetriever();
            metaData.setDataSource(song.getPath());
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
            } else {
                // This may be called when the listener is still registered but context will be null because it isn't on screen
                // Null check for context, same as above
                if (getContext() != null) {
                    popupAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_album_24px, getActivity().getTheme()));
                }
            }
            popupArtist.setText(song.getArtist());
            popupTitle.setText(song.getTitle());
        }
    }

    public void updateTitleWithSearch(String search) {
        MainActivity ac = (MainActivity) getActivity();
        if (!search.isEmpty()) {
            ac.getSupportActionBar().setTitle("Search: " + search);
        } else {
            ac.getSupportActionBar().setTitle("Music");
        }

    }
}
