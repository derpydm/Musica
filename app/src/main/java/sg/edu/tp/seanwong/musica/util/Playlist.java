package sg.edu.tp.seanwong.musica.util;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable, Parcelable {
    String name;
    ArrayList<Song> songs;
    public Playlist(String name, ArrayList<Song> songs) {
        this.name = name;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    // Serializes an object and saves it to a file
    public void saveToFile(Context context) {
        try {
            // If the data directory doesn't exist we make it
            if (!context.getExternalFilesDir(null).exists()) {
                context.getExternalFilesDir(null).mkdir();
            }

            // Write playlist object to a file
            File outputFile = new File(context.getExternalFilesDir(null), this.name + ".playlist");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();

            // Make playlist list aware a new playlist has been added
            // 1. Open input for playlists file so we can append the new playlist
            ArrayList<String> playlistNames = new ArrayList<>();
            File origPlaylists = new File(context.getExternalFilesDir(null), "playlists.bin");
            if (!origPlaylists.exists()) {
                // Don't read, just create the file
                origPlaylists.createNewFile();
                playlistNames.add(this.name);
            } else {
                // The file exists and is nonzero - load file and append
                FileInputStream fileInputStream = new FileInputStream(origPlaylists);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                playlistNames = (ArrayList<String>) objectInputStream.readObject();
                Log.d("Playlist names",playlistNames.toString());
                playlistNames.add(this.name);
                objectInputStream.close();
                fileInputStream.close();
            }

            // 2. Save the playlist names.
            FileOutputStream playlistListStream =  new FileOutputStream(origPlaylists);
            objectOutputStream = new ObjectOutputStream(playlistListStream);
            objectOutputStream.writeObject(playlistNames);
            playlistListStream.close();
            objectOutputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    // Creates an object by reading it from a file
    public static Playlist readFromFile(Context context, String playlistName) {
        Playlist playlist = null;
        File file = new File(context.getExternalFilesDir(null),playlistName + ".playlist");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            playlist = (Playlist) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return playlist;
    }


    public static void deletePlaylistFile(Context context, String playlistName) {
        // Delete the playlist if it exists
        File file = new File(context.getExternalFilesDir(null),playlistName);
        if (file.exists()) {
            file.delete();
        }
        // Load up master playlists file and delete the playlist
        ArrayList<String> playlistNames;
        File origPlaylists = new File(context.getExternalFilesDir(null), "playlists.bin");
        try {
            FileInputStream fileInputStream = new FileInputStream(origPlaylists);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            playlistNames = (ArrayList<String>) objectInputStream.readObject();
            Log.d("Playlist names", playlistNames.toString());
            playlistNames.remove(playlistName);
            objectInputStream.close();
            fileInputStream.close();

            // Save the playlist names after deletion.
            FileOutputStream playlistListStream =  new FileOutputStream(origPlaylists);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(playlistListStream);
            objectOutputStream.writeObject(playlistNames);
            playlistListStream.close();
            objectOutputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Write song to parcel to facilitate transfer through Intent
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeList(songs);

    }
    // Reconstruct object in the same order that we added them to parcel
    private Playlist(Parcel in) {
        this.name = in.readString();
        this.songs = in.readArrayList(null);
    }


}
