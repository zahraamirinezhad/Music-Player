package com.example.musicplayer

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String
)

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(
        duration,
        TimeUnit.MILLISECONDS
    ) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!Player.repeat) {
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

fun exitApplication() {
    if (Player.musicService != null) {
        Player.musicService!!.stopForeground(true)
        Player.musicService!!.mediaPlayer!!.release()
        Player.musicService = null

        exitProcess(1)
    }
}