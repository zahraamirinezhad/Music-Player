package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.checkPlaylist
import com.example.musicplayer.Music_Stuff.getImageArt
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {
    companion object {
        var currentPlaylist = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: MusicAdapter
    }

    lateinit var binding: ActivityPlaylistDetailsBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylist = intent.extras?.get("index") as Int
        playlist.listOfPlaylists.ref[currentPlaylist].musics =
            checkPlaylist(playlist.listOfPlaylists.ref[currentPlaylist].musics)

        binding.playlistPLDRV.setItemViewCacheSize(10)
        binding.playlistPLDRV.setHasFixedSize(true)
        binding.playlistPLDRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(
            this,
            playlist.listOfPlaylists.ref[currentPlaylist].musics,
            playlistDetails = true
        )
        binding.playlistPLDRV.adapter = adapter

        binding.dragDownPLD.setOnClickListener {
            finish()
        }

        binding.shufflePLD.setOnClickListener {
            val intent = Intent(this@PlaylistDetails, Player::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }

        binding.moreBTNPLD.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.moreBTNPLD)
            popupMenu.menuInflater.inflate(R.menu.playlist_menu, popupMenu.menu)
            val menuHelper =
                MenuPopupHelper(this, popupMenu.menu as MenuBuilder, binding.moreBTNPLD)
            menuHelper.setForceShowIcon(true)
            popupMenu.setOnMenuItemClickListener { p0 ->
                when (p0.itemId) {
                    R.id.addSongPLA -> {
                        startActivity(Intent(this, Selection::class.java))
                    }

                    R.id.removeAllPLA -> {
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setTitle("Remove All The Musics")
                            .setMessage("Do You Want to Remove All the Musics in this Playlist ?")
                            .setPositiveButton("YES") { dialog, _ ->
                                playlist.listOfPlaylists.ref[currentPlaylist].musics.clear()
                                adapter.refreshPlaylist()
                                dialog.dismiss()
                            }
                            .setNegativeButton("NO") { dialog, _ ->
                                dialog.dismiss()
                            }
                        val customDialog = builder.create()
                        customDialog.show()
                        customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(Color.GREEN)
                        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                    }
                }
                true
            }
            menuHelper.show()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.playlistNamePLD.text = playlist.listOfPlaylists.ref[currentPlaylist].name
        binding.moreInfoPLD.text =
            "Created On \n" + playlist.listOfPlaylists.ref[currentPlaylist].createdOn + "\n By \n" + playlist.listOfPlaylists.ref[currentPlaylist].createdBy
        if (adapter.itemCount > 0) {
            val img = getImageArt(
                playlist.listOfPlaylists.ref[currentPlaylist].musics.get(0).path,
                BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.music_player_icon_slash_screen
                )
            )
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.music_player_icon_slash_screen
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            binding.playlistImagePLD.background = dr
            binding.shufflePLD.visibility = View.VISIBLE
            binding.playlistNamePLD.isSelected = true
        }
        adapter.notifyDataSetChanged()

        val editor = getSharedPreferences("savedInfo", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(playlist.listOfPlaylists)
        editor.putString("Playlists", jsonStringPlaylist)
        editor.apply()
    }
}