package com.example.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlayerBinding


class Player : AppCompatActivity(), ServiceConnection {
    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        initializeLayout()

        binding.playPauseBTN.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }

        binding.back.setOnClickListener {
            backNextMusic(false)
        }

        binding.next.setOnClickListener {
            backNextMusic(true)
        }
    }

    private fun setLayout() {
        Glide.with(this).load(musicListPA[songPosition].artUri).apply(
            RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop()
        ).into(binding.songImgPA)

        val img = getImageArt(musicListPA[songPosition].path)
        val image = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                resources,
                R.drawable.music_player_icon_slash_screen
            )
        }

        val dr: Drawable = BitmapDrawable(image)
        binding.musicContainer.background = dr

        binding.songNamePA.text = musicListPA[songPosition].title
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            playMusic()
        } catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {

            "MusicAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }

            "MainActivity" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun playMusic() {
        binding.playPauseBTN.setIconResource(R.drawable.pause_music)
        musicService!!.showNotification(R.drawable.pause_music)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.playPauseBTN.setIconResource(R.drawable.play_music)
        musicService!!.showNotification(R.drawable.play_music)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    fun backNextMusic(increment: Boolean) {
        setSongPosition(increment)
        setLayout()
        createMediaPlayer()
    }


    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder = p1 as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}