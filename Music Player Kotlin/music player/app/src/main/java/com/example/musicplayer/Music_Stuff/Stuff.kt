package com.example.musicplayer.Music_Stuff

import android.media.MediaMetadataRetriever
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import androidx.core.text.bold
import com.example.musicplayer.Activity.Favourite
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.R
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
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

        fun getImageArt(uri: String): ByteArray? {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(uri)
            val art = retriever.embeddedPicture
            retriever.release()
            return art
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
    }
}