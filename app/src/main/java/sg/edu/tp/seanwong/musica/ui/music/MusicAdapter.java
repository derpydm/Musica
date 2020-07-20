package sg.edu.tp.seanwong.musica.ui.music;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    Context context;
    private OnUpdateListener listener;
    public interface OnUpdateListener {
        void updatePopupText(Song song);
        void updatePlayButtonState(boolean isPlaying);
        void setMusicStartPlaying();
    }
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
    private ArrayList<Song> mSongs;

    // Variable for currently queued songs
    private ArrayList<Song> queue;
    // Index of current queue progress
    private int currentIndex = 0;
    // Pass in the contact array into the constructor
    public MusicAdapter(ArrayList<Song> songs, Context context, OnUpdateListener listener) {
        mSongs = songs;
        this.listener = listener;
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
                playMusic(position);
            }
        });
    }
    // Start service to play music and queue all following tracks
    private void playMusic(final int position) {
        // Add every song besides the ones before it
        queue = new ArrayList<>();
        queue.addAll(0, mSongs.subList(position, mSongs.size()));
        listener.setMusicStartPlaying();
        Intent intent = new Intent(context, MusicService.class);
        ArrayList<Song> q = new ArrayList<>(queue);
        Log.d("Songs queue", queue.toString());
        intent.putParcelableArrayListExtra("queue", q);
        intent.setAction(MusicService.ACTION_START_PLAY);
        listener.updatePopupText(queue.get(0));
        context.startService(intent);
    }

    // Get total list length
    @Override
    public int getItemCount() {
        return mSongs.size();
    }
}
