package sg.edu.tp.seanwong.musica.ui.playlist_creation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class PlaylistCreationAdapter extends RecyclerView.Adapter<PlaylistCreationAdapter.ViewHolder> implements Filterable {

    ArrayList<Song> mSongs;
    ArrayList<Song> filteredSongs;
    ArrayList<Song> selectedSongs = new ArrayList<>();
    Context context;
    OnUpdateListener listener;

    // Implement listener so that we can send changes to fragment
    public interface OnUpdateListener {
        void updateTitleWithSearch(String search);
    }

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
        return new PlaylistCreationAdapter.ViewHolder(musicView);
    }

    @Override
    public void onBindViewHolder(final PlaylistCreationAdapter.ViewHolder holder, final int position) {
        // Get the song based on position
        final Song song = filteredSongs.get(position);
        // Set item views based on your views and data model
        TextView title = holder.musicSongTitle;
        TextView artist = holder.musicSongArtist;
        ImageView image = holder.musicSongImage;

        // Init metadata retriever to get album art in bytes
        MediaMetadataRetriever metaData = new MediaMetadataRetriever();
        metaData.setDataSource(song.getPath());

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
        } // Use standard vector srcCompat for default image if the art can't be found
        artist.setText(song.getArtist());
        title.setText(song.getTitle());

        // Highlight row if song is selected
        // We need to redo this as the adapter refresh doesn't rehighlight the rows
        if (selectedSongs.contains(song)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#ffaaaaaa"));
        } else {
            // Unhighlight the song
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffffff"));
        }
        // Set click callback
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the song's already selected then remove it, if it's not then add it to the selected songs list
                if (selectedSongs.contains(filteredSongs.get(position))) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
                    selectedSongs.remove(filteredSongs.get(position));
                } else {
                    holder.itemView.setBackgroundColor(Color.parseColor("#ffaaaaaa"));
                    selectedSongs.add(filteredSongs.get(position));
                }
            }
        });
    }

    PlaylistCreationAdapter(Context context, ArrayList<Song> songs, OnUpdateListener listener) {
        this.listener = listener;
        this.context = context;
        this.mSongs = songs;
        // Init as new arraylist to avoid them pointing to the same reference
        this.filteredSongs = new ArrayList<>(songs);
    }

    @Override
    public int getItemCount() {
        return filteredSongs.size();
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

    // Implement filter for searching songs
    // We also introduce some
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                filteredSongs.clear();
                Log.d("msongs on query", mSongs.toString());
                ArrayList<Song> searchResults;
                if (charString.isEmpty()) {
                    searchResults = new ArrayList<>(mSongs);
                } else {
                    ArrayList<Song> filteredList = new ArrayList<>();
                    for (Song row: mSongs) {
                        Log.d("Filtering song:", row.toString());
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) || row.getArtist().toLowerCase().contains(charString.toLowerCase()) || row.getAlbum().toLowerCase().contains(charString.toLowerCase())) {
                            Log.d("Row matched:", row.toString());
                            filteredList.add(row);
                        }
                    }
                    searchResults = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.count = searchResults.size();
                filterResults.values = searchResults;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredSongs = (ArrayList<Song>) filterResults.values;
                Log.d("filtered songs", filteredSongs.toString());
                listener.updateTitleWithSearch(charSequence.toString());
                notifyDataSetChanged();
            }
        };
    }

}
