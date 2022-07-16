package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.PlaylistDetailsAdapter
import com.example.musicplayer.Music_Stuff.Constants.Companion.CLASS
import com.example.musicplayer.Music_Stuff.Constants.Companion.INDEX
import com.example.musicplayer.Music_Stuff.Constants.Companion.NO
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYLIST_DETAILS_SHUFFLE
import com.example.musicplayer.Music_Stuff.Constants.Companion.REMOVE_ALL_MUSICS
import com.example.musicplayer.Music_Stuff.Constants.Companion.WANNA_REMOVE_ALL_MUSICS
import com.example.musicplayer.Music_Stuff.Constants.Companion.YES
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistDetails : AppCompatActivity() {
    companion object {
        var currentPlaylist = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: PlaylistDetailsAdapter
    }

    lateinit var binding: ActivityPlaylistDetailsBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylist = intent.extras?.get(INDEX) as Int
        Playlist.listOfPlaylists.ref[currentPlaylist].musics =
            Stuff.checkPlaylist(Playlist.listOfPlaylists.ref[currentPlaylist].musics)

        binding.playlistPLDRV.setItemViewCacheSize(10)
        binding.playlistPLDRV.setHasFixedSize(true)
        binding.playlistPLDRV.layoutManager = LinearLayoutManager(this)
        adapter = PlaylistDetailsAdapter(
            this,
            Playlist.listOfPlaylists.ref[currentPlaylist].musics,
        )
        binding.playlistPLDRV.adapter = adapter

        binding.dragDownPLD.setOnClickListener {
            finish()
        }

        binding.shufflePLD.setOnClickListener {
            val intent = Intent(this@PlaylistDetails, Player::class.java)
            intent.putExtra(INDEX, 0)
            intent.putExtra(CLASS, PLAYLIST_DETAILS_SHUFFLE)
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
                        builder.setTitle(REMOVE_ALL_MUSICS)
                            .setMessage(WANNA_REMOVE_ALL_MUSICS)
                            .setPositiveButton(YES) { dialog, _ ->
                                Playlist.listOfPlaylists.ref[currentPlaylist].musics.clear()
                                adapter.refreshPlaylist()
                                dialog.dismiss()
                            }
                            .setNegativeButton(NO) { dialog, _ ->
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
        binding.playlistNamePLD.text = Playlist.listOfPlaylists.ref[currentPlaylist].name
        binding.moreInfoPLD.text =
            "Created On \n" + Playlist.listOfPlaylists.ref[currentPlaylist].createdOn + "\n By \n" + Playlist.listOfPlaylists.ref[currentPlaylist].createdBy
        if (adapter.itemCount > 0) {
            if (Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].image == null) {
                try {
                    val img =
                        Stuff.getImageArt(Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].path)
                    val image = if (img != null) {
                        BitmapFactory.decodeByteArray(img, 0, img.size)
                    } else {
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.image_background
                        )
                    }
                    Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].image = image
                    binding.playlistImagePLD.setImageBitmap(Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].image)
                } catch (e: Exception) {
                    Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].image =
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.image_background
                        )
                    binding.playlistImagePLD.setImageBitmap(Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].image)
                }
            } else {
                binding.playlistImagePLD.setImageBitmap(Playlist.listOfPlaylists.ref[currentPlaylist].musics[0].image)
            }

            binding.shufflePLD.visibility = View.VISIBLE
            binding.playlistNamePLD.isSelected = true
        }
        adapter.notifyDataSetChanged()
    }
}