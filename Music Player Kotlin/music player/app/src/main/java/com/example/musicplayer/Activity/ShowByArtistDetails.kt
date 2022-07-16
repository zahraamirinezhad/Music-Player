package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.ShowByArtistAdapter
import com.example.musicplayer.Music_Stuff.Constants.Companion.ARTIST_DETAILS_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.CLASS
import com.example.musicplayer.Music_Stuff.Constants.Companion.INDEX
import com.example.musicplayer.Music_Stuff.Constants.Companion.ITS_FROM_PLAY_BTN
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityShowByArtistDetailsBinding

class ShowByArtistDetails : AppCompatActivity() {
    companion object {
        var currentArtist = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: ShowByArtistAdapter
        fun isAdapterSHBARInitialized(): Boolean {
            return this::adapter.isInitialized
        }
    }

    lateinit var binding: ActivityShowByArtistDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityShowByArtistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentArtist = intent.extras?.get(INDEX) as Int
        val isFromPlay = intent.extras!!.getBoolean(ITS_FROM_PLAY_BTN, false)
        if (isFromPlay) {
            val intent = Intent(this, Player::class.java)
            intent.putExtra(INDEX, 0)
            intent.putExtra(CLASS, ARTIST_DETAILS_ADAPTER)
            ContextCompat.startActivity(this, intent, null)
        }
        binding.musicRVSBAD.setItemViewCacheSize(10)
        binding.musicRVSBAD.setHasFixedSize(true)
        binding.musicRVSBAD.layoutManager = LinearLayoutManager(this)
        adapter = ShowByArtistAdapter(
            this,
            MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!!,
        )
        binding.musicRVSBAD.adapter = adapter
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!!.size == 0)
            MainActivity.songByArtist.remove(
                MainActivity.songByArtist.keys.elementAt(
                    currentArtist
                )
            )
        MainActivity.artistAdapter.updateAll()
    }
}