package sg.edu.tp.seanwong.musica.ui.music;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Song;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> implements Filterable {
    Context context;
    private OnUpdateListener listener;



    public interface OnUpdateListener {
        void updatePopupText(Song song);
        void updateTitleWithSearch(String search);
    }

    public ArrayList<Song> getmSongs() {
        return mSongs;
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
    // Store member variable for filtered songs
    private ArrayList<Song> filteredSongs;

    // Pass in the contact array into the constructor
    public MusicAdapter(ArrayList<Song> songs, Context context, OnUpdateListener listener) {
        // Set to new instance as the array is passed by reference, and we need two seperate array lists
        filteredSongs = new ArrayList<>(songs);
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
        // Get the song based on position
        final Song song = filteredSongs.get(position);
        // Set item views based on your views and data model
        TextView title = holder.musicSongTitle;
        TextView artist = holder.musicSongArtist;
        ImageView image = holder.musicSongImage;
        Log.d("mSongs bind view", mSongs.toString());
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
        } // Use standard vector srcCompat for default image if the art can't be found
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
        int truePosition = mSongs.indexOf(filteredSongs.get(position));
        Intent intent = new Intent(context, MusicService.class);
        ArrayList<Song> q = new ArrayList<>(mSongs);
        intent.putParcelableArrayListExtra("queue", q);
        intent.putExtra("currentIndex", truePosition);
        listener.updatePopupText(filteredSongs.get(position));
        intent.setAction(MusicService.ACTION_START_PLAY);
        context.startService(intent);
    }

    // Get total list length
    @Override
    public int getItemCount() {
        return filteredSongs.size();
    }

    // Implement filter for searching songs
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
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
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
