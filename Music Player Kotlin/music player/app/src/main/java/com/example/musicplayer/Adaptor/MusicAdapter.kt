package com.example.musicplayer.Adaptor

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Activity.PlayNext
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSIC_ADAPTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSIC_ADAPTER_SEARCH
import com.example.musicplayer.Music_Stuff.Constants.Companion.NOW_PLAYING
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAY_SONG_FIRST
import com.example.musicplayer.Music_Stuff.CustomDialog
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R
import com.example.musicplayer.databinding.DetailsViewBinding
import com.example.musicplayer.databinding.MoreFeatureBinding
import com.google.android.material.snackbar.Snackbar


class MusicAdapter(
    context: Context,
    musicList: ArrayList<Music>
) : Adapter(context, musicList) {

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.genre.text = musicList[position].genre
        holder.duration.text = Stuff.formatDuration(musicList[position].duration)

        setImage(musicList[position].path, holder.image, musicList[position])

        if (Player.isMusicListPaInitialized() && Player.musicListPA.size != 0 && musicList[position].id == Player.musicListPA[Player.songPosition].id) {
            holder.root.setBackgroundResource(R.drawable.fragment_background)
        } else {
            holder.root.background = null
        }

        holder.root.setOnLongClickListener {
            val customDialog =
                LayoutInflater.from(super.context)
                    .inflate(R.layout.more_feature, holder.root, false)
            val bindingMF = MoreFeatureBinding.bind(customDialog)
            val dialog = CustomDialog.getDialogForOnLongClickListener(super.context, customDialog)

            bindingMF.AddToPNBtn.setOnClickListener {
                try {
                    if (PlayNext.playNextList.isEmpty()) {
                        PlayNext.playNextList.add(Player.musicListPA[Player.songPosition])
                        Player.songPosition = 0
                    }

                    PlayNext.playNextList.add(musicList[position])
                    Player.musicListPA = ArrayList()
                    Player.musicListPA.addAll(PlayNext.playNextList)
                } catch (e: Exception) {
                    Snackbar.make(context, holder.root, PLAY_SONG_FIRST, 3000).show()
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

            bindingMF.deleteBtn.setOnClickListener {
                dialog.dismiss()
                deleteMusic(position)
            }

            return@setOnLongClickListener true
        }

        holder.root.setOnClickListener {
            when {
                MainActivity.search -> sendIntent(MUSIC_ADAPTER_SEARCH, position)
                musicList[position].id == Player.nowPlayingID -> sendIntent(
                    NOW_PLAYING,
                    position
                )
                else -> {
                    sendIntent(MUSIC_ADAPTER, position)
                }
            }
        }
    }
}