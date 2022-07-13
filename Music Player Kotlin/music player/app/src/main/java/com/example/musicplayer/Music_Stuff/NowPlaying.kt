package com.example.musicplayer.Music_Stuff

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicplayer.Activity.*
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {
    companion object {
        lateinit var binding: FragmentNowPlayingBinding
        fun isBindingNPInitialized(): Boolean {
            return this::binding.isInitialized
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        binding.playPauseNP.setOnClickListener {
            if (Player.isPlaying) pauseMusic() else playMusic()
        }
        binding.nextNP.setOnClickListener {
            prevNext(true)
        }

        binding.backNP.setOnClickListener {
            prevNext(false)
        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), Player::class.java)
            intent.putExtra("index", Player.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    private fun prevNext(prevOrNext: Boolean) {
        try {
            if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                MediaPlayer()

            if (Player.isShuffle)
                Stuff.setSongPositionShuffle()
            else Stuff.setSongPosition(prevOrNext)
            MainActivity.musicAdapter.update()
            if (ShowByAlbumDetails.isAdapterSHBALInitialized()) ShowByAlbumDetails.adapter.update()
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.update()
            if (Player.isPlayingFavourites) Favourite.adapter.update()

            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()

            Glide.with(binding.root).asBitmap().load(
                if (Stuff.getImageArt(Player.musicListPA[Player.songPosition].path) == null) BitmapFactory.decodeResource(
                    binding.root.resources,
                    R.drawable.image_background
                ) else Stuff.getImageArt(
                    Player.musicListPA[Player.songPosition].path
                )
            ).into(binding.songImgNP)

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            binding.songNameNP.text = Player.musicListPA[Player.songPosition].title


            if (!prevOrNext)
                Player.musicService!!.showNotification(
                    Stuff.playingState(),
                    Stuff.favouriteState(),
                    Stuff.musicState()
                )
            else Player.musicService!!.showNotification(
                Stuff.playingState(),
                Stuff.favouriteState(),
                Stuff.musicState()
            )

            playMusic()
            if (ShowByAlbumDetails.isAdapterSHBALInitialized())
                ShowByAlbumDetails.adapter.update()
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        if (Player.musicService != null && Player.isMusicListPaInitialized() && Player.musicListPA.size != 0) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true

            binding.songNameNP.text = Player.musicListPA[Player.songPosition].title
            if (Player.isPlaying) binding.playPauseNP.setIconResource(R.drawable.pause_music)
            else binding.playPauseNP.setIconResource(R.drawable.play_music)

            Glide.with(binding.root).asBitmap().load(
                if (Stuff.getImageArt(Player.musicListPA[Player.songPosition].path) == null) BitmapFactory.decodeResource(
                    binding.root.resources,
                    R.drawable.image_background
                ) else Stuff.getImageArt(
                    Player.musicListPA[Player.songPosition].path
                )
            ).into(binding.songImgNP)

            if (Player.musicListPA[Player.songPosition].image == null) {
                try {
                    val img = Stuff.getImageArt(Player.musicListPA[Player.songPosition].path)
                    val image = if (img != null) {
                        BitmapFactory.decodeByteArray(img, 0, img.size)
                    } else {
                        BitmapFactory.decodeResource(
                            context?.resources,
                            R.drawable.image_background
                        )
                    }
                    Player.musicListPA[Player.songPosition].image = image
                    binding.songImgNP.setImageBitmap(Player.musicListPA[Player.songPosition].image)
                } catch (e: Exception) {
                    Player.musicListPA[Player.songPosition].image = BitmapFactory.decodeResource(
                        context?.resources,
                        R.drawable.image_background
                    )
                    binding.songImgNP.setImageBitmap(Player.musicListPA[Player.songPosition].image)
                }
            } else {
                binding.songImgNP.setImageBitmap(Player.musicListPA[Player.songPosition].image)
            }


        } else {
            binding.root.visibility = View.INVISIBLE
        }
    }

    private fun playMusic() {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        binding.playPauseNP.setIconResource(R.drawable.pause_music)
        Player.musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.favouriteState(),
            Stuff.musicState()
        )
    }

    private fun pauseMusic() {
        Player.isPlaying = false
        Player.musicService!!.mediaPlayer!!.pause()
        binding.playPauseNP.setIconResource(R.drawable.play_music)
        Player.musicService!!.showNotification(
            Stuff.playingState(),
            Stuff.favouriteState(),
            Stuff.musicState()
        )
    }
}