package com.example.musicplayer.Music_Stuff

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.R

class playing_song_image : Fragment() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var playPause: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var songImage: ImageView
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playing_song_image, container, false)
        playPause = rootView.findViewById(R.id.pauseOrPlay)
        songImage = rootView.findViewById(R.id.songImgPA)

        Player.mainImageAnimator = ObjectAnimator.ofFloat(
            songImage,
            "rotation",
            (Math.toDegrees(2 * Math.PI)).toFloat()
        )
        Player.mainImageAnimator.repeatCount = Animation.INFINITE
        Player.mainImageAnimator.duration = 20000
        Player.mainImageAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animation.removeListener(this)
                animation.duration = 0
                (animation as ValueAnimator).reverse()
            }
        })
        Player.mainImageAnimator.start()
        // Inflate the layout for this fragment
        return rootView
    }

    override fun onResume() {
        super.onResume()

        val img = Stuff.getImageArt(
            Player.musicListPA[Player.songPosition].path, BitmapFactory.decodeResource(
                this.resources,
                R.drawable.image_background
            )
        )
        var image = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                resources,
                R.drawable.image_background
            )
        }
        if (image == null) {
            image = BitmapFactory.decodeResource(
                resources,
                R.drawable.image_background
            )
        }

        val dr: Drawable = BitmapDrawable(image)
        if (NowPlaying.isBindingNPInitialized()) NowPlaying.binding.songImgNP.background = dr

        val output = Bitmap.createBitmap(
            image.width,
            image.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, image.width, image.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(
            (image.width / 2).toFloat(), (image.height / 2).toFloat(),
            (image.width / 3).toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(image, rect, rect, paint)

        songImage.setImageBitmap(output)
    }
}