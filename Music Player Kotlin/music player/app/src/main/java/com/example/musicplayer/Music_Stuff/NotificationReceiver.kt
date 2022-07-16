package com.example.musicplayer.Music_Stuff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.musicplayer.Activity.*
import com.example.musicplayer.Music_Stuff.PlayerJobs.Companion.prevNextNotPlayer
import com.example.musicplayer.Music_Stuff.PlayerJobs.Companion.repeatState
import com.example.musicplayer.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            ApplicationClass.PLAY -> if (Player.isPlaying) pauseMusic(context = p0!!) else playMusic(
                context = p0!!
            )
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = p0!!)
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = p0!!)

            ApplicationClass.REPEAT -> {
                repeatState()
            }
            ApplicationClass.EXIT -> {
                Data.saveAllInfo(p0!!)
                Stuff.exitApplication()
            }
        }
    }

    private fun playMusic(context: Context) {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        Player.musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.musicState()
        )
        Player.binding.playPauseBTN.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.pause_music
            )
        )
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.pause_music)
    }

    private fun pauseMusic(context: Context) {
        Player.isPlaying = false
        Player.musicService!!.mediaPlayer!!.pause()
        Player.musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.musicState()
        )
        Player.binding.playPauseBTN.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.play_music
            )
        )
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.play_music)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        try {
            prevNextNotPlayer(context, increment)
            Player.nowPlayingID = Player.musicListPA[Player.songPosition].id
            playMusic(context)
            Player.fIndex = Stuff.favoriteChecker(Player.musicListPA[Player.songPosition].id)
            if (Player.fIndex != -1) Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
            else Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
        } catch (e: Exception) {
            return
        }
    }
}