package sg.edu.tp.seanwong.musica.util;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.exoplayer2.source.ShuffleOrder;

import java.util.ArrayList;

public class Song implements Parcelable {
    private String title;
    private String path;
    private long length;
    private String album;
    private String artist;
    private long albumId;



    public Song(String title, String path, long length, String album, String artist, long albumId) {
        this.title = title;
        this.path = path;
        this.length = length;
        this.album = album;
        this.artist = artist;
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumArt(long albumId) {
        this.albumId = albumId;
    }
    public static ArrayList<Song> getAllAudioFromDevice(final Context context) {
        final ArrayList<Song> tempAudioList = new ArrayList<>();
        Uri externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.MediaColumns.DURATION, MediaStore.Audio.AudioColumns.ALBUM_ID};
        Cursor c = context.getContentResolver().query(externalContentUri, projection, null, null, MediaStore.Audio.Media.TITLE + " ASC");
        if (c != null) {
            Log.e("Song finder", String.valueOf(c.getCount()));
            while (c.moveToNext()) {
                String path = c.getString(0);
                String name = c.getString(1);
                String album = c.getString(2);
                String artist = c.getString(3);
                String length = c.getString(4);
                long albumId = c.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);
                Log.e("Song found", name);
                Song song = new Song(name,path,Long.parseLong(length),album,artist,albumId);
                tempAudioList.add(song);
            }
            c.close();
        }

        return tempAudioList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Regenerate song from parcel
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    // Write song to parcel to facilitate transfer through Intent
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(path);
        out.writeString(album);
        out.writeString(artist);
        out.writeLong(length);
        out.writeLong(albumId);

    }
    // Reconstruct object in the same order that we added them to parcel
    private Song(Parcel in) {
        this.title = in.readString();
        this.path = in.readString();
        this.album = in.readString();
        this.artist = in.readString();
        this.length = in.readLong();
        this.albumId = in.readLong();
    }
}


