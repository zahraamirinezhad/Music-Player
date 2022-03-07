package com.example.musicplayer.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.Adaptor.AlbumViewAdapter
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityShowByAlbumBinding

class ShowByAlbum : AppCompatActivity() {
    private lateinit var binding: ActivityShowByAlbumBinding
    private lateinit var adapter: AlbumViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityShowByAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.albumsRV.setHasFixedSize(true)
        binding.albumsRV.setItemViewCacheSize(13)
        binding.albumsRV.layoutManager = GridLayoutManager(this@ShowByAlbum, 2)
        adapter = AlbumViewAdapter(this, MainActivity.songByAlbum)
        binding.albumsRV.adapter = adapter

        binding.dragDownAL.setOnClickListener {
            finish()
        }
    }
}