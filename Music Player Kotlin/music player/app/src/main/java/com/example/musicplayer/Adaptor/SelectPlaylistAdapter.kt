package com.example.musicplayer.Adaptor

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.Playlist
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSICS_ADDED_SUCCESSFULLY
import com.example.musicplayer.R
import com.example.musicplayer.databinding.SelectPlaylistViewBinding

class SelectPlaylistAdapter(
    private val context: Context,
    private var playlistList: ArrayList<MyPlaylist>
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
        if (Playlist.listOfPlaylists.ref[position].musics.size > 0) {
            if (Playlist.listOfPlaylists.ref[position].musics[0].image == null) {
                try {
                    val img =
                        Stuff.getImageArt(Playlist.listOfPlaylists.ref[position].musics[0].path)
                    val image = if (img != null) {
                        BitmapFactory.decodeByteArray(img, 0, img.size)
                    } else {
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.image_background
                        )
                    }
                    Playlist.listOfPlaylists.ref[position].musics[0].image = image
                    holder.image.setImageBitmap(ImageFormatter.getReflectionBackground(Playlist.listOfPlaylists.ref[position].musics[0].image!!))
                } catch (e: Exception) {
                    Playlist.listOfPlaylists.ref[position].musics[0].image =
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.image_background
                        )
                    holder.image.setImageBitmap(ImageFormatter.getReflectionBackground(Playlist.listOfPlaylists.ref[position].musics[0].image!!))
                }
            } else {
                holder.image.setImageBitmap(ImageFormatter.getReflectionBackground(Playlist.listOfPlaylists.ref[position].musics[0].image!!))
            }
        } else {
            val image = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )

            holder.image.setImageBitmap(ImageFormatter.getReflectionBackground(image))
        }
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.createdBy.text = playlistList[position].createdBy
        holder.createdOn.text = playlistList[position].createdOn

        holder.root.setOnClickListener {
            if (MainActivity.binding.musicArtistAlbum.currentItem == 1) {
                for (music in MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                    AlbumViewAdapter.currentAlbum
                )]!!) {
                    if (!Stuff.doesListContainsThisMusic(playlistList[position].musics, music.id)) {
                        playlistList[position].musics.add(music)
                    }
                }
                Toast.makeText(context, MUSICS_ADDED_SUCCESSFULLY, Toast.LENGTH_SHORT).show()
                AlbumViewAdapter.menu.dismiss()
            } else if (MainActivity.binding.musicArtistAlbum.currentItem == 0) {
                for (music in MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                    ArtistViewAdapter.currentArtist
                )]!!) {
                    if (!Stuff.doesListContainsThisMusic(playlistList[position].musics, music.id)) {
                        playlistList[position].musics.add(music)
                    }
                }
                Toast.makeText(context, MUSICS_ADDED_SUCCESSFULLY, Toast.LENGTH_SHORT).show()
                ArtistViewAdapter.menu.dismiss()
            }
        }

    }

    override fun getItemCount(): Int {
        return playlistList.size
    }
}