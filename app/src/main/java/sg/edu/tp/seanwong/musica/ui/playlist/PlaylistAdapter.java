package sg.edu.tp.seanwong.musica.ui.playlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Playlist;
import sg.edu.tp.seanwong.musica.util.Song;

public class PlaylistAdapter extends RecyclerView.Adapter<sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.ViewHolder> {
    Context context;
    private sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.OnUpdateListener listener;

    // TODO implement playlist deletion on long press
    public interface OnUpdateListener {
        void updatePopupText(Song song);
    }

    public ArrayList<Playlist> getmPlaylists() {
        return mPlaylists;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView musicSongTitle;
        public ImageView musicSongImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            musicSongImage = itemView.findViewById(R.id.playlist_musicSongImage);
            musicSongTitle = itemView.findViewById(R.id.playlistTitle);

        }
    }

    // Store a member variable for the songs
    // This is a reference to ALL songs
    private ArrayList<Playlist> mPlaylists;
    // Index of current queue progress
    private int currentIndex = 0;
    // Pass in the contact array into the constructor
    public PlaylistAdapter(ArrayList<Playlist> playlists, Context context, sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.OnUpdateListener listener) {
        mPlaylists = playlists;
        this.listener = listener;
        this.context = context;
    }

    // Grab ViewHolder responsible for Views
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View musicView = inflater.inflate(R.layout.playlistlayout, parent, false);
        // Return a new holder instance
        PlaylistAdapter.ViewHolder viewHolder = new PlaylistAdapter.ViewHolder(musicView);
        return viewHolder;
    }



    // Set up each recycled view with the proper info
    @Override
    public void onBindViewHolder(sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.ViewHolder holder, final int position) {
        // Get the playlist based on position
        final Playlist playlist = mPlaylists.get(position);
        // Set info
        TextView title = holder.musicSongTitle;
        ImageView image = holder.musicSongImage;
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_album_24px)
                // Means there's no album art, use default album icon
                .error(R.drawable.ic_album_24px)
                .fitCenter();


        // Init metadata retriever to get album art in bytes
        MediaMetadataRetriever metaData = new MediaMetadataRetriever();

        // For the purposes of simplicity for each playlist the image will be the cover art of the first song
        metaData.setDataSource(context, Uri.parse(playlist.getSongs().get(0).getPath()));
        // Encode the artwork into a byte array and then use BitmapFactory to turn it into a Bitmap to load
        byte art[] = metaData.getEmbeddedPicture();
        if (art != null) {
            // Album art exists, we grab the artwork
            Bitmap img = BitmapFactory.decodeByteArray(art,0,art.length);
            if (context != null) {
                Glide.with(context)
                        .load(img)
                        .apply(options)
                        .into(image);
            }

        } else {
            if (context != null) {
                Glide.with(context)
                        .load(R.drawable.ic_album_24px)
                        .apply(options)
                        .into(image);
            }

        }
        title.setText(playlist.getName());
        // Set click callback
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPlaylist(position);
            }
        });
    }
    // Start service to play music and queue all following tracks
    private void playPlaylist(final int position) {
        // Add every song in the playlist
        Intent intent = new Intent(context, MusicService.class);
        ArrayList<Song> q = mPlaylists.get(position).getSongs();
        intent.putParcelableArrayListExtra("queue", q);
        intent.putExtra("currentIndex", 0);
        listener.updatePopupText(mPlaylists.get(position).getSongs().get(0));
        intent.setAction(MusicService.ACTION_START_PLAY);
        context.startService(intent);
    }

    // Get total list length
    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }
}
