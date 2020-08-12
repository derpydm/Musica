package sg.edu.tp.seanwong.musica.ui.playlist_creation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

class PlaylistCreationActivity extends AppCompatActivity {
    RecyclerView rv;
    TextView missingText;
    PlaylistCreationAdapter playlistCreationAdapter;

    public void setupRecyclerView(ArrayList<Song> songList) {
        // Create adapter using songs, set adapter
        playlistCreationAdapter = new PlaylistCreationAdapter(this, songList);
        rv.setAdapter(playlistCreationAdapter);

        // Set layout manager
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean hasPermission() {
        // Check for external storage write perms
        if ((ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        } else {
            return false;
        }
    }

    // TODO handle playlist saving using inbuilt methods
    // TODO make popup for handling naming the playlist
    private void savePlaylist() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rv = findViewById(R.id.playlistCreationRecyclerView);
        missingText = findViewById(R.id.playlistCreationSongMissingText);

        // If no perms or no songs found we enable the missing song text
        // For UX reasons we will not request for permissions here.
        if (!hasPermission()) {
            missingText.setVisibility(View.VISIBLE);
        } else {
            ArrayList<Song> songs = Song.getAllAudioFromDevice(this);
            if (songs.size() > 0) {
                missingText.setVisibility(View.INVISIBLE);
                setupRecyclerView(songs);
            } else {
                missingText.setVisibility(View.VISIBLE);
            }
        }
    }
}