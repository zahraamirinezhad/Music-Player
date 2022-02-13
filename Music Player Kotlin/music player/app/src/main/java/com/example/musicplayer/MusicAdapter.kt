package com.example.musicplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.MusicViewBinding

class MusicAdapter(private val context : Context, private val musicList : ArrayList<Music>) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding : MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songName
        val album = binding.songAlbum
        val image = binding.imgMV
        val duration = binding.songDuration
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = musicList[position].duration.toString()
        Glide.with(context).load(musicList[position].artUri).apply(RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop()).into(holder.image)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}