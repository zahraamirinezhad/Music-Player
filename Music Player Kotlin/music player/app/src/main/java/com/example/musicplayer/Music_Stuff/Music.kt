package com.example.musicplayer.Music_Stuff

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


