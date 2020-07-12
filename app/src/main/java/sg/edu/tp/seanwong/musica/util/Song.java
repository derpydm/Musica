package sg.edu.tp.seanwong.musica.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Song {
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

    public String getFormattedTime() {
        long minutes = (long) ((length / 1000) / 60);
        long seconds = (long) ((length / 1000) % 60);
        return (String.format("%d", minutes) + ":" + String.format("%d", seconds));
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
    public static final List<Song> getAllAudioFromDevice(final Context context) {
        final List<Song> tempAudioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.MediaColumns.DURATION, MediaStore.Audio.AudioColumns.ALBUM_ID};
        Cursor c = context.getContentResolver().query(uri, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0 ", null , MediaStore.Audio.AudioColumns.TITLE + " ASC");

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
        } else {
            Log.e("Song finder", "No song found");
        }

        return tempAudioList;
    }
}


