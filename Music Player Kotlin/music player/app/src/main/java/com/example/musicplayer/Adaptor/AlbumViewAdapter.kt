package com.example.musicplayer.Adaptor

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.ShowByAlbumDetails
import com.example.musicplayer.Music_Stuff.Music
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(AlbumViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = listOfAlbums.keys.elementAt(position)
        holder.name.isSelected = true
        if (MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(position)]?.size != 0) {
            try {
                val img = getImageArt(
                    MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(position)]?.get(
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

        holder.root.setOnClickListener {
            val intent = Intent(context, ShowByAlbumDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return listOfAlbums.size
    }
}