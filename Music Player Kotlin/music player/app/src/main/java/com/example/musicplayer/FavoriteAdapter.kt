package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.FavoriteViewBinding

class FavoriteAdapter(private val context: Context, private var musicList: ArrayList<Music>) :
    RecyclerView.Adapter<FavoriteAdapter.MyHolder>() {
    class MyHolder(binding: FavoriteViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.songImgFA
        val name = binding.songNameFA
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavoriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        Glide.with(context).load(musicList[position].artUri).apply(
            RequestOptions().placeholder(R.drawable.music_player_icon_slash_screen).centerCrop()
        ).into(holder.image)
        holder.name.text = musicList[position].title
        holder.name.isSelected = true
        holder.root.setOnClickListener {
            sendIntent("FavoriteAdapter", position)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, Player::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }
}