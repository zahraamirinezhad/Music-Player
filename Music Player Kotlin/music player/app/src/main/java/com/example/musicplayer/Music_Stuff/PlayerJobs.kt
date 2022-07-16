package com.example.musicplayer.Music_Stuff

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.musicplayer.Activity.*
import com.example.musicplayer.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerJobs {
    companion object {

        fun repeatState() {
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
                        Stuff.musicState()
                    )
                }
            }
        }

        fun prevNextNotPlayer(context: Context, increment: Boolean) {
            try {
                if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                    MediaPlayer()

                if (Player.isShuffle)
                    Stuff.setSongPositionShuffle()
                else Stuff.setSongPosition(increment)

                updateCurrentMusicBack()

                Player.musicService!!.mediaPlayer!!.reset()
                Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
                Player.musicService!!.mediaPlayer!!.prepare()
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
                                context.resources,
                                R.drawable.image_background
                            )
                        }
                    } catch (e: Exception) {
                        image = BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.image_background
                        )
                    }
                } else {
                    image = Player.musicListPA[Player.songPosition].image
                }

                NowPlaying.binding.songImgNP.setImageBitmap(image)

                Player.binding.seekMusic.progress = 0
                Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
                Player.binding.seekMusicStart.text =
                    Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
                Player.binding.seekMusicEnd.text =
                    Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

                NowPlaying.binding.songNameNP.text = Player.musicListPA[Player.songPosition].title
            } catch (e: Exception) {
            }
        }

        fun updateCurrentMusicBack() {
            if (MainActivity.binding.musicArtistAlbum.currentItem == 2) MainActivity.musicAdapter.update()
            if (MainActivity.binding.musicArtistAlbum.currentItem == 1 && ShowByAlbumDetails.isAdapterSHBALInitialized()) ShowByAlbumDetails.adapter.update()
            if (MainActivity.binding.musicArtistAlbum.currentItem == 0 && ShowByArtistDetails.isAdapterSHBARInitialized()) ShowByArtistDetails.adapter.update()
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.update()
            if (Player.isPlayingFavourites) Favourite.adapter.update()
        }

        fun setTimer(context: Context) {
            val isTimer = Player.min15 || Player.min30 || Player.min60
            if (!isTimer) showButtonSheetDialog(context)
            else {
                val builder = MaterialAlertDialogBuilder(context)
                builder.setTitle(Constants.STOP_TIMER)
                    .setMessage(Constants.WANNA_STOP_TIMER)
                    .setPositiveButton(Constants.YES) { _, _ ->
                        Player.min15 = false
                        Player.min30 = false
                        Player.min60 = false
                        Player.binding.timer.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.timer
                            )
                        )
                    }
                    .setNegativeButton(Constants.NO) { dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }

        private fun showButtonSheetDialog(context: Context) {
            val dialog = BottomSheetDialog(context)
            dialog.setContentView(R.layout.bottom_sheet_dialog)
            dialog.show()
            dialog.findViewById<LinearLayout>(R.id.min15)?.setOnClickListener {
                Player.binding.timer.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selected_timer
                    )
                )
                Player.min15 = true
                Thread {
                    Thread.sleep((15 * 60000).toLong())
                    if (Player.min15) Stuff.exitApplication()
                }.start()
                dialog.dismiss()
            }
            dialog.findViewById<LinearLayout>(R.id.min30)?.setOnClickListener {
                Player.binding.timer.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selected_timer
                    )
                )
                Player.min30 = true
                Thread {
                    Thread.sleep((30 * 60000).toLong())
                    if (Player.min30) Stuff.exitApplication()
                }.start()
                dialog.dismiss()
            }
            dialog.findViewById<LinearLayout>(R.id.min60)?.setOnClickListener {
                Player.binding.timer.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.selected_timer
                    )
                )
                Player.min60 = true
                Thread {
                    Thread.sleep((60 * 60000).toLong())
                    if (Player.min60) Stuff.exitApplication()
                }.start()
                dialog.dismiss()
            }
        }

        fun shareCurrentMusic(context: Context) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = Constants.TYPE_FOR_SHARE
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                Uri.parse(Player.musicListPA[Player.songPosition].path)
            )
            context.startActivity(Intent.createChooser(shareIntent, Constants.SHARE))
        }

        fun setCurrentMusicAsFavourite() {
            Player.fIndex = Stuff.favoriteChecker(Player.musicListPA[Player.songPosition].id)
            if (Player.fIndex != -1) {
                Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_empty_icon)
                Favourite.favoriteSongs.removeAt(Player.fIndex)
                if (Favourite.favoriteSongs.isEmpty()) {
                    Favourite.binding.instructionFV.visibility = View.VISIBLE
                }
            } else {
                Player.binding.favoritesBTN.setImageResource(R.drawable.favorite_full_icon)
                Favourite.favoriteSongs.add(Player.musicListPA[Player.songPosition])
            }
        }
    }
}