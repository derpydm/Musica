package sg.edu.tp.seanwong.musica.ui.playlist_creation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

public class PlaylistCreationActivity extends AppCompatActivity {
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
                // Go back to playlist, store the playlist name for easier loading
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_creation);
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
}