package com.example.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            ApplicationClass.PLAY -> if (Player.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = p0!!)
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = p0!!)
            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }

    private fun playMusic() {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        Player.musicService!!.showNotification(R.drawable.pause_music)
        Player.binding.playPauseBTN.setIconResource(R.drawable.pause_music)
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.pause_music)
    }

    private fun pauseMusic() {
        Player.isPlaying = false
        Player.musicService!!.mediaPlayer!!.pause()
        Player.musicService!!.showNotification(R.drawable.play_music)
        Player.binding.playPauseBTN.setIconResource(R.drawable.play_music)
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.play_music)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {

        try {
            if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                MediaPlayer()
            setSongPosition(increment)
            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()
            Glide.with(context).load(Player.musicListPA[Player.songPosition].artUri).apply(
                RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop()
            ).into(Player.binding.songImgPA)
            Player.binding.songNamePA.text = Player.musicListPA[Player.songPosition].title

            val img = getImageArt(Player.musicListPA[Player.songPosition].path)
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.music_player_icon_slash_screen
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            Player.binding.musicContainer.background = dr

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            Glide.with(context).load(Player.musicListPA[Player.songPosition].artUri).apply(
                RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop()
            ).into(NowPlaying.binding.songImgNP)
            NowPlaying.binding.songNameNP.text = Player.musicListPA[Player.songPosition].title
            Player.nowPlayingID = Player.musicListPA[Player.songPosition].id
            playMusic()
        } catch (e: Exception) {
            return
        }

    }
}