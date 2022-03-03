package com.example.musicplayer.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivitySelectionBinding

class Selection : AppCompatActivity() {
    lateinit var binding: ActivitySelectionBinding
    lateinit var adapter: MusicAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.MusicListMA, selectionActivity = true)
        binding.selectionRV.adapter = adapter
        binding.dragDownSL.setOnClickListener {
            finish()
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
}