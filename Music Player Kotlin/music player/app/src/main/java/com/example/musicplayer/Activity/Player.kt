package com.example.musicplayer.Activity

import android.animation.*
import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musicplayer.*
import com.example.musicplayer.Fragment.PlayerViewPagerAdapter
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM_DETAILS_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM_DETAILS_SHUFFLE
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM_VIEW_PLAY
import com.example.musicplayer.Music_Stuff.Constants.Companion.ARTIST_DETAILS_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.CLASS
import com.example.musicplayer.Music_Stuff.Constants.Companion.FAVOURITES_SHUFFLE
import com.example.musicplayer.Music_Stuff.Constants.Companion.FAVOURITE_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.INDEX
import com.example.musicplayer.Music_Stuff.Constants.Companion.MAIN_ACTIVITY
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSIC_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSIC_ADAPTER_SEARCH
import com.example.musicplayer.Music_Stuff.Constants.Companion.NOW_PLAYING
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYER_MENU
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYLIST_DETAILS_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYLIST_DETAILS_SHUFFLE
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC_CURRENT_POSITION
import com.example.musicplayer.Music_Stuff.Constants.Companion.ROTATION
import com.example.musicplayer.Music_Stuff.Constants.Companion.SONG_IMAGE
import com.example.musicplayer.Music_Stuff.Constants.Companion.SONG_LYRICS
import com.example.musicplayer.Music_Stuff.Constants.Companion.UNKNOWN
import com.example.musicplayer.Music_Stuff.PlayerJobs.Companion.updateCurrentMusicBack
import com.example.musicplayer.databinding.ActivityPlayerBinding
import kotlin.collections.ArrayList


class Player : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    var startMusicAt = 0

    companion object {
        var isContent = false
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = true
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingID: String = ""
        var fIndex: Int = -1
        var isShuffle = false
        var state: Int = 0
        lateinit var stateArray: Array<Drawable?>
        lateinit var mainImageAnimator: ObjectAnimator
        var isPlayingPlaylist: Boolean = false
        var isPlayingFavourites: Boolean = false

        fun isMusicListPaInitialized(): Boolean {
            return this::musicListPA.isInitialized
        }
    }

    @SuppressLint("Recycle", "RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals(Constants.CONTENT)) {
            commingFromOutside()
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
                if (b) {
                    musicService!!.mediaPlayer!!.seekTo(i)
                    musicService!!.showNotification(
                        Stuff.playingState(),
                        Stuff.musicState()
                    )
                    mainImageAnimator.duration =
                        musicService!!.mediaPlayer!!.duration.toLong() - i + 10000
                    mainImageAnimator.setFloatValues(
                        ((musicService!!.mediaPlayer!!.duration.toLong() - i + 10000) * Math.toDegrees(
                            2 * Math.PI
                        ) / 50000).toFloat()
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        binding.repeatMusic.setOnClickListener {
            PlayerJobs.repeatState()
        }

        binding.dragDownPL.setOnClickListener {
            finish()
        }

        binding.equalizer.setOnClickListener {
            val intent = Intent(this, MyEqualizer::class.java)
            startActivity(intent)

        }

        binding.timer.setOnClickListener {
            PlayerJobs.setTimer(this)

        }

        binding.shareMusic.setOnClickListener {
            PlayerJobs.shareCurrentMusic(this)
        }

        binding.favoritesBTN.setOnClickListener {
            PlayerJobs.setCurrentMusicAsFavourite()
        }

        binding.moreOptions.setOnClickListener {
            val menu = PlayerMenu()
            menu.show(supportFragmentManager, PLAYER_MENU)
        }
    }

    @SuppressLint("Recycle")
    private fun setLayout() {
        stateArray = arrayOf(
            ContextCompat.getDrawable(
                this,
                R.drawable.repeat_music
            ), ContextCompat.getDrawable(
                this,
                R.drawable.repeat_loop
            ), ContextCompat.getDrawable(
                this,
                R.drawable.shuffle_icon
            )
        )

        fIndex = Stuff.favoriteChecker(musicListPA[songPosition].id)
        if (fIndex != -1) binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
        else binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
        binding.songNamePA.isSelected = true

        NowPlaying.binding.songNameNP.text = musicListPA[songPosition].title

        binding.songNamePA.text = musicListPA[songPosition].title


        if (!repeat && !isShuffle) binding.repeatMusic.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.repeat_music
            )
        )
        else if (repeat && !isShuffle) binding.repeatMusic.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.repeat_loop
            )
        )
        else if (!repeat && isShuffle) binding.repeatMusic.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.shuffle_icon
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

        val wormDotsIndicator = binding.wormDotsIndicator
        val viewPager = binding.viewPager
        val adapter = PlayerViewPagerAdapter(supportFragmentManager)
        adapter.addFrag(playing_song_image(), SONG_IMAGE)
        adapter.addFrag(playin_song_lyrics(), SONG_LYRICS)
        viewPager.adapter = adapter
        wormDotsIndicator.setViewPager(viewPager)
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()

            binding.seekMusicStart.text =
                Stuff.formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekMusicEnd.text =
                Stuff.formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekMusic.progress = startMusicAt
            musicService!!.mediaPlayer!!.seekTo(startMusicAt)
            binding.seekMusic.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingID = musicListPA[songPosition].id
            mainImageAnimator.setFloatValues(
                (musicService!!.mediaPlayer!!.duration.toLong() * Math.toDegrees(
                    2 * Math.PI
                ) / 50000).toFloat()
            )
            mainImageAnimator.duration = musicService!!.mediaPlayer!!.duration.toLong()

            playMusic()

//            if (!isPlaying) {
//                pauseMusic()
//            } else {
//                playMusic()
//            }
        } catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout() {
        val classType = intent.getStringExtra((CLASS))
        if (classType == NOW_PLAYING) {
            try {
                setLayout()
                binding.seekMusicStart.text =
                    Stuff.formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekMusicEnd.text =
                    Stuff.formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekMusic.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekMusic.max = musicService!!.mediaPlayer!!.duration

                if (isPlaying) binding.playPauseBTN.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.pause_music
                    )
                )
                else binding.playPauseBTN.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.play_music
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            songPosition = intent.getIntExtra(INDEX, 0)
            when (classType) {
                PLAYLIST_DETAILS_SHUFFLE -> {
                    isPlayingPlaylist = true
                    isPlayingFavourites = false
                    initServiceAndPlaylist(
                        Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics,
                        shuffle = true
                    )
                }

                ALBUM_DETAILS_SHUFFLE -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(
                        MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                            ShowByAlbumDetails.currentAlbum
                        )]!!,
                        shuffle = true
                    )
                }

                PLAYLIST_DETAILS_ADAPTER -> {
                    isPlayingPlaylist = true
                    isPlayingFavourites = false
                    initServiceAndPlaylist(
                        Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics,
                        shuffle = false
                    )
                }

                ALBUM_DETAILS_ADAPTER -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(
                        MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                            ShowByAlbumDetails.currentAlbum
                        )]!!,
                        shuffle = false
                    )
                }

                ARTIST_DETAILS_ADAPTER -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(
                        MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                            ShowByArtistDetails.currentArtist
                        )]!!,
                        shuffle = false
                    )
                }

                ALBUM_VIEW_PLAY -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(
                        MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                            ShowByAlbumDetails.currentAlbum
                        )]!!,
                        shuffle = false,
                    )
                }

                FAVOURITE_ADAPTER -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = true
                    initServiceAndPlaylist(Favourite.favoriteSongs, shuffle = false)
                }

                MUSIC_ADAPTER_SEARCH -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(MainActivity.MusicListSearch, shuffle = false)
                }

                MUSIC_ADAPTER -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(MainActivity.MusicListMA, shuffle = false)
                }

                MAIN_ACTIVITY -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(MainActivity.MusicListMA, shuffle = true)
                }

                FAVOURITES_SHUFFLE -> {
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(Favourite.favoriteSongs, shuffle = true)
                }

                RECENT_MUSIC -> {
                    val recentMusicIsPlaying =
                        intent.getBooleanExtra(Constants.RECENT_MUSIC_IS_PLAYING, true)
                    isPlaying = recentMusicIsPlaying
                    val currentPosition = intent.getIntExtra(RECENT_MUSIC_CURRENT_POSITION, 0)
                    startMusicAt = currentPosition
                    isPlayingPlaylist = false
                    isPlayingFavourites = false
                    initServiceAndPlaylist(Data.playingPlayList, shuffle = false)
                }
            }
        }

//        if (musicService != null && !isPlaying) playMusic()
    }

    private fun initServiceAndPlaylist(
        playlist: ArrayList<Music>,
        shuffle: Boolean,
    ) {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        musicListPA = ArrayList()
        musicListPA.addAll(playlist)
        if (shuffle) isShuffle = true
        setLayout()
    }

    private fun playMusic() {
        binding.playPauseBTN.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pause_music
            )
        )
        isPlaying = true
        musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.musicState()
        )
        musicService!!.mediaPlayer!!.start()
        val isPausedOrNot = playing_song_image.playPause
        val imageViewObjectAnimator = ObjectAnimator.ofFloat(isPausedOrNot, ROTATION, 2f)
        imageViewObjectAnimator.duration = 800
        isPausedOrNot.pivotX = isPausedOrNot.drawable.bounds.width().toFloat() - 200f
        isPausedOrNot.pivotY = 100f
        imageViewObjectAnimator.start()
        mainImageAnimator.resume()
        updateCurrentMusicBack()
    }

    private fun pauseMusic() {
        binding.playPauseBTN.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.play_music
            )
        )
        isPlaying = false
        musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.musicState()
        )
        musicService!!.mediaPlayer!!.pause()
        val isPausedOrNot = playing_song_image.playPause
        val imageViewObjectAnimator = ObjectAnimator.ofFloat(isPausedOrNot, ROTATION, -5f)
        imageViewObjectAnimator.duration = 800
        isPausedOrNot.pivotX = isPausedOrNot.drawable.bounds.width().toFloat() - 200f
        isPausedOrNot.pivotY = 100f
        imageViewObjectAnimator.start()
        mainImageAnimator.pause()
//        if (MainActivity.isAlbumAdapterInitialized()) MainActivity.albumAdapter.update()
    }

    private fun backNextMusic(increment: Boolean) {
        startMusicAt = 0
        if (isShuffle)
            Stuff.setSongPositionShuffle()
        else Stuff.setSongPosition(increment)
        updateCurrentMusicBack()
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
        if (isShuffle)
            Stuff.setSongPositionShuffle()
        else Stuff.setSongPosition(true)

        try {
            startMusicAt = 0
            createMediaPlayer()
            setLayout()
            updateCurrentMusicBack()
        } catch (e: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 && resultCode == RESULT_OK) {
            return
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
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: UNKNOWN
            return Music(
                title = title,
                duration = duration,
                path = path
            )
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == UNKNOWN && !isPlaying) Stuff.exitApplication()
    }

    fun commingFromOutside() {
        isContent = true
        val intentService = Intent(this, MusicService::class.java)
        bindService(intentService, this, BIND_AUTO_CREATE)
        startService(intentService)
        musicListPA = ArrayList()
        musicListPA.add(getMusicDetails(intent.data!!))
        songPosition = 0

        binding.favoritesBTN.visibility = View.INVISIBLE
        binding.favoritesBTN.isEnabled = false
        binding.songNamePA.isSelected = true

        binding.repeatMusic.isEnabled = false
        binding.repeatMusic.visibility = View.GONE
        binding.timer.isEnabled = false
        binding.timer.visibility = View.GONE
        binding.moreOptions.isEnabled = false
        binding.moreOptions.visibility = View.GONE
        binding.back.isEnabled = false
        binding.back.visibility = View.GONE
        binding.next.isEnabled = false
        binding.next.visibility = View.GONE
        binding.dragDownPL.isEnabled = false
        binding.dragDownPL.visibility = View.GONE

        binding.songNamePA.text = musicListPA[songPosition].title

        val wormDotsIndicator = binding.wormDotsIndicator
        val viewPager = binding.viewPager
        val adapter = PlayerViewPagerAdapter(supportFragmentManager)
        adapter.addFrag(playing_song_image(), SONG_IMAGE)
        viewPager.adapter = adapter
        wormDotsIndicator.setViewPager(viewPager)
    }
}