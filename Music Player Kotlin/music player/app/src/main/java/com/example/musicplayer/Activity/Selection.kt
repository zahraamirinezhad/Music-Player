package com.example.musicplayer.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.SelectionAdapter
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivitySelectionBinding

class Selection : AppCompatActivity() {
    lateinit var binding: ActivitySelectionBinding
    lateinit var adapter: SelectionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = SelectionAdapter(this, MainActivity.MusicListMA)
        binding.selectionRV.adapter = adapter

        binding.selectAll.setOnClickListener {
            if (binding.selectAll.isChecked) {
                for (x in binding.selectionRV.touchables)
                    x.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.cool_pink
                        )
                    )

                for (song in MainActivity.MusicListMA) {
                    var exist = false
                    Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.forEachIndexed { index, music ->
                        if (song.id == music.id) {
                            exist = true
                        }
                    }
                    if (!exist) {
                        Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.add(
                            song
                        )
                    }
                }
            } else {
                for (x in binding.selectionRV.touchables)
                    x.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.white
                        )
                    )
                for (song in MainActivity.MusicListMA) {
                    Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.clear()
                }
            }
            adapter.selectAll = binding.selectAll.isChecked
            adapter.update()
        }
        binding.searchSongSL.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean = true

            override fun onQueryTextChange(p0: String?): Boolean {
                MainActivity.MusicListSearch = ArrayList()
                if (p0 != null) {
                    val input = p0.lowercase()
                    for (song in MainActivity.MusicListMA) {
                        if (song.title.lowercase().contains(input)) {
                            MainActivity.MusicListSearch.add(song)
                        }
                    }
                    MainActivity.search = true
                    adapter.updateMusicList(MainActivity.MusicListSearch)
                }
                return true
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.selectAll = false
    }
}