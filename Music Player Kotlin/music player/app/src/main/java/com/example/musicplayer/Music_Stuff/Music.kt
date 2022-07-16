package com.example.musicplayer.Music_Stuff

import android.graphics.Bitmap

data class Music(
    val id: String = Constants.UNKNOWN,
    val title: String = Constants.UNKNOWN,
    val album: String = Constants.UNKNOWN,
    val artist: String = Constants.UNKNOWN,
    val duration: Long = 0,
    val path: String = Constants.UNKNOWN,
    val artUri: String = Constants.UNKNOWN,
    var lyrics: String? = null,
    var genre: String = Constants.UNKNOWN,
    var image: Bitmap? = null,
    var date: String = Constants.UNKNOWN,
    var size: Long = 0
)


