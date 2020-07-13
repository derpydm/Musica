package sg.edu.tp.seanwong.musica.ui.music;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    Context context;
    MediaPlayer mp;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView musicSongTitle;
        public TextView musicSongArtist;
        public ImageView musicSongImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            musicSongArtist = itemView.findViewById(R.id.musicSongArtist);
            musicSongImage = itemView.findViewById(R.id.musicSongImage);
            musicSongTitle = itemView.findViewById(R.id.musicSongTitle);

        }
    }

    // Store a member variable for the songs
    // This is a reference to ALL songs
    private List<Song> mSongs;

    // Variable for currently queued songs
    private List<Song> queue;

    // Index of current queue progress
    private int currentIndex = 0;
    // Pass in the contact array into the constructor
    public MusicAdapter(List<Song> songs, Context context) {
        mSongs = songs;
        this.context = context;
    }

    // Grab ViewHolder responsible for Views
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View musicView = inflater.inflate(R.layout.musiclayout, parent, false);
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(musicView);
        return viewHolder;
    }

    // Set up each recycled view with the proper info
    @Override
    public void onBindViewHolder(MusicAdapter.ViewHolder holder, final int position) {
        // Get the data model based on position
        final Song song = mSongs.get(position);
        // Set item views based on your views and data model
        TextView title = holder.musicSongTitle;
        TextView artist = holder.musicSongArtist;
        ImageView image = holder.musicSongImage;
        Uri artworkUri = Uri.parse("content://media/external/audio/media/" + song.getAlbumId() + "/albumart");
        RequestBuilder<Drawable> requestBuilder = Glide.with(image).load(artworkUri);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .override(100,100)
                .placeholder(R.drawable.ic_album_24px)
                .error(R.drawable.ic_album_24px)
                .priority(Priority.HIGH);
        requestBuilder
                .load(artworkUri)
                .apply(options)
                .into(image);
        artist.setText(song.getArtist());
        title.setText(song.getTitle());
        // Set click callback
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = 0;
                playMusic(song, position);
            }
        });
    }
    // TODO: Migrate to a Service
    private void playMusicWithoutPartitioning() {
        // Does not touch queue, plays next song based on currentIndex
        // Called on assumption that next song in queue exists.
        Song song = queue.get(currentIndex);

        try {
            mp.setDataSource(song.getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "An error occured trying to play that track",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        if (!mp.isPlaying()) {
            mp.start();
        }
        Log.d("MusicPlayer", song.getTitle() + "is now playing");
    }
    // TODO: Migrate this to a Service so that we can play music in the background
    private void playMusic(Song song, final int position) {
        // Add every song besides the ones before it
        queue = mSongs.subList(position, mSongs.size() - 1);

        // Play the music here, regardless of whether any music was playing beforehand
        // Stop any other tracks beforehand

        if (mp != null) {
            mp.stop();
            mp.reset();
        } else {
            mp = new MediaPlayer();
        }
        try {
            mp.setDataSource(song.getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,
                    "An error occured trying to play that track",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        if (!mp.isPlaying()) {
            mp.start();
        }
        Log.d("MusicPlayer", song.getTitle() + "is now playing");
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.reset();
                // Check if the queue contains a new song
                // If so we play it

                if (!(position + 1 == queue.size())) {
                    currentIndex += 1;
                    mp.reset();
                    playMusicWithoutPartitioning();
                } else {
                    mp.reset();
                }
            }
        });
    }

    // TODO: Migrate to a Service
    private void playOrPauseMusic(Song song) {
        // Play or pause the music. Depends on view state, if music is playing we pause it.
        // If it's not we trust that it's not null and play the music.
    }
    // Get total list length
    @Override
    public int getItemCount() {
        return mSongs.size();
    }
}
