package com.example.musicplayer.Music_Stuff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.view.View
import androidx.core.content.ContextCompat
import com.example.musicplayer.Activity.*
import com.example.musicplayer.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            ApplicationClass.PLAY -> if (Player.isPlaying) pauseMusic(context = p0!!) else playMusic(
                context = p0!!
            )
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = p0!!)
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = p0!!)
            ApplicationClass.FAVOURITE -> {
                setFavourite()
            }
            ApplicationClass.REPEAT -> {
                repeatState()
            }
            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }

    private fun repeatState() {
        if (Player.state == 2)
            Player.state = 0
        else Player.state++

        when (Player.state) {
            0 -> {
                if (Player.isShuffle) {
                    Player.isShuffle = false
                }
                Player.repeat = false
                Player.binding.repeatMusic.setImageDrawable(
                    Player.stateArray[Player.state]
                )
                Player.musicService!!.showNotification(
                    playingState(),
                    favouriteState(),
                    musicState()
                )
            }

            1 -> {
                Player.repeat = true
                Player.binding.repeatMusic.setImageDrawable(
                    Player.stateArray[Player.state]
                )
                Player.musicService!!.showNotification(
                    playingState(),
                    favouriteState(),
                    musicState()
                )
            }

            2 -> {
                Player.repeat = false
                Player.isShuffle = true
                Player.binding.repeatMusic.setImageDrawable(
                    Player.stateArray[Player.state]
                )
                Player.musicService!!.showNotification(
                    playingState(),
                    favouriteState(),
                    musicState()
                )
            }
        }
    }

    private fun setFavourite() {
        if (Player.isFavorite) {
            Player.isFavorite = false
            Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
            Player.musicService!!.showNotification(
                playingState(),
                favouriteState(),
                musicState()
            )
            favourite.favoriteSongs.removeAt(Player.fIndex)
            if (favourite.favoriteSongs.isEmpty()) {
                favourite.binding.instructionFV.visibility = View.VISIBLE
            }
        } else {
            Player.isFavorite = true
            Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
            Player.musicService!!.showNotification(
                playingState(),
                favouriteState(),
                musicState()
            )
            favourite.favoriteSongs.add(Player.musicListPA[Player.songPosition])
        }
        favourite.favouritesChanged = true
    }

    private fun playMusic(context: Context) {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        Player.musicService!!.showNotification(
            playingState(),
            favouriteState(),
            musicState()
        )
        Player.binding.playPauseBTN.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.pause_music
            )
        )
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.pause_music)
        MainActivity.albumAdapter.update()
    }

    private fun pauseMusic(context: Context) {
        Player.isPlaying = false
        Player.musicService!!.mediaPlayer!!.pause()
        Player.musicService!!.showNotification(
            playingState(),
            favouriteState(),
            musicState()
        )
        Player.binding.playPauseBTN.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.play_music
            )
        )
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.play_music)
        MainActivity.albumAdapter.update()
    }

    private fun prevNextSong(increment: Boolean, context: Context) {

        try {
            if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                MediaPlayer()

            if (Player.isShuffle)
                setSongPositionShuffle()
            else setSongPosition(increment)

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

            val final_Bitmap = returnBlurredBackground(image, context)
            val newdr: Drawable = BitmapDrawable(final_Bitmap)
            Player.binding.musicContainer.background = newdr

            NowPlaying.binding.songImgNP.setImageBitmap(image)

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
            if (ShowByAlbumDetails.isAdapterSHBALInitialized())
                ShowByAlbumDetails.adapter.update()
        } catch (e: Exception) {
            return
        }

    }
}