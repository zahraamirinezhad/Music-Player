package com.example.musicplayer.Adaptor

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.musicplayer.Activity.Playlist
import com.example.musicplayer.Activity.PlaylistDetails
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R

class SelectionAdapter(
    context: Context,
    musicList: ArrayList<Music>,
    selectAll: Boolean = false
) : Adapter(context, musicList, selectAll) {


    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = Stuff.formatDuration(musicList[position].duration)
        holder.image.setImageBitmap(getSongImage(position))

        if (!selectAll) {
            var exist = false
            for (song in Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics) {
                if (musicList[position].id == song.id) {
                    exist = true
                }
            }
            if (exist) {
                holder.root.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.cool_pink
                    )
                )
            } else {
                holder.root.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            }
        } else {
            holder.root.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.cool_pink
                )
            )
        }

        holder.root.setOnClickListener {
            if (addSong(musicList[position])) {
                holder.root.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.cool_pink
                    )
                )
            } else {
                holder.root.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            }
        }
    }
}