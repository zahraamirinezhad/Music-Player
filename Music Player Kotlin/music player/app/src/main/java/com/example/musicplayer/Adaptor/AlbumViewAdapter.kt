package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Activity.ShowByAlbumDetails
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.NowPlaying
import com.example.musicplayer.Music_Stuff.getImageArt
import com.example.musicplayer.R
import com.example.musicplayer.databinding.AlbumViewBinding

class AlbumViewAdapter(
    private val context: Context, private var listOfAlbums: LinkedHashMap<String, ArrayList<Music>>
) :
    RecyclerView.Adapter<AlbumViewAdapter.MyHolder>() {

    class MyHolder(binding: AlbumViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.albumImage
        val name = binding.albumName
        val root = binding.root
        val play = binding.play
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(AlbumViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = listOfAlbums.keys.elementAt(position)
        holder.name.isSelected = true
        if (Player.isMusicListPaInitialized() && Player.musicListPA[0].id == listOfAlbums[listOfAlbums.keys.elementAt(
                position
            )]?.get(0)?.id && Player.isPlaying
        ) {
            holder.play.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.pause_music
                )
            )
        } else {
            holder.play.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.play_music
                )
            )
        }
        if (listOfAlbums[listOfAlbums.keys.elementAt(position)]?.size != 0) {
            try {
                val img = getImageArt(
                    listOfAlbums[listOfAlbums.keys.elementAt(position)]?.get(
                        0
                    )!!.path,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                )
                val image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                }

                val dr: Drawable = BitmapDrawable(image)
                holder.image.setImageBitmap((dr as BitmapDrawable).bitmap)
            } catch (e: Exception) {
                val image = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.image_background
                )

                val dr: Drawable = BitmapDrawable(image)
                holder.image.setImageBitmap((dr as BitmapDrawable).bitmap)
            }
        } else {
            val image = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )

            val dr: Drawable = BitmapDrawable(image)
            holder.image.setImageBitmap((dr as BitmapDrawable).bitmap)
        }

        holder.play.setOnClickListener {
            if (!Player.isMusicListPaInitialized() || Player.musicListPA != listOfAlbums[listOfAlbums.keys.elementAt(
                    position
                )]
            ) {

                val intent2 = Intent(context, ShowByAlbumDetails::class.java)
                intent2.putExtra(
                    "prevIndex",
                    if (ShowByAlbumDetails.prevAlbum != -1) ShowByAlbumDetails.prevAlbum else 0
                )
                intent2.putExtra("index", position)
                intent2.putExtra("ItsFromPlayBTN", true)
                ContextCompat.startActivity(context, intent2, null)

                holder.play.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.pause_music
                    )
                )
            } else if (!Player.isPlaying) {
                holder.play.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.pause_music
                    )
                )
                playMusic()
            } else if (Player.isPlaying) {
                holder.play.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.play_music
                    )
                )
                pauseMusic()
            }
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, ShowByAlbumDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return listOfAlbums.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        notifyItemChanged(ShowByAlbumDetails.currentAlbum)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(pos: Int) {
        notifyItemChanged(pos)
    }

    private fun playMusic() {
        Player.isPlaying = true
        if (Player.musicService != null) {
            Player.musicService!!.mediaPlayer!!.start()
            Player.musicService!!.showNotification(R.drawable.pause_music)
        }
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.pause_music)
    }

    private fun pauseMusic() {
        Player.isPlaying = false
        if (Player.musicService != null) {
            Player.musicService!!.mediaPlayer!!.pause()
            Player.musicService!!.showNotification(R.drawable.play_music)
        }
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.play_music)

    }
}