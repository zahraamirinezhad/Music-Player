package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistViewAdapter(
    private val context: Context,
    private var playlistList: ArrayList<myPlaylist>
) :
    RecyclerView.Adapter<PlaylistViewAdapter.MyHolder>() {
    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImage
        val name = binding.playlistName
        val root = binding.root
        val deleteBTN = binding.deletePL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.deleteBTN.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete this playlist ?")
                .setPositiveButton("YES") { dialog, _ ->
                    playlist.listOfPlaylists.ref.removeAt(position)
                    refresh()
                    dialog.dismiss()
                }
                .setNegativeButton("NO") { dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }

        if (playlist.listOfPlaylists.ref[position].musics.size > 0) {
            try {
                val img = getImageArt(playlist.listOfPlaylists.ref[position].musics.get(0).path)
                val image = if (img != null) {
                    BitmapFactory.decodeByteArray(img, 0, img.size)
                } else {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.music_player_icon_slash_screen
                    )
                }

                val dr: Drawable = BitmapDrawable(image)
                holder.image.setImageBitmap(getReflectionBackground((dr as BitmapDrawable).bitmap))
            } catch (e: Exception) {
                val image = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.music_player_icon_slash_screen
                )

                val dr: Drawable = BitmapDrawable(image)
                holder.image.setImageBitmap(getReflectionBackground((dr as BitmapDrawable).bitmap))
            }
        } else {
            val image = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.music_player_icon_slash_screen
            )

            val dr: Drawable = BitmapDrawable(image)
            holder.image.setImageBitmap(getReflectionBackground((dr as BitmapDrawable).bitmap))
        }
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        playlistList = ArrayList()
        playlistList.addAll(playlist.listOfPlaylists.ref)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, Player::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }
}