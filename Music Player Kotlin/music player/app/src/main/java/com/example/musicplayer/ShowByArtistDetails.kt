package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Activity.Playlist
import com.example.musicplayer.Activity.ShowByAlbumDetails
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.getImageArt
import com.example.musicplayer.Music_Stuff.getReflectionBackground
import com.example.musicplayer.databinding.ActivityShowByArtistDetailsBinding

class ShowByArtistDetails : AppCompatActivity() {
    companion object {
        var currentArtist = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: MusicAdapter
        fun isAdapterSHBALInitialized(): Boolean {
            return this::adapter.isInitialized
        }
    }

    lateinit var binding: ActivityShowByArtistDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityShowByArtistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentArtist = intent.extras?.get("index") as Int
        binding.musicRVSBAD.setItemViewCacheSize(10)
        binding.musicRVSBAD.setHasFixedSize(true)
        binding.musicRVSBAD.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(
            this,
            MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!!,
            artist = true
        )
        binding.musicRVSBAD.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        try {
            val img = getImageArt(
                MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!![0].path,
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.music_bg
                )
            )
            var image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.music_bg
                )
            }

            if (image == null) {
                image = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.music_bg
                )
            }

            binding.showByArtistDTBG.setImageBitmap(getReflectionBackground(image))
        } catch (e: Exception) {
            val image = BitmapFactory.decodeResource(
                resources,
                R.drawable.music_bg
            )

            binding.showByArtistDTBG.setImageBitmap(getReflectionBackground(image))
        }
    }
}