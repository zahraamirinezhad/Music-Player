package com.example.musicplayer

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
import com.example.musicplayer.databinding.MusicViewBinding

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>,
    private val playlistDetails: Boolean = false,
    private val selectionActivity: Boolean = false
) :
    RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songName
        val album = binding.songAlbum
        val image = binding.imgMV
        val duration = binding.songDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

        val img = getImageArt(musicList[position].path)
        val myImage = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.music_player_icon_slash_screen
            )
        }

        val dr: Drawable = BitmapDrawable(myImage)
        holder.image.setImageBitmap((dr as BitmapDrawable).bitmap)

        when {
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlaylistDetailsAdapter", position)
                }
            }

            selectionActivity -> {
                var itExists: Boolean = false
                playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.forEachIndexed { index, music ->
                    if (musicList[position].id.equals(music.id)) {
                        itExists = true
                    }
                }
                if (itExists) {
                    holder.root.isEnabled = false
                    holder.root.alpha = 0.6F
                } else {
                    holder.root.setOnClickListener {
                        if (addSong(musicList[position])) {
                            holder.root.setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.light_blue
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

            else -> {
                holder.root.setOnClickListener {
                    when {
                        MainActivity.search -> sendIntent("MusicAdapterSearch", position)
                        musicList[position].id == Player.nowPlayingID -> sendIntent(
                            "NowPlaying",
                            position
                        )
                        else -> sendIntent("MusicAdapter", position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, Player::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun addSong(song: Music): Boolean {
        playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.forEachIndexed { index, music ->
            if (song.id.equals(music.id)) {
                playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.removeAt(index)
                return false
            }
        }
        playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        musicList = ArrayList()
        musicList = playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics
        notifyDataSetChanged()
    }
}