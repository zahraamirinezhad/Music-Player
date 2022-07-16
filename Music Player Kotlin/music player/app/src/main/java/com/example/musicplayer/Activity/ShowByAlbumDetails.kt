package com.example.musicplayer.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.Adaptor.AlbumAdapter
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM_DETAILS_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM_DETAILS_SHUFFLE
import com.example.musicplayer.Music_Stuff.Constants.Companion.CLASS
import com.example.musicplayer.Music_Stuff.Constants.Companion.INDEX
import com.example.musicplayer.Music_Stuff.Constants.Companion.ITS_FROM_PLAY_BTN
import com.example.musicplayer.Music_Stuff.ImageFormatter
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityShowByAlbumDetailsBinding

class ShowByAlbumDetails : AppCompatActivity() {
    companion object {
        var currentAlbum = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var adapter: AlbumAdapter
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
        setSupportActionBar(binding.toolbarShowByAlbumDetails)

        binding.toolbarShowByAlbumDetails.setNavigationOnClickListener {
            onBackPressed()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentAlbum = intent.extras?.get(INDEX) as Int
        val isFromPlay = intent.extras!!.getBoolean(ITS_FROM_PLAY_BTN, false)
        if (isFromPlay) {
            val intent = Intent(this, Player::class.java)
            intent.putExtra(INDEX, 0)
            intent.putExtra(CLASS, ALBUM_DETAILS_ADAPTER)
            ContextCompat.startActivity(this, intent, null)
        }
        binding.albumMusicRV.setItemViewCacheSize(10)
        binding.albumMusicRV.setHasFixedSize(true)
        binding.albumMusicRV.layoutManager = LinearLayoutManager(this)
        adapter = AlbumAdapter(
            this,
            MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!!,
        )
        binding.albumMusicRV.adapter = adapter

        binding.shuffleALDT.setOnClickListener {
            val intent = Intent(this@ShowByAlbumDetails, Player::class.java)
            intent.putExtra(INDEX, 0)
            intent.putExtra(CLASS, ALBUM_DETAILS_SHUFFLE)
            startActivity(intent)
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
//        binding.albumNameALDT.isSelected = true
        if (MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!!.size >= 2) {
            binding.shuffleALDT.visibility = View.VISIBLE
        }
        binding.albumName.text =
            "Album : " + MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                currentAlbum
            )]!![0].album
        binding.songAmountInAlbum.text =
            MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!!.size.toString() + " Songs"
        binding.artistNameOfAlbum.text =
            "Artist : " + MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                currentAlbum
            )]!![0].artist

        if (MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!![0].image == null) {
            try {
                val img = Stuff.getImageArt(
                    MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!![0].path
                )
                val image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.image_background
                    )
                }
                MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!![0].image =
                    image
                binding.showByAlbumDTBG.setImageBitmap(
                    ImageFormatter.getReflectionBackground(
                        MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                            currentAlbum
                        )]!![0].image!!
                    )
                )
            } catch (e: Exception) {
                MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!![0].image =
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.image_background
                    )
                binding.showByAlbumDTBG.setImageBitmap(
                    ImageFormatter.getReflectionBackground(
                        MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                            currentAlbum
                        )]!![0].image!!
                    )
                )
            }
        } else {
            binding.showByAlbumDTBG.setImageBitmap(
                ImageFormatter.getReflectionBackground(
                    MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!![0].image!!
                )
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(currentAlbum)]!!.size == 0)
            MainActivity.songByAlbum.remove(MainActivity.songByAlbum.keys.elementAt(currentAlbum))
        MainActivity.albumAdapter.updateAll()
    }
}