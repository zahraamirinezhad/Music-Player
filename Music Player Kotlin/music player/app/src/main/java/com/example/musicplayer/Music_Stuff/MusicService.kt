package com.example.musicplayer.Music_Stuff

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Music_Stuff.playing_song_image.Companion.playPause
import com.example.musicplayer.R

class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager
    private lateinit var runnable: Runnable
    private lateinit var notification: Notification
    override fun onBind(p0: Intent?): IBinder {
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            mediaSession = MediaSessionCompat(baseContext, "My Music")
            return this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPause: Int, favourite: Int, repeat: Int) {

        val prevIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val favouriteIntent =
            Intent(
                baseContext,
                NotificationReceiver::class.java
            ).setAction(ApplicationClass.FAVOURITE)
        val favouritePendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            favouriteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val repeatIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.REPEAT)
        val repeatPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            repeatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var image: Bitmap?
        if (Player.musicListPA[Player.songPosition].image == null) {
            try {
                val img = Stuff.getImageArt(
                    Player.musicListPA[Player.songPosition].path
                )
                image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.image_background
                    )
                }
            } catch (e: Exception) {
                image = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.image_background
                )
            }

        } else {
            image = Player.musicListPA[Player.songPosition].image
        }

        if (Player.isContent) {
            notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                .setContentTitle(Player.musicListPA[Player.songPosition].title)
                .setContentText(Player.musicListPA[Player.songPosition].artist)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(image)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(playPause, "Play", playPendingIntent)
                .addAction(R.drawable.exit, "Exit", exitPendingIntent)
                .build()
        } else {
            val intent = Intent(baseContext, Player::class.java)
            intent.putExtra("index", Player.songPosition)
            intent.putExtra("class", "NowPlaying")
            val contentIntent = PendingIntent.getActivity(this, 0, intent, 0)

            notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(Player.musicListPA[Player.songPosition].title)
                .setContentText(Player.musicListPA[Player.songPosition].artist)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(image)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .addAction(repeat, "Repeat", repeatPendingIntent)
                .addAction(R.drawable.previous_music, "Previous", prevPendingIntent)
                .addAction(playPause, "Play", playPendingIntent)
                .addAction(R.drawable.next_music, "Next", nextPendingIntent)
//                .addAction(favourite, "Favourite", favouritePendingIntent)
                .addAction(R.drawable.exit, "Exit", exitPendingIntent)
                .build()

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val playbackSpeed = if (Player.isPlaying) 1F else 0F
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            val playBackState = PlaybackStateCompat.Builder()
                .setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer!!.currentPosition.toLong(),
                    playbackSpeed
                )
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession.setPlaybackState(playBackState)
            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    mediaPlayer!!.seekTo(pos.toInt())
                    val playBackStateNew = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackStateNew)
                }
            })
        }

        startForeground(13, notification)
    }

    override fun onAudioFocusChange(p0: Int) {
        if (p0 == AUDIOFOCUS_LOSS_TRANSIENT) {
            Player.binding.playPauseBTN.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.play_music
                )
            )
            NowPlaying.binding.playPauseNP.setIconResource(R.drawable.play_music)
            showNotification(
                Stuff.playingState(),
                Stuff.favouriteState(),
                Stuff.musicState()
            )
            Player.isPlaying = false
            mediaPlayer!!.pause()
        } else {
            Player.binding.playPauseBTN.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pause_music
                )
            )
            NowPlaying.binding.playPauseNP.setIconResource(R.drawable.pause_music)
            showNotification(
                Stuff.playingState(),
                Stuff.favouriteState(),
                Stuff.musicState()
            )
            Player.isPlaying = true
            mediaPlayer!!.start()
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            Player.binding.seekMusicStart.text =
                Stuff.formatDuration(mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusic.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
}