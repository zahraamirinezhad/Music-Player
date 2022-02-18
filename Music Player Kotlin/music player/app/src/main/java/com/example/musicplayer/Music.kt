package com.example.musicplayer

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
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

fun returnBlurredBackground(input: Bitmap, context: Context): Bitmap? {
    return try {
        val rsScript: RenderScript = RenderScript.create(context)
        val alloc: Allocation = Allocation.createFromBitmap(rsScript, input)
        val blur: ScriptIntrinsicBlur =
            ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript))
        blur.setRadius(21F)
        blur.setInput(alloc)
        val result = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val outAlloc: Allocation = Allocation.createFromBitmap(rsScript, result)
        blur.forEach(outAlloc)
        outAlloc.copyTo(result)
        rsScript.destroy()
        result
    } catch (e: Exception) {
        // TODO: handle exception
        input
    }
}

fun favoriteChecker(id: String): Int {
    Player.isFavorite = false
    favorite.favoriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            Player.isFavorite = true
            return index
        }
    }
    return -1
}