package com.example.musicplayer.Music_Stuff

data class MyPlaylist(
    var name: String = "",
    var musics: ArrayList<Music> = ArrayList(),
    var createdBy: String = "",
    var createdOn: String = ""
)