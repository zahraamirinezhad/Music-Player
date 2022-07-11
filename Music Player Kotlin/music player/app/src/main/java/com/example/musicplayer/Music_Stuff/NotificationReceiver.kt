package com.example.musicplayer.Music_Stuff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
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
                Stuff.exitApplication()
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
                    Stuff.playingState(),
                    Stuff.favouriteState(),
                    Stuff.musicState()
                )
            }

            1 -> {
                Player.repeat = true
                Player.binding.repeatMusic.setImageDrawable(
                    Player.stateArray[Player.state]
                )
                Player.musicService!!.showNotification(
                    Stuff.playingState(),
                    Stuff.favouriteState(),
                    Stuff.musicState()
                )
            }

            2 -> {
                Player.repeat = false
                Player.isShuffle = true
                Player.binding.repeatMusic.setImageDrawable(
                    Player.stateArray[Player.state]
                )
                Player.musicService!!.showNotification(
                    Stuff.playingState(),
                    Stuff.favouriteState(),
                    Stuff.musicState()
                )
            }
        }
    }

    private fun setFavourite() {
        Player.fIndex = Stuff.favoriteChecker(Player.musicListPA[Player.songPosition].id)
        if (Player.fIndex != -1) {
            Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
            Player.musicService!!.showNotification(
                Stuff.playingState(),
                R.drawable.favorite_full_icon,
                Stuff.musicState()
            )
            Favourite.favoriteSongs.removeAt(Player.fIndex)
        } else {
            Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
            Player.musicService!!.showNotification(
                Stuff.playingState(),
                R.drawable.favorite_empty_icon,
                Stuff.musicState()
            )
            Favourite.favoriteSongs.add(Player.musicListPA[Player.songPosition])
        }
    }

    private fun playMusic(context: Context) {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        Player.musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.favouriteState(),
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
            Stuff.favouriteState(),
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
            if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                MediaPlayer()

            if (Player.isShuffle)
                Stuff.setSongPositionShuffle()
            else Stuff.setSongPosition(increment)

            MainActivity.musicAdapter.update()
            if (ShowByAlbumDetails.isAdapterSHBALInitialized()) ShowByAlbumDetails.adapter.update()
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.update()
            if (Player.isPlayingFavourites) Favourite.adapter.update()

            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()

            val img = Stuff.getImageArt(
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

            val final_Bitmap = ImageFormatter.returnBlurredBackground(image, context)
            val newdr: Drawable = BitmapDrawable(final_Bitmap)
            Player.binding.musicContainer.background = newdr

            NowPlaying.binding.songImgNP.setImageBitmap(image)

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            NowPlaying.binding.songNameNP.text = Player.musicListPA[Player.songPosition].title
            Player.nowPlayingID = Player.musicListPA[Player.songPosition].id
            playMusic(context)
            Player.fIndex = Stuff.favoriteChecker(Player.musicListPA[Player.songPosition].id)
            if (Player.fIndex != -1) Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
            else Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
            if (ShowByAlbumDetails.isAdapterSHBALInitialized())
                ShowByAlbumDetails.adapter.update()
        } catch (e: Exception) {
            return
        }

    }
}