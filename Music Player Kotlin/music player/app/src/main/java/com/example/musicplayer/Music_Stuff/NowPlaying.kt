package com.example.musicplayer.Music_Stuff

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Activity.PlaylistDetails
import com.example.musicplayer.Activity.ShowByAlbumDetails
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {
    companion object {
        lateinit var binding: FragmentNowPlayingBinding
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

            setSongPosition(prevOrNext)
            MainActivity.musicAdapter.update()
            if (ShowByAlbumDetails.isAdapterSHBALInitialized()) ShowByAlbumDetails.adapter.update()
            if (Player.isPlayingPlaylist) PlaylistDetails.adapter.update()

            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()

            val img = getImageArt(
                Player.musicListPA[Player.songPosition].path, BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.image_background
                )
            )
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.image_background
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            binding.songImgNP.setImageBitmap((dr as BitmapDrawable).bitmap)

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            binding.songNameNP.text = Player.musicListPA[Player.songPosition].title


            if (!prevOrNext)
                Player.musicService!!.showNotification(R.drawable.play_music)
            else Player.musicService!!.showNotification(R.drawable.pause_music)

            playMusic()
            if (ShowByAlbumDetails.isAdapterSHBALInitialized())
                ShowByAlbumDetails.adapter.update()
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        if (Player.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true

            binding.songNameNP.text = Player.musicListPA[Player.songPosition].title
            if (Player.isPlaying) binding.playPauseNP.setIconResource(R.drawable.pause_music)
            else binding.playPauseNP.setIconResource(R.drawable.play_music)

            val img = getImageArt(
                Player.musicListPA[Player.songPosition].path, BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.image_background
                )
            )
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.image_background
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            binding.songImgNP.setImageBitmap((dr as BitmapDrawable).bitmap)
        }
    }

    private fun playMusic() {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        binding.playPauseNP.setIconResource(R.drawable.pause_music)
        Player.musicService!!.showNotification(R.drawable.pause_music)
        MainActivity.albumAdapter.update()
    }

    private fun pauseMusic() {
        Player.isPlaying = false
        Player.musicService!!.mediaPlayer!!.pause()
        binding.playPauseNP.setIconResource(R.drawable.play_music)
        Player.musicService!!.showNotification(R.drawable.play_music)
        MainActivity.albumAdapter.update()
    }
}