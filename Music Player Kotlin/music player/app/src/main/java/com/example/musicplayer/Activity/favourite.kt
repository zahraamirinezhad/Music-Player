package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.Adaptor.FavoriteAdapter
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.checkPlaylist
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityFavouriteBinding

class favourite : AppCompatActivity() {
    companion object {
        var favoriteSongs: ArrayList<Music> = ArrayList()
        lateinit var binding: ActivityFavouriteBinding
        var favouritesChanged: Boolean = false

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: FavoriteAdapter
        fun isAdapterInitialized(): Boolean {
            return this::adapter.isInitialized
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        favoriteSongs = checkPlaylist(favoriteSongs)

        if (favoriteSongs.isNotEmpty()) binding.instructionFV.visibility = View.GONE

        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = GridLayoutManager(this, 3)
        adapter = FavoriteAdapter(this, favoriteSongs, isFavourite = true)
        binding.favoriteRV.adapter = adapter
        favouritesChanged = false
        if (favoriteSongs.size == 1) binding.shuffleFav.visibility = View.INVISIBLE
        else binding.shuffleFav.visibility = View.VISIBLE

        binding.dragDownFA.setOnClickListener {
            finish()
        }

        binding.shuffleFav.setOnClickListener {
            if (favoriteSongs.isNotEmpty()) {
                val intent = Intent(this@favourite, Player::class.java)
                intent.putExtra("index", 0)
                intent.putExtra("class", "FavouritesShuffle")
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (favouritesChanged) {
            adapter.update(favoriteSongs)
            favouritesChanged = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.isFavourite = false
    }
}