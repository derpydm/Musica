package sg.edu.tp.seanwong.musica.ui.music;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.List;

import sg.edu.tp.seanwong.musica.MainActivity;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicFragment extends Fragment {
    public static MusicFragment newInstance() {
        return new MusicFragment();
    }
    public static final int EXTERNAL_STORAGE_REQUEST = 1;
    RecyclerView rv;
    List<Song> songs = Collections.emptyList();
    public boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music, container, false);
        // Get reference to recycler view
        rv = root.findViewById(R.id.musicRecyclerView);
        if (hasPermission()) {
            // Load songs if permissions are available
            songs = Song.getAllAudioFromDevice(getContext());
            setupRecyclerView(songs);
        } else {
            // Ask for external fs r/w permission
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MusicFragment.EXTERNAL_STORAGE_REQUEST);
        }
        return root;
    }
    public void setupRecyclerView(List<Song> songList) {
        // Create adapter using songs, set adapter
        MusicAdapter mAdapter = new MusicAdapter(songList, getContext());
        rv.setAdapter(mAdapter);
        // Set layout manager
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == MusicFragment.EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Pop a toast to inform user
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

}
