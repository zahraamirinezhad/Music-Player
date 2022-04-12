package com.example.musicplayer.Music_Stuff

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import com.example.musicplayer.Activity.Favourite
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String,
    var lyrics: String? = null
)

class myPlaylist {
    lateinit var name: String
    lateinit var musics: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class ListOfPlaylists {
    var ref: ArrayList<myPlaylist> = ArrayList()
}

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
    Favourite.favoriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            return index
        }
    }
    return -1
}

fun getReflectionBackground(image: Bitmap): Bitmap? {
    // The gap we want between the reflection and the original image
    val reflectionGap = 4

    // Get you bit map from drawable folder
    val width = image.width
    val height = image.height

    // This will not scale but will flip on the Y axis
    val matrix = Matrix()
    matrix.preScale(-1F, 1F)

    // Create a Bitmap with the flip matix applied to it.
    // We only want the bottom half of the image
    val reflectionImage = Bitmap.createBitmap(
        image, 0,
        height / 2, width, height / 2, matrix, false
    )

    // Create a new bitmap with same width but taller to fit reflection
    val bitmapWithReflection = Bitmap.createBitmap(
        width,
        height + height / 2, Bitmap.Config.ARGB_8888
    )

    // Create a new Canvas with the bitmap that's big enough for
    // the image plus gap plus reflection
    val canvas = Canvas(bitmapWithReflection)
    // Draw in the original image
    canvas.drawBitmap(image, 0F, 0F, null)
    //Draw the reflection Image
    canvas.drawBitmap(reflectionImage, 0F, (height + reflectionGap).toFloat(), null)

    // Create a shader that is a linear gradient that covers the reflection
    val paint = Paint()
    val shader = LinearGradient(
        0F,
        image.height.toFloat(), 0F, (bitmapWithReflection.height
                + reflectionGap).toFloat(), 0x70ffffff, 0x00ffffff, Shader.TileMode.CLAMP
    )
    // Set the paint to use this shader (linear gradient)
    paint.setShader(shader)
    // Set the Transfer mode to be porter duff and destination in
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
    // Draw a rectangle using the paint with our linear gradient
    canvas.drawRect(
        0F, height.toFloat(), width.toFloat(), (bitmapWithReflection.height
                + reflectionGap).toFloat(), paint
    )
    return bitmapWithReflection
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

fun getDialogForOnLongClickListener(context: Context, customDialog: View): AlertDialog {
    val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
        .create()
    dialog.show()
    dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
    return dialog
}

fun getInfoDialog(context: Context, detailsDialog: View) {
    val dDialog = MaterialAlertDialogBuilder(context)
        .setBackground(ColorDrawable(0x99000000.toInt()))
        .setView(detailsDialog)
        .setPositiveButton("OK") { self, _ -> self.dismiss() }
        .setCancelable(false)
        .create()
    dDialog.show()
    dDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
    dDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
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
//    val list = ArrayList<Map.Entry<String, ArrayList<Music>>>()
//    list.addAll(listOfAlbums.entries)
//
//    list.sortWith(Comparator { p0, p1 -> p0!!.value.size - p1!!.value.size })
//
//    return listOfAlbums

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

fun sortByAlbumName(listOfAlbums: LinkedHashMap<String, ArrayList<Music>>): LinkedHashMap<String, ArrayList<Music>> {
    val names: ArrayList<String> = ArrayList()
    names.addAll(listOfAlbums.keys)
    quickSort(names, 0, names.size - 1)
    val newList: LinkedHashMap<String, ArrayList<Music>> = LinkedHashMap()
    for (x in names) {
        newList[x] = listOfAlbums.getValue(x)
    }
    return newList

}

private fun swap(arr: ArrayList<String>, i: Int, j: Int) {
    val temp = arr[i]
    arr[i] = arr[j]
    arr[j] = temp
}

private fun partition(arr: ArrayList<String>, low: Int, high: Int): Int {
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

private fun quickSort(arr: ArrayList<String>, low: Int, high: Int) {
    if (low < high) {
        val pi = partition(arr, low, high)
        quickSort(arr, low, pi - 1)
        quickSort(arr, pi + 1, high)
    }
}

