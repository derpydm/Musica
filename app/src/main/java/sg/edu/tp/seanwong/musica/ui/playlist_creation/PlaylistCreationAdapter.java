package sg.edu.tp.seanwong.musica.ui.playlist_creation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class PlaylistCreationAdapter extends RecyclerView.Adapter<PlaylistCreationAdapter.ViewHolder> {

    ArrayList<Song> mSongs;
    ArrayList<Song> selectedSongs = new ArrayList<>();
    Context context;

    public ArrayList<Song> getSelectedSongs() {
        return selectedSongs;
    }

    @NonNull
    @Override
    public PlaylistCreationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View musicView = inflater.inflate(R.layout.musiclayout, parent, false);
        // Return a new holder instance
        PlaylistCreationAdapter.ViewHolder viewHolder = new PlaylistCreationAdapter.ViewHolder(musicView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PlaylistCreationAdapter.ViewHolder holder, final int position) {
        // Get the song based on position
        final Song song = mSongs.get(position);
        // Set item views based on your views and data model
        TextView title = holder.musicSongTitle;
        TextView artist = holder.musicSongArtist;
        ImageView image = holder.musicSongImage;

        // Init metadata retriever to get album art in bytes
        MediaMetadataRetriever metaData = new MediaMetadataRetriever();
        metaData.setDataSource(context, Uri.parse(song.getPath()));

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
            Glide.with(context)
                    .load(img)
                    .apply(options)
                    .into(image);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_album_24px)
                    .apply(options)
                    .into(image);
        }
        artist.setText(song.getArtist());
        title.setText(song.getTitle());
        // Set click callback
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @ColorInt int highlighted = Color.parseColor("#ffaaaaaa");
                // If the song's already selected then remove it, if it's not then add it to the selected songs list
                if (selectedSongs.contains(mSongs.get(position))) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
                    selectedSongs.remove(mSongs.get(position));
                } else {
                    holder.itemView.setBackgroundColor(highlighted);
                    selectedSongs.add(mSongs.get(position));
                }
            }
        });
    }

    PlaylistCreationAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.mSongs = songs;
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
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


}
