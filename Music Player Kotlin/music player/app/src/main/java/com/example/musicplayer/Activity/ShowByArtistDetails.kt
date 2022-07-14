package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.ShowByArtistAdapter
import com.example.musicplayer.Music_Stuff.ImageFormatter
import com.example.musicplayer.Music_Stuff.Stuff
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

        currentArtist = intent.extras?.get("index") as Int
        val isFromPlay = intent.extras!!.getBoolean("ItsFromPlayBTN", false)
        if (isFromPlay) {
            val intent = Intent(this, Player::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "ArtistDetailsAdapter")
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

    override fun onResume() {
        super.onResume()

        if (MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!![0].image == null) {
            try {
                val img = Stuff.getImageArt(
                    MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!![0].path
                )
                val image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.image_background
                    )
                }
                MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!![0].image =
                    image
                binding.showByArtistDTBG.setImageBitmap(
                    ImageFormatter.getReflectionBackground(
                        MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                            currentArtist
                        )]!![0].image!!
                    )
                )
            } catch (e: Exception) {
                MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!![0].image =
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.image_background
                    )
                binding.showByArtistDTBG.setImageBitmap(
                    ImageFormatter.getReflectionBackground(
                        MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                            currentArtist
                        )]!![0].image!!
                    )
                )
            }
        } else {
            binding.showByArtistDTBG.setImageBitmap(
                ImageFormatter.getReflectionBackground(
                    MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(currentArtist)]!![0].image!!
                )
            )
        }
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