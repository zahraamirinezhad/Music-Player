package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.FavoriteViewBinding
import com.example.musicplayer.databinding.MoreFeatureBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class FavoriteAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>,
    private var playNext: Boolean = false
) :
    RecyclerView.Adapter<FavoriteAdapter.MyHolder>() {
    class MyHolder(binding: FavoriteViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.songImgFA
        val name = binding.songNameFA
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavoriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        val img = getImageArt(
            musicList[position].path, BitmapFactory.decodeResource(
                context.resources,
                R.drawable.music_player_icon_slash_screen
            )
        )
        val myImage = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )
        }

        val dr: Drawable = BitmapDrawable(myImage)
        holder.image.setImageBitmap(getReflectionBackground((dr as BitmapDrawable).bitmap))

        holder.name.text = musicList[position].title
        holder.name.isSelected = true

        if (playNext) {
            holder.root.setOnClickListener {
                sendIntent("PlayNext", position)
            }
            holder.root.setOnLongClickListener {
                val customDialog =
                    LayoutInflater.from(context).inflate(R.layout.more_feature, holder.root, false)
                val bindingMF = MoreFeatureBinding.bind(customDialog)
                val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
                    .create()
                dialog.show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                bindingMF.AddToPNBtn.text = "Remove"
                bindingMF.AddToPNBtn.setOnClickListener {
                    if (position == Player.songPosition)
                        Snackbar.make(
                            (context as Activity).findViewById(R.id.linearLayoutPN),
                            "Can't Remove Currently Playing Song.", Snackbar.LENGTH_SHORT
                        ).show()
                    else {
                        if (Player.songPosition < position && Player.songPosition != 0) --Player.songPosition
                        PlayNext.playNextList.removeAt(position)
                        Player.musicListPA.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    dialog.dismiss()
                }
                return@setOnLongClickListener true
            }
        } else {
            holder.root.setOnClickListener {
                refreshBackground()
                MainActivity.MusicListMA[findMusicById(musicList[position])].isPlayingOrNot = true
                MainActivity.musicAdapter.updateMusicList(MainActivity.MusicListMA)
                sendIntent("FavoriteAdapter", position)
            }
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

    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(newList)
        notifyDataSetChanged()
    }
}