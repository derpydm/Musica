package sg.edu.tp.seanwong.musica.ui.music;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
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

import org.w3c.dom.Text;

import java.util.List;

import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
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

    // Store a member variable for the contacts
    private List<Song> mSongs;

    // Pass in the contact array into the constructor
    public MusicAdapter(List<Song> songs) {
        mSongs = songs;
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
    public void onBindViewHolder(MusicAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        Song song = mSongs.get(position);
        // Set item views based on your views and data model
        TextView title = holder.musicSongTitle;
        TextView artist = holder.musicSongArtist;
        ImageView image = holder.musicSongImage;
        Uri artworkUri = Uri.parse("content://media/external/audio/media/" + song.getAlbumId() + "/albumart");
        RequestBuilder<Drawable> requestBuilder = Glide.with(image).load(artworkUri);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .override(100,100)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_playlist_add_24px)
                .priority(Priority.HIGH);
        requestBuilder
                .load(artworkUri)
                .apply(options)
                .into(image);
        artist.setText(song.getArtist());
        title.setText(song.getTitle());
    }

    // Get total list length
    @Override
    public int getItemCount() {
        return mSongs.size();
    }
}
