package sg.edu.tp.seanwong.musica.ui.now_playing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class NowPlayingFragment extends Fragment {
    PlayerView pv;
    MusicService musicService;
    boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // After binding to service, we attach our views and listeners to the player
            MusicService.ServiceBinder binder = (MusicService.ServiceBinder) iBinder;
            musicService = binder.getService();
            isBound = true;
            initPlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    // TODO get this damn thing to not hide on start once music plays
    private void initPlayer() {
        final SimpleExoPlayer player = musicService.getplayerInstance();
        pv.setPlayer(player);
        pv.setControllerShowTimeoutMs(0);
        pv.setControllerHideOnTouch(false);
        pv.setControllerAutoShow(true);
        pv.setShowBuffering(false);
        pv.setUseArtwork(true);
        pv.setDefaultArtwork(getResources().getDrawable(R.drawable.ic_album_24px));
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
        setupBinding();
        return root;
    }
}
