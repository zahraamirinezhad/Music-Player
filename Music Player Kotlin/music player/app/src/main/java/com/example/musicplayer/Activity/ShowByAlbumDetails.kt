package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityShowByAlbumDetailsBinding

class ShowByAlbumDetails : AppCompatActivity() {
    companion object {
        var currentAlbum = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: MusicAdapter
        fun isAdapterSHBALInitialized(): Boolean {
            return this::adapter.isInitialized
        }
    }

    lateinit var binding: ActivityShowByAlbumDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityShowByAlbumDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentAlbum = intent.extras?.get("index") as Int
        binding.albumMusicRV.setItemViewCacheSize(10)
        binding.albumMusicRV.setHasFixedSize(true)
        binding.albumMusicRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(
            this,
            MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!!,
            album = true
        )
        binding.albumMusicRV.adapter = adapter

        binding.shuffleALDT.setOnClickListener {
            val intent = Intent(this@ShowByAlbumDetails, Player::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "AlbumDetailsShuffle")
            startActivity(intent)
        }

        binding.dragDownALDT.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.albumNameALDT.text = MainActivity.songByAlbum.keys.elementAt(currentAlbum)
        binding.albumNameALDT.isSelected = true
        if (MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!!.size >= 2) {
            binding.shuffleALDT.visibility = View.VISIBLE
        }
    }
}