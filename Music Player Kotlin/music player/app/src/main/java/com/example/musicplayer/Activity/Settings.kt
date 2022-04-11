package com.example.musicplayer.Activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.Adaptor.AlbumViewAdapter
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.sortByAlbumName
import com.example.musicplayer.Music_Stuff.sortByMusicAmount
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackThemeNav)
        //Initializing Binding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "SETTINGS"
        binding.version.text = "1.0.1"
        binding.sortBy.setOnClickListener {
            if (MainActivity.binding.musicRV.adapter is MusicAdapter) {
                val menuList = arrayOf("Recently Added", "Song Title", "File Size")
                var currentSort = MainActivity.sortBy
                val build = MaterialAlertDialogBuilder(this)
                build.setTitle("SORT ORDER").setPositiveButton("YES") { _, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("SORT ORDER", currentSort)
                    editor.apply()
                }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
                    currentSort = wich
                }
                val customDialog = build.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
            } else if (MainActivity.binding.musicRV.adapter is AlbumViewAdapter) {
                val menuList = arrayOf("Album Name", "Song Amount")
                var currentSort = MainActivity.albumSortBy
                val build = MaterialAlertDialogBuilder(this)
                build.setTitle("SORT ORDER").setPositiveButton("YES") { _, _ ->
                    if (MainActivity.albumSortBy != currentSort) {
                        val editor =
                            getSharedPreferences("SORTING_ALBUM", MODE_PRIVATE).edit()
                        editor.putInt("SORT ORDER FOR ALBUM", currentSort)
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
        }
    }
}