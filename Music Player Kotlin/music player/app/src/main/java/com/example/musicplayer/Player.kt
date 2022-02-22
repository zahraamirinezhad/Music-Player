package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class Player : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    private lateinit var runnable: Runnable

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingID: String = ""
        var isFavorite: Boolean = false
        var fIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.darkBlueTheme)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals("content")) {
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))

            binding.favoritesBTN.visibility = View.INVISIBLE
            binding.favoritesBTN.isEnabled = false
            binding.songNamePA.isSelected = true

            val img = getImageArt(
                musicListPA[songPosition].path, BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.music_player_icon_slash_screen
                )
            )
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.music_player_icon_slash_screen
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            binding.songImgPA.setImageBitmap(getReflectionBackground((dr as BitmapDrawable).bitmap))
            val icon: Bitmap = (dr as BitmapDrawable).bitmap
            val final_Bitmap = returnBlurredBackground(icon, this)
            val newdr: Drawable = BitmapDrawable(final_Bitmap)
            binding.musicContainer.background = newdr

            binding.songNamePA.text = musicListPA[songPosition].title

            if (repeat) binding.repeatMusic.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.repeat_loop
                )
            )

            if (min15 || min30 || min60) {
                binding.timer.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.selected_timer
                    )
                )
            }
        } else {
            initializeLayout()
        }

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

        binding.timer.setOnClickListener {
            val isTimer = min15 || min30 || min60
            if (!isTimer) showButtonSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("STOP TIMER")
                    .setMessage("Do You Want to Stop the Timer ?")
                    .setPositiveButton("YES") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timer.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.timer
                            )
                        )
                    }
                    .setNegativeButton("NO") { dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }

        }

        binding.shareMusic.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Share Music File !!"))
        }

        binding.favoritesBTN.setOnClickListener {
            if (isFavorite) {
                isFavorite = false;
                binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
                favourite.favoriteSongs.removeAt(fIndex)
            } else {
                isFavorite = true;
                binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
                favourite.favoriteSongs.add(musicListPA[songPosition])
            }
        }
    }

    private fun setLayout() {
        fIndex = favoriteChecker(musicListPA[songPosition].id)
        if (fIndex != -1) binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
        else binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
        binding.songNamePA.isSelected = true

        val img = getImageArt(
            musicListPA[songPosition].path, BitmapFactory.decodeResource(
                this.resources,
                R.drawable.music_player_icon_slash_screen
            )
        )
        val image = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                resources,
                R.drawable.music_player_icon_slash_screen
            )
        }

        val dr: Drawable = BitmapDrawable(image)
        binding.songImgPA.setImageBitmap(getReflectionBackground((dr as BitmapDrawable).bitmap))
        val icon: Bitmap = (dr as BitmapDrawable).bitmap
        val final_Bitmap = returnBlurredBackground(icon, this)
        val newdr: Drawable = BitmapDrawable(final_Bitmap)
        binding.musicContainer.background = newdr

        NowPlaying.binding.songImgNP.background = dr

        binding.songNamePA.text = musicListPA[songPosition].title

        if (repeat) binding.repeatMusic.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.repeat_loop
            )
        )

        if (min15 || min30 || min60) {
            binding.timer.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.selected_timer
                )
            )
        }

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
            nowPlayingID = musicListPA[songPosition].id
        } catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "PlaylistDetailsShuffle" -> {
                initServiceAndPlaylist(
                    playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics,
                    shuffle = true
                )
            }

            "PlaylistDetailsAdapter" -> {
                initServiceAndPlaylist(
                    playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics,
                    shuffle = false
                )
            }

            "FavoriteAdapter" -> {
                initServiceAndPlaylist(favourite.favoriteSongs, shuffle = false)
            }

            "NowPlaying" -> {
                setLayout()
                binding.seekMusicStart.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekMusicEnd.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekMusic.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekMusic.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying) binding.playPauseBTN.setIconResource(R.drawable.pause_music)
                else binding.playPauseBTN.setIconResource(R.drawable.play_music)
            }

            "MusicAdapterSearch" -> {
                initServiceAndPlaylist(MainActivity.MusicListSearch, shuffle = false)
            }

            "MusicAdapter" -> {
                initServiceAndPlaylist(MainActivity.MusicListMA, shuffle = false)
            }

            "MainActivity" -> {
                initServiceAndPlaylist(MainActivity.MusicListMA, shuffle = true)
            }

            "FavouritesShuffle" -> {
                initServiceAndPlaylist(favourite.favoriteSongs, shuffle = true)
            }
        }
        if (musicService != null && !isPlaying) playMusic()
    }

    private fun initServiceAndPlaylist(playlist: ArrayList<Music>, shuffle: Boolean) {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPA = ArrayList()
        musicListPA.addAll(playlist)
        if (shuffle) musicListPA.shuffle()
        setLayout()
    }

    private fun playMusic() {
        binding.playPauseBTN.setIconResource(R.drawable.pause_music)
        musicService!!.showNotification(R.drawable.pause_music, 1F)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.playPauseBTN.setIconResource(R.drawable.play_music)
        musicService!!.showNotification(R.drawable.play_music, 0F)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    fun backNextMusic(increment: Boolean) {
        setSongPosition(increment)
        setLayout()
        createMediaPlayer()
    }


    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        if (musicService == null) {
            val binder = p1 as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
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

    private fun showButtonSheetDialog() {
        val dialog = BottomSheetDialog(this@Player)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min15)?.setOnClickListener {
            binding.timer.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.selected_timer
                )
            )
            min15 = true
            Thread {
                Thread.sleep((15 * 60000).toLong())
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min30)?.setOnClickListener {
            binding.timer.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.selected_timer
                )
            )
            min30 = true
            Thread {
                Thread.sleep((30 * 60000).toLong())
                if (min30) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min60)?.setOnClickListener {
            binding.timer.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.selected_timer
                )
            )
            min60 = true
            Thread {
                Thread.sleep((60 * 60000).toLong())
                if (min60) exitApplication()
            }.start()
            dialog.dismiss()
        }
    }

    @SuppressLint("Range")
    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE
            )
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            cursor!!.moveToFirst()
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
            val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
            val title =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
            return Music(
                id = "Unknown",
                title = title,
                album = "Unknown",
                artist = "Unknown",
                duration = duration,
                artUri = "Unknown",
                path = path
            )
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id.equals("Unknown") && !isPlaying) exitApplication()
    }
}