package com.example.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlayerBinding


class Player : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    private lateinit var runnable: Runnable

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
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

        binding.seekMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) musicService!!.mediaPlayer!!.seekTo(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        binding.repeatMusic.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatMusic.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.repeat_loop
                    )
                )
            } else {
                repeat = false
                binding.repeatMusic.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.repeat_music
                    )
                )
            }
        }

        binding.dragDownPL.setOnClickListener {
            finish()
        }

        binding.equalizer.setOnClickListener {
            try {
                val EqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                EqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                EqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                EqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(EqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature Doesn't Support .", Toast.LENGTH_SHORT)
                    .show()
            }
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

        if (repeat) binding.repeatMusic.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.repeat_loop
            )
        )
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            playMusic()
            binding.seekMusicStart.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekMusicEnd.text =
                formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekMusic.progress = 0
            binding.seekMusic.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
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
        seekbarSetup()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    private fun seekbarSetup() {
        runnable = Runnable {
            binding.seekMusicStart.text =
                formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekMusic.progress = musicService!!.mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onCompletion(p0: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: Exception) {
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 && resultCode == RESULT_OK) {
            return
        }
    }
}