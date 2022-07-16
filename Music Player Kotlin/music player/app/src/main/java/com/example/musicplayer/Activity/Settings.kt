package com.example.musicplayer.Activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.Adaptor.AlbumViewAdapter
import com.example.musicplayer.Adaptor.ArtistViewAdapter
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.Constants
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM_NAME
import com.example.musicplayer.Music_Stuff.Constants.Companion.APP_VERSION
import com.example.musicplayer.Music_Stuff.Constants.Companion.ARTIST_NAME
import com.example.musicplayer.Music_Stuff.Constants.Companion.FILE_SIZE
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECEANTLY_ADDED
import com.example.musicplayer.Music_Stuff.Constants.Companion.SONG_AMOUNT
import com.example.musicplayer.Music_Stuff.Constants.Companion.SONG_TITLE
import com.example.musicplayer.Music_Stuff.Constants.Companion.SORTING
import com.example.musicplayer.Music_Stuff.Constants.Companion.SORTING_ALBUM
import com.example.musicplayer.Music_Stuff.Constants.Companion.SORTING_ARTIST
import com.example.musicplayer.Music_Stuff.Constants.Companion.SORT_ORDER
import com.example.musicplayer.Music_Stuff.Constants.Companion.SORT_ORDER_FOR_ALBUM
import com.example.musicplayer.Music_Stuff.Constants.Companion.SORT_ORDER_FOR_ARTIST
import com.example.musicplayer.Music_Stuff.Constants.Companion.YES
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackThemeNav)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = Constants.SETTINGS
        binding.version.text = APP_VERSION

        binding.sortBy.setOnClickListener {
            sort()
        }
    }

    private fun sort() {
        when (MainActivity.binding.musicArtistAlbum.currentItem) {
            2 -> {
                sortByMusic()
            }
            1 -> {
                sortByAlbum()
            }
            0 -> {
                sortByArtist()
            }
        }
    }

    private fun sortByAlbum() {
        val menuList = arrayOf(ALBUM_NAME, SONG_AMOUNT)
        var currentSort = MainActivity.albumSortBy
        val build = MaterialAlertDialogBuilder(this)
        build.setTitle(SORT_ORDER).setPositiveButton(YES) { _, _ ->
            if (MainActivity.albumSortBy != currentSort) {
                val editor =
                    getSharedPreferences(SORTING_ALBUM, MODE_PRIVATE).edit()
                editor.putInt(SORT_ORDER_FOR_ALBUM, currentSort)
                editor.apply()
            }

        }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
            currentSort = wich
        }
        val customDialog = build.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(Color.GREEN)
    }

    private fun sortByArtist() {
        val menuList = arrayOf(ARTIST_NAME, SONG_AMOUNT)
        var currentSort = MainActivity.artistSortBy
        val build = MaterialAlertDialogBuilder(this)
        build.setTitle(SORT_ORDER).setPositiveButton(YES) { _, _ ->
            if (MainActivity.artistSortBy != currentSort) {
                val editor =
                    getSharedPreferences(SORTING_ARTIST, MODE_PRIVATE).edit()
                editor.putInt(SORT_ORDER_FOR_ARTIST, currentSort)
                editor.apply()
            }

        }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
            currentSort = wich
        }
        val customDialog = build.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(Color.GREEN)
    }

    private fun sortByMusic() {
        val menuList = arrayOf(RECEANTLY_ADDED, SONG_TITLE, FILE_SIZE)
        var currentSort = MainActivity.sortBy
        val build = MaterialAlertDialogBuilder(this)
        build.setTitle(SORT_ORDER).setPositiveButton(YES) { _, _ ->
            val editor = getSharedPreferences(SORTING, MODE_PRIVATE).edit()
            editor.putInt(SORT_ORDER, currentSort)
            editor.apply()
        }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
            currentSort = wich
        }
        val customDialog = build.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
    }
}