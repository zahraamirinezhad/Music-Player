package com.example.musicplayer.Music_Stuff

import android.graphics.Bitmap

data class Music(
    val id: String = "Unknown",
    val title: String = "Unknown",
    val album: String = "Unknown",
    val artist: String = "Unknown",
    val duration: Long = 0,
    val path: String = "Unknown",
    val artUri: String = "Unknown",
    var lyrics: String? = null,
    var genre: String = "Unknown",
    var image: Bitmap? = null,
    var date: String = "Unknown",
    var size: Long = 0
)


