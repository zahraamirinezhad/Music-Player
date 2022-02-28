package com.example.musicplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.databinding.ActivityPlaylistBinding
import com.example.musicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class playlist : AppCompatActivity() {
    companion object {
        var listOfPlaylists: ListOfPlaylists = ListOfPlaylists()
        lateinit var binding: ActivityPlaylistBinding
    }

    private lateinit var adapter: PlaylistViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@playlist, 2)
        adapter = PlaylistViewAdapter(this@playlist, listOfPlaylists.ref)
        binding.playlistRV.adapter = adapter

        if (listOfPlaylists.ref.isNotEmpty()) binding.instructionPA.visibility = View.GONE

        binding.dragDownPLA.setOnClickListener {
            finish()
        }

        binding.addPlaylist.setOnClickListener {
            customAlertDialog()
        }
    }


    private fun customAlertDialog() {
        val dialog = LayoutInflater.from(this@playlist)
            .inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(dialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(dialog).setTitle("PLAYLIST NAME")
            .setPositiveButton("ADD") { dialog, _ ->
                binding.instructionPA.visibility = View.GONE
                val name = binder.playListNamePL.text
                val username = binder.userNamePL.text
                if (name != null && username != null && name.isNotEmpty() && username.isNotEmpty()) {
                    addPlaylist(name.toString(), username.toString())
                }
                dialog.dismiss()
            }.show()
    }

    private fun addPlaylist(name: String, username: String) {
        var playlistExist = false
        for (i in listOfPlaylists.ref) {
            if (i.name.equals(name)) {
                playlistExist = true
                break
            }
        }

        if (playlistExist) Toast.makeText(this, "Playlist Exist !!", Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = myPlaylist()
            tempPlaylist.name = name
            tempPlaylist.createdBy = username
            tempPlaylist.musics = ArrayList()
            val calender = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd mm yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calender)
            listOfPlaylists.ref.add(tempPlaylist)
            adapter.refresh()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}