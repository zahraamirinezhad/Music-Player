package com.example.musicplayer.Music_Stuff

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import com.example.musicplayer.Activity.Favourite
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Activity.Playlist
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALL_LYRICS
import com.example.musicplayer.Music_Stuff.Constants.Companion.FAVOURITE_SONGS
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSIC_ADDRESS
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYLISTS
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC_CURRENT_POSITION
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC_CURRENT_POSITION_IN_SEEKBAR
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC_IS_PLAYING
import com.example.musicplayer.Music_Stuff.Constants.Companion.SAVED_INFO
import com.example.musicplayer.Music_Stuff.Constants.Companion.UNKNOWN
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.oxycblt.auxio.music.id3GenreName
import org.oxycblt.auxio.music.queryCursor
import org.oxycblt.auxio.music.useQuery
import java.io.File

class Data {
    companion object {
        var playingPlayList: ArrayList<Music> = ArrayList()
        var nowPlayingMusicID: String = UNKNOWN
        var nowPlayingMusicPositionInSeekBar: Int = -1
        var isCurrentMusicPlaying: Boolean? = null
        fun saveAllInfo(context: Context) {
            val editor =
                context.getSharedPreferences(SAVED_INFO, AppCompatActivity.MODE_PRIVATE).edit()
            val jsonString = GsonBuilder().create().toJson(Favourite.favoriteSongs)
            editor.putString(FAVOURITE_SONGS, jsonString)
            val jsonStringPlaylist = GsonBuilder().create().toJson(Playlist.listOfPlaylists)
            editor.putString(PLAYLISTS, jsonStringPlaylist)

            if (Player.musicListPA.size != 0 && Player.musicService != null && Player.musicService!!.mediaPlayer != null) {
                val jsonStringPlayerMusicList = GsonBuilder().create().toJson(Player.musicListPA)
                editor.putString(RECENT_MUSIC, jsonStringPlayerMusicList)
                editor.putString(
                    RECENT_MUSIC_CURRENT_POSITION,
                    Player.musicListPA[Player.songPosition].id
                )
                editor.putInt(RECENT_MUSIC_IS_PLAYING, if (Player.isPlaying) 1 else 0)
                editor.putInt(
                    RECENT_MUSIC_CURRENT_POSITION_IN_SEEKBAR,
                    Player.musicService!!.mediaPlayer!!.currentPosition
                )
            }
            for (music in MainActivity.MusicListMA) {
                if (music.lyrics != null && music.lyrics != "") MainActivity.allMusicsLyrics.put(
                    music.id,
                    music.lyrics!!
                )
            }
            val lyricsJsonString = GsonBuilder().create().toJson(MainActivity.allMusicsLyrics)
            editor.putString(ALL_LYRICS, lyricsJsonString)
            editor.apply()
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        fun getAllAudio(context: Context): ArrayList<Music> {
            MainActivity.allMusicsLyrics = LinkedHashMap()
            val editor = context.getSharedPreferences(SAVED_INFO, AppCompatActivity.MODE_PRIVATE)
            val jsonString = editor.getString(ALL_LYRICS, null)
            val typeToken = object : TypeToken<LinkedHashMap<String, String>>() {}.type
            if (jsonString != null) {
                MainActivity.allMusicsLyrics =
                    GsonBuilder().create().fromJson(jsonString, typeToken)

            }
            MainActivity.songByAlbum = LinkedHashMap()
            MainActivity.songByArtist = LinkedHashMap()
            val tempList = ArrayList<Music>()
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val projection = arrayOf(
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.DATE_ADDED,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.SIZE,
            )
            val cursor = context.contentResolver.queryCursor(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null
            )

            if (cursor != null) {
                if (cursor.moveToFirst())
                    do {
                        val titleC =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))
                                ?: UNKNOWN
                        val idC =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID))
                                .toString()
                        val albumC =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))
                                ?: UNKNOWN
                        val artistC =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))
                                ?: UNKNOWN
                        val dateC =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED))
                                ?: UNKNOWN
                        val sizeC =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE))
                        val pathC =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA))
                        val durationC =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))
                        val albumIdC =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
                                .toString()
                        val uri = Uri.parse(MUSIC_ADDRESS)
                        val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                        val lyricsC: String? =
                            if (MainActivity.allMusicsLyrics.containsKey(idC)) MainActivity.allMusicsLyrics[idC] else null
                        val music = Music(
                            id = idC,
                            title = titleC,
                            album = albumC,
                            artist = artistC,
                            path = pathC,
                            duration = durationC,
                            artUri = artUriC,
                            lyrics = lyricsC,
                            genre = UNKNOWN,
                            date = dateC,
                            size = sizeC
                        )
                        val file = File(music.path)
                        if (file.exists()) {
                            tempList.add(music)
                        }
                    } while (cursor.moveToNext())
                cursor.close()
            }

            context.contentResolver.useQuery(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
            ) { genreCursor ->
                val idIndex = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val nameIndex = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

                while (genreCursor.moveToNext()) {
                    val id = genreCursor.getLong(idIndex)
                    val name = (genreCursor.getStringOrNull(nameIndex) ?: continue).id3GenreName
                    context.contentResolver.useQuery(
                        MediaStore.Audio.Genres.Members.getContentUri(
                            MediaStore.VOLUME_EXTERNAL,
                            id
                        ),
                        arrayOf(MediaStore.Audio.Genres.Members._ID)
                    ) { cursor ->
                        val songIdIndex =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members._ID)

                        while (cursor.moveToNext()) {
                            val songId = cursor.getLong(songIdIndex)
                            tempList.find { it.id.toLong() == songId }
                                ?.let { song -> song.genre = name }
                        }
                    }
                }
            }

            for (music in tempList) {
                if (MainActivity.songByAlbum.containsKey(music.album)) {
                    MainActivity.songByAlbum[music.album]?.add(music)
                } else {
                    MainActivity.songByAlbum[music.album] = ArrayList()
                    MainActivity.songByAlbum[music.album]?.add(music)
                }

                if (MainActivity.songByArtist.containsKey(music.artist)) {
                    MainActivity.songByArtist[music.artist]?.add(music)
                } else {
                    MainActivity.songByArtist[music.artist] = ArrayList()
                    MainActivity.songByArtist[music.artist]?.add(music)
                }
            }

            return tempList
        }

        fun getSavedInfo(context: Context) {
            Favourite.favoriteSongs = ArrayList()
            val editor = context.getSharedPreferences(SAVED_INFO, AppCompatActivity.MODE_PRIVATE)
            val jsonString = editor.getString(FAVOURITE_SONGS, null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                Favourite.favoriteSongs.addAll(data)
            }
            for (i in Favourite.favoriteSongs.size - 1 until -1) {
                val file = File(Favourite.favoriteSongs[i].path)
                if (!file.exists()) {
                    Favourite.favoriteSongs.removeAt(i)
                }
            }
            for (music in Favourite.favoriteSongs) {
                music.image = null
            }

            Playlist.listOfPlaylists = ListOfPlaylists()
            val editorPlaylist =
                context.getSharedPreferences(SAVED_INFO, AppCompatActivity.MODE_PRIVATE)
            val jsonStringPlaylist = editorPlaylist.getString(PLAYLISTS, null)
            val typeTokenPlaylist = object : TypeToken<ListOfPlaylists>() {}.type
            if (jsonStringPlaylist != null) {
                val data: ListOfPlaylists =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, typeTokenPlaylist)
                Playlist.listOfPlaylists = data
            }
            for (i in Playlist.listOfPlaylists.ref.size - 1 until -1) {
                for (j in Playlist.listOfPlaylists.ref[i].musics.size - 1 until -1) {
                    val file = File(Playlist.listOfPlaylists.ref[i].musics[i].path)
                    if (!file.exists()) {
                        Playlist.listOfPlaylists.ref[i].musics.removeAt(i)
                    }
                }
            }
            for (list in Playlist.listOfPlaylists.ref) {
                for (music in list.musics) {
                    music.image = null
                }
            }

            val musicList: ArrayList<Music> = ArrayList()
            val jsonStringPlayerMusicList = editor.getString(RECENT_MUSIC, null)
            val typeTokenPlayerMusicList = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonStringPlayerMusicList != null) {
                val data: ArrayList<Music> = GsonBuilder().create()
                    .fromJson(jsonStringPlayerMusicList, typeTokenPlayerMusicList)
                musicList.addAll(data)
            }
            for (i in musicList.size - 1 until -1) {
                val file = File(musicList[i].path)
                if (!file.exists()) {
                    musicList.removeAt(i)
                }
            }
            for (music in musicList) {
                music.image = null
            }
            val ID = editor.getString(RECENT_MUSIC_CURRENT_POSITION, UNKNOWN)
            val isPlaying = editor.getInt(RECENT_MUSIC_IS_PLAYING, 0)
            val currentPositionInSeekbar =
                editor.getInt(RECENT_MUSIC_CURRENT_POSITION_IN_SEEKBAR, 0)

            playingPlayList = musicList
            isCurrentMusicPlaying = isPlaying == 1
            nowPlayingMusicPositionInSeekBar = currentPositionInSeekbar
            nowPlayingMusicID = ID ?: UNKNOWN
        }
    }
}