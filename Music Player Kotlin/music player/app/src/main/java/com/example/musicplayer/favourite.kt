package com.example.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.databinding.ActivityFavouriteBinding

class favourite : AppCompatActivity() {
    companion object {
        var favoriteSongs: ArrayList<Music> = ArrayList()
    }

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var adapter: FavoriteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.darkBlueTheme)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        favoriteSongs = checkPlaylist(favoriteSongs)

        binding.dragDownFA.setOnClickListener {
            finish()
        }

        binding.shuffleFav.setOnClickListener {
            val intent = Intent(this@favourite, Player::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavouritesShuffle")
            startActivity(intent)
        }

        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = GridLayoutManager(this, 3)
        adapter = FavoriteAdapter(this, favoriteSongs)
        binding.favoriteRV.adapter = adapter
        if (favoriteSongs.size == 1) binding.shuffleFav.visibility = View.INVISIBLE
        else binding.shuffleFav.visibility = View.VISIBLE
    }
}