package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.*
import com.example.musicplayer.Activity.PlayNext
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.Music_Stuff.Constants.Companion.CANT_REMOVE_CURRENT_PLAYING
import com.example.musicplayer.Music_Stuff.Constants.Companion.REMOVE
import com.example.musicplayer.databinding.DetailsViewBinding
import com.example.musicplayer.databinding.MoreFeatureBinding
import com.example.musicplayer.databinding.PlayNextViewBinding
import com.google.android.material.snackbar.Snackbar

class PlayNextAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>,
) :
    RecyclerView.Adapter<PlayNextAdapter.MyHolder>() {
    class MyHolder(binding: PlayNextViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNamePLN
        val image = binding.songImgPLN
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlayNextViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        if (Player.musicListPA[Player.songPosition].image == null) {
            try {
                val img = Stuff.getImageArt(Player.musicListPA[Player.songPosition].path)
                val image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                }
                Player.musicListPA[Player.songPosition].image = image
                holder.image.setImageBitmap(Player.musicListPA[Player.songPosition].image)
            } catch (e: Exception) {
                Player.musicListPA[Player.songPosition].image = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.image_background
                )
                holder.image.setImageBitmap(Player.musicListPA[Player.songPosition].image)
            }
        } else {
            holder.image.setImageBitmap(Player.musicListPA[Player.songPosition].image)
        }

        holder.title.text = musicList[position].title
        holder.title.isSelected = true

        holder.root.setOnLongClickListener {
            val customDialog =
                LayoutInflater.from(context)
                    .inflate(R.layout.more_feature, holder.root, false)
            val bindingMF = MoreFeatureBinding.bind(customDialog)
            val dialog = CustomDialog.getDialogForOnLongClickListener(context, customDialog)
            bindingMF.AddToPNBtn.text = REMOVE
            bindingMF.deleteBtn.visibility = View.GONE
            bindingMF.AddToPNBtn.setOnClickListener {
                if (position == Player.songPosition)
                    Snackbar.make(
                        (context as Activity).findViewById(R.id.linearLayoutPN),
                        CANT_REMOVE_CURRENT_PLAYING, Snackbar.LENGTH_SHORT
                    ).show()
                else {
                    if (Player.songPosition < position && Player.songPosition != 0) --Player.songPosition
                    PlayNext.playNextList.removeAt(position)
                    Player.musicListPA.removeAt(position)
                    notifyItemRemoved(position)
                }
                dialog.dismiss()
            }
            bindingMF.infoBtn.setOnClickListener {
                dialog.dismiss()
                val detailsDialog = LayoutInflater.from(context)
                    .inflate(R.layout.details_view, bindingMF.root, false)
                val binder = DetailsViewBinding.bind(detailsDialog)
                binder.detailsTV.setTextColor(Color.WHITE)
                binder.root.setBackgroundColor(Color.TRANSPARENT)
                CustomDialog.getInfoDialog(context, detailsDialog)
                val str = Stuff.getDetails(music = musicList[position])
                binder.detailsTV.text = str
            }
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}