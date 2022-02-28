package com.example.musicplayer

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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {
    companion object {
        lateinit var binding: FragmentNowPlayingBinding
    }

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
            try {
                if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                    MediaPlayer()
                setSongPosition(true)
                Player.musicService!!.mediaPlayer!!.reset()
                Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
                Player.musicService!!.mediaPlayer!!.prepare()
                Glide.with(this).load(Player.musicListPA[Player.songPosition].artUri).apply(
                    RequestOptions().placeholder(R.drawable.image_background)
                        .centerCrop()
                ).into(Player.binding.songImgPA)
                Player.binding.songNamePA.text = Player.musicListPA[Player.songPosition].title

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

                Player.musicService!!.showNotification(R.drawable.pause_music)
                Player.nowPlayingID = Player.musicListPA[Player.songPosition].id

//                mainImageAnimator = ObjectAnimator.ofFloat(
//                    binding.songImgNP,
//                    "rotation",
//                    (Player.musicService!!.mediaPlayer!!.duration.toLong() * Math.toDegrees(2 * Math.PI) / 50000).toFloat()
//                )
//                mainImageAnimator.repeatCount = Animation.INFINITE
//                mainImageAnimator.addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        animation.removeListener(this)
//                        animation.duration = 0
//                        (animation as ValueAnimator).reverse()
//                    }
//                })
//                mainImageAnimator.duration = Player.musicService!!.mediaPlayer!!.duration.toLong()
//                mainImageAnimator.start()
                playMusic()
            } catch (e: Exception) {

            }
        }

        binding.backNP.setOnClickListener {
            try {
                if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
                    MediaPlayer()
                setSongPosition(false)
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


                Player.musicService!!.showNotification(R.drawable.play_music)

//                mainImageAnimator = ObjectAnimator.ofFloat(
//                    binding.songImgNP,
//                    "rotation",
//                    (Player.musicService!!.mediaPlayer!!.duration.toLong() * Math.toDegrees(2 * Math.PI) / 50000).toFloat()
//                )
//                mainImageAnimator.repeatCount = Animation.INFINITE
//                mainImageAnimator.addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        animation.removeListener(this)
//                        animation.duration = 0
//                        (animation as ValueAnimator).reverse()
//                    }
//                })
//                mainImageAnimator.duration = Player.musicService!!.mediaPlayer!!.duration.toLong() + 10000
//                mainImageAnimator.start()

                playMusic()
            } catch (e: Exception) {

            }
        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), Player::class.java)
            intent.putExtra("index", Player.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
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
    }

    private fun pauseMusic() {
        Player.isPlaying = false
        Player.musicService!!.mediaPlayer!!.pause()
        binding.playPauseNP.setIconResource(R.drawable.play_music)
        Player.musicService!!.showNotification(R.drawable.play_music)
    }
}