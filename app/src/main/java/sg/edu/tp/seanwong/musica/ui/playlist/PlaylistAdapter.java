package sg.edu.tp.seanwong.musica.ui.playlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
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

import java.util.ArrayList;
import sg.edu.tp.seanwong.musica.MusicService;
import sg.edu.tp.seanwong.musica.R;
import sg.edu.tp.seanwong.musica.util.Playlist;
import sg.edu.tp.seanwong.musica.util.Song;

public class PlaylistAdapter extends RecyclerView.Adapter<sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.ViewHolder> implements Filterable {
    Context context;
    private sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.OnUpdateListener listener;
    String search = "";

    // Implement listener so that we can send changes to fragment
    public interface OnUpdateListener {
        void updatePopupText(Song song);
        void makeMissingTextVisible();
        void updateTitleWithSearch(String search);
    }

    public ArrayList<Playlist> getmPlaylists() {
        return mPlaylists;
    }

    public void setmPlaylists(ArrayList<Playlist> mPlaylists) {
        this.mPlaylists = mPlaylists;
        // Filter the playlists with the necessary search once playlist list is changed
        getFilter().filter(search);
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

    // Store a member variable for the playlists
    // This is a reference to ALL playlists
    private ArrayList<Playlist> mPlaylists;

    // Filtered playlists list
    private ArrayList<Playlist> filteredPlaylists;
    // Index of current queue progress
    private int currentIndex = 0;
    // Pass in the contact array into the constructor
    public PlaylistAdapter(ArrayList<Playlist> playlists, Context context, sg.edu.tp.seanwong.musica.ui.playlist.PlaylistAdapter.OnUpdateListener listener) {
        // Set to new instance as the array is passed by reference, and we need two seperate array lists
        filteredPlaylists = new ArrayList<>(playlists);
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
        final Playlist playlist = filteredPlaylists.get(position);
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
        metaData.setDataSource(playlist.getSongs().get(0).getPath()

        );
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
        } // Use standard vector srcCompat for default image if the art can't be found
        title.setText(playlist.getName());
        // Set click callback
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPlaylist(position);
            }
        });

        // Set delete popup on long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Playlist");
                builder.setMessage("Are you sure you want to delete this playlist?");
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePlaylist(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            }
        });

    }

    // Start service to play music and queue all tracks in playlist
    private void playPlaylist(final int position) {
        // Add every song in the playlist
        Intent intent = new Intent(context, MusicService.class);
        Playlist selectedPlaylist = filteredPlaylists.get(position);
        ArrayList<Song> queue = selectedPlaylist.getSongs();
        intent.putParcelableArrayListExtra("queue", queue);
        intent.putExtra("currentIndex", 0);
        listener.updatePopupText(queue.get(0));
        intent.setAction(MusicService.ACTION_START_PLAY);
        context.startService(intent);
    }

    // Delete playlist
    private void deletePlaylist(final int position) {
        Playlist deletedPlaylist = filteredPlaylists.get(position);
        Playlist.deletePlaylistFile(context, deletedPlaylist.getName());
        filteredPlaylists.remove(position);
        mPlaylists.remove(deletedPlaylist);
        this.notifyDataSetChanged();
        if (mPlaylists.size() == 0) {
            listener.makeMissingTextVisible();
        }
    }

    // Get total list length
    @Override
    public int getItemCount() {
        return filteredPlaylists.size();
    }

    // Implement filter for searching songs
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                search = charString;
                filteredPlaylists.clear();
                ArrayList<Playlist> searchResults;
                if (charString.isEmpty()) {
                    searchResults = new ArrayList<>(mPlaylists);
                } else {
                    ArrayList<Playlist> filteredList = new ArrayList<>();
                    for (Playlist row: mPlaylists) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for title match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
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
                filteredPlaylists = (ArrayList<Playlist>) filterResults.values;
                listener.updateTitleWithSearch(charSequence.toString());
                notifyDataSetChanged();
            }
        };
    }
}
