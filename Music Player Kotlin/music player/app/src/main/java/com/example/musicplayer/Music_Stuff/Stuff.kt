package com.example.musicplayer.Music_Stuff

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import androidx.core.text.bold
import com.example.musicplayer.Activity.Favourite
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class Stuff {
    companion object {
        fun formatDuration(duration: Long): String {
            val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
            val seconds = (TimeUnit.SECONDS.convert(
                duration,
                TimeUnit.MILLISECONDS
            ) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
            return String.format("%02d:%02d", minutes, seconds)
        }

        fun getImageArt(path: String, dr: Bitmap): ByteArray? {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)
                retriever.embeddedPicture
            } catch (e: Exception) {
                val bitmap = dr
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val bitmapdata = stream.toByteArray()
                bitmapdata
            }
        }

        fun setSongPosition(increment: Boolean) {
            if (!Player.repeat && !Player.isShuffle) {
                if (increment) {
                    if (Player.songPosition == Player.musicListPA.size - 1)
                        Player.songPosition = 0
                    else
                        ++Player.songPosition
                } else {
                    if (Player.songPosition == 0)
                        Player.songPosition = Player.musicListPA.size - 1
                    else
                        --Player.songPosition
                }
            }
        }

        fun playingState(): Int {
            if (Player.isPlaying)
                return R.drawable.pause_music
            return R.drawable.play_music
        }

        fun favouriteState(): Int {
            Player.fIndex = favoriteChecker(Player.musicListPA[Player.songPosition].id)
            if (Player.fIndex != -1)
                return R.drawable.favorite_full_icon
            return R.drawable.favorite_empty_icon
        }

        fun doesListContainsThisMusic(list: ArrayList<Music>, ID: String): Boolean {
            for (music in list) {
                if (music.id == ID)
                    return true
            }
            return false
        }

        fun musicState(): Int {
            when (Player.state) {
                0 -> {
                    return R.drawable.repeat_music
                }

                1 -> {
                    return R.drawable.repeat_loop
                }

                2 -> {
                    return R.drawable.shuffle_icon
                }

                else -> {
                    return 0
                }
            }
        }

        fun setSongPositionShuffle() {
            if (!Player.repeat && Player.isShuffle) {
                Player.songPosition = (0..Player.musicListPA.size).random()
            }
        }

        fun exitApplication() {
            if (Player.musicService != null) {
                Player.musicService!!.audioManager.abandonAudioFocus(Player.musicService)
                Player.musicService!!.stopForeground(true)
                Player.musicService!!.mediaPlayer!!.release()
                Player.musicService = null

                exitProcess(1)
            }
        }

        fun favoriteChecker(id: String): Int {
            Favourite.favoriteSongs.forEachIndexed { index, music ->
                if (id == music.id) {
                    return index
                }
            }
            return -1
        }

        fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music> {
            playlist.forEachIndexed { index, music ->
                val file = File(music.path)
                if (!file.exists())
                    playlist.removeAt(index)
            }
            return playlist
        }

        fun findMusicById(music: Music): Int {
            for ((index, x) in MainActivity.MusicListMA.withIndex()) {
                if (x.id == music.id) {
                    return index
                }
            }
            return -1
        }

        fun getDetails(music: Music): SpannableStringBuilder {
            val str = SpannableStringBuilder().bold { append("DETAILS\n\nName: ") }
                .append(music.title)
                .bold { append("\n\nDuration: ") }
                .append(DateUtils.formatElapsedTime(music.duration / 1000))
                .bold { append("\n\nLocation: ") }.append(music.path)
            return str
        }

        fun sortByMusicAmount(listOfAlbums: LinkedHashMap<String, ArrayList<Music>>): LinkedHashMap<String, ArrayList<Music>> {
            val companyFounderSet: ArrayList<Map.Entry<String, ArrayList<Music>>> = ArrayList()
            companyFounderSet.addAll(listOfAlbums.entries)

            val companyFounderListEntry: List<Map.Entry<String, ArrayList<Music>>> = ArrayList(
                companyFounderSet
            )
            Collections.sort(
                companyFounderListEntry
            ) { p0, p1 -> p1!!.value.size.compareTo(p0!!.value.size) }

            listOfAlbums.clear()

            for ((key, value) in companyFounderListEntry) {
                listOfAlbums[key] = value
            }

            return listOfAlbums
        }

        fun sortByName(listOfAlbums: LinkedHashMap<String, ArrayList<Music>>): LinkedHashMap<String, ArrayList<Music>> {
            val names: ArrayList<String> = ArrayList()
            names.addAll(listOfAlbums.keys)
            quickSort(names, 0, names.size - 1)
            val newList: LinkedHashMap<String, ArrayList<Music>> = LinkedHashMap()
            for (x in names) {
                newList[x] = listOfAlbums.getValue(x)
            }
            return newList
        }

        fun quickSort(arr: ArrayList<String>, low: Int, high: Int) {
            if (low < high) {
                val pi = partition(arr, low, high)
                quickSort(arr, low, pi - 1)
                quickSort(arr, pi + 1, high)
            }
        }

        fun partition(arr: ArrayList<String>, low: Int, high: Int): Int {
            val pivot = arr[high]
            var i = low - 1
            for (j in low until high) {
                if (arr[j].lowercase() < pivot.lowercase()) {
                    i++
                    swap(arr, i, j)
                }
            }
            swap(arr, i + 1, high)
            return i + 1
        }

        fun swap(arr: ArrayList<String>, i: Int, j: Int) {
            val temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }
    }
}