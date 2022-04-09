package com.example.musicplayer.Adaptor

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.playlist
import com.example.musicplayer.Music_Stuff.getImageArt
import com.example.musicplayer.Music_Stuff.getReflectionBackground
import com.example.musicplayer.Music_Stuff.myPlaylist
import com.example.musicplayer.R
import com.example.musicplayer.databinding.SelectPlaylistViewBinding

class SelectPlaylistAdapter(
    private val context: Context,
    private var playlistList: ArrayList<myPlaylist>
) :
    RecyclerView.Adapter<SelectPlaylistAdapter.MyHolder>() {
    class MyHolder(binding: SelectPlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.imgSPLV
        val name = binding.playlistNameSPLV
        val createdBy = binding.createdBySPLV
        val createdOn = binding.createdOnSPLV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            SelectPlaylistViewBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        if (playlist.listOfPlaylists.ref[position].musics.size > 0) {
            try {
                val img = getImageArt(
                    playlist.listOfPlaylists.ref[position].musics.get(0).path,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                )
                var image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                }

                if (image == null) {
                    image = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                }

                holder.image.setImageBitmap(getReflectionBackground(image))
            } catch (e: Exception) {
                val image = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.image_background
                )

                holder.image.setImageBitmap(getReflectionBackground(image))
            }
        } else {
            val image = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )

            holder.image.setImageBitmap(getReflectionBackground(image))
        }
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.createdBy.text = playlistList[position].createdBy
        holder.createdOn.text = playlistList[position].createdOn

        holder.root.setOnClickListener {
            playlistList[position].musics.addAll(
                MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                    AlbumViewAdapter.currentAlbum
                )]!!
            )
            Toast.makeText(context, "Musics Added Successfully", Toast.LENGTH_SHORT).show()
            AlbumViewAdapter.menu.dismiss()
        }

    }

    override fun getItemCount(): Int {
        return playlistList.size
    }
}