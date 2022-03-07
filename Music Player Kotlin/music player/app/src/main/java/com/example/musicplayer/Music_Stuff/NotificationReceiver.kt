package com.example.musicplayer.Music_Stuff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Activity.PlaylistDetails
import com.example.musicplayer.Activity.ShowByAlbumDetails
import com.example.musicplayer.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            ApplicationClass.PLAY -> if (Player.isPlaying) pauseMusic(context = p0!!) else playMusic(
                context = p0!!
            )
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = p0!!)
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = p0!!)
            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }

    private fun playMusic(context: Context) {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        Player.musicService!!.showNotification(R.drawable.pause_music)
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
        Player.musicService!!.showNotification(R.drawable.play_music)
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
            if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                MediaPlayer()

            MainActivity.musicAdapter.musicList[findMusicById(Player.musicListPA[Player.songPosition])].isPlayingOrNot =
                false
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.musicList[Player.songPosition].isPlayingOrNot =
                false
            setSongPosition(increment)
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.musicList[Player.songPosition].isPlayingOrNot =
                true
            MainActivity.musicAdapter.musicList[findMusicById(Player.musicListPA[Player.songPosition])].isPlayingOrNot =
                true
            MainActivity.musicAdapter.update()
            if (ShowByAlbumDetails.isAdapterSHBALInitialized()) ShowByAlbumDetails.adapter.update()
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.update()
            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()

            val img = getImageArt(
                Player.musicListPA[Player.songPosition].path, BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.music_player_icon_slash_screen
                )
            )
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.music_player_icon_slash_screen
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            val icon: Bitmap = (dr as BitmapDrawable).bitmap
            val final_Bitmap = returnBlurredBackground(icon, context)
            val newdr: Drawable = BitmapDrawable(final_Bitmap)
            Player.binding.musicContainer.background = newdr

            NowPlaying.binding.songImgNP.setImageBitmap((dr as BitmapDrawable).bitmap)

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            NowPlaying.binding.songNameNP.text = Player.musicListPA[Player.songPosition].title
            Player.nowPlayingID = Player.musicListPA[Player.songPosition].id
            playMusic(context)
            Player.fIndex = favoriteChecker(Player.musicListPA[Player.songPosition].id)
            if (Player.isFavorite) Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
            else Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
        } catch (e: Exception) {
            return
        }

    }
}