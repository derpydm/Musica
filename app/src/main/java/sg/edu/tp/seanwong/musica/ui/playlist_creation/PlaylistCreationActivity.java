package sg.edu.tp.seanwong.musica.ui.playlist_creation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Playlist;
import sg.edu.tp.seanwong.musica.util.Song;

public class PlaylistCreationActivity extends AppCompatActivity implements PlaylistCreationAdapter.OnUpdateListener {
    RecyclerView rv;
    TextView missingText;
    SearchView searchView;
    PlaylistCreationAdapter playlistCreationAdapter;

    public void setupRecyclerView(ArrayList<Song> songList) {
        // Create adapter using songs, set adapter
        playlistCreationAdapter = new PlaylistCreationAdapter(this, songList, this);
        rv.setAdapter(playlistCreationAdapter);

        // Set layout manager
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean hasPermission() {
        // Check for external storage write perms
        if ((ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }
        return false;
    }

    public void savePlaylistButtonClicked(View view) {
        if (playlistCreationAdapter.getSelectedSongs().size() == 0) {
            Toast.makeText(this,"You can't make an empty playlist!", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Playlist Name");

        // Set up the text input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set context
        final Context ctx = this;
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (name.length() == 0) {
                    // User did not enter anything, show notice saying you can't do that and exit early
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(ctx);
                    builder2.setTitle("You can't have an empty name!");
                    builder2.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder2.show();
                    return;
                }

                // Save the playlist to file.
                Playlist playlist = new Playlist(name, playlistCreationAdapter.getSelectedSongs());
                playlist.saveToFile(ctx);
                // Go back to playlist, store the playlist name so that PlaylistFragment can load it in without reloading all playlists
                Intent returnIntent = new Intent();
                returnIntent.putExtra("playlistName", playlist.getName());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu UI
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        searchView = (SearchView) menu.findItem(R.id.search_action).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                playlistCreationAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                playlistCreationAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.search_action:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_creation);

        // Set action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Select Songs");
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

    public void updateTitleWithSearch(String search) {
        if (!search.isEmpty()) {
            setTitle("Search: " + search);
        } else {
            setTitle("Select Songs");
        }

    }
}