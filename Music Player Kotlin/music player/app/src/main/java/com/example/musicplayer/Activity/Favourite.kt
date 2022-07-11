package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.FavouritesAdapter
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityFavouriteBinding

class Favourite : AppCompatActivity() {
    companion object {
        var favoriteSongs: ArrayList<Music> = ArrayList()
        lateinit var binding: ActivityFavouriteBinding

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: FavouritesAdapter
        fun isAdapterInitialized(): Boolean {
            return this::adapter.isInitialized
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        favoriteSongs = Stuff.checkPlaylist(favoriteSongs)

        if (favoriteSongs.isNotEmpty()) binding.instructionFV.visibility = View.GONE

        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = LinearLayoutManager(this)
        adapter = FavouritesAdapter(this, favoriteSongs)
        binding.favoriteRV.adapter = adapter
        if (favoriteSongs.size == 1) binding.shuffleFav.visibility = View.INVISIBLE
        else binding.shuffleFav.visibility = View.VISIBLE

        binding.dragDownFA.setOnClickListener {
            finish()
        }

        binding.shuffleFav.setOnClickListener {
            if (favoriteSongs.isNotEmpty()) {
                val intent = Intent(this@Favourite, Player::class.java)
                intent.putExtra("index", 0)
                intent.putExtra("class", "FavouritesShuffle")
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.update()
    }
}