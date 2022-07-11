package com.example.musicplayer.Adaptor

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.example.musicplayer.Activity.*
import com.example.musicplayer.Music_Stuff.CustomDialog
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R
import com.example.musicplayer.databinding.DetailsViewBinding
import com.example.musicplayer.databinding.MoreFeatureBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File

class FavouritesAdapter (
    context: Context,
    musicList: ArrayList<Music>
) : Adapter(context, musicList) {

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = Stuff.formatDuration(musicList[position].duration)
        holder.image.setImageBitmap(getSongImage(position))

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
                    Snackbar.make(context, holder.root, "Play A Song First!!", 3000).show()
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
                val file = File(musicList[position].path)
                if (file.exists()) {
                    val removed = musicList[position]

                    if (Favourite.favoriteSongs.contains(removed)) {
                        Favourite.favoriteSongs.remove(removed)
                    }
                    if (Playlist.listOfPlaylists.ref.size != 0) {
                        for (x in Playlist.listOfPlaylists.ref) {
                            if (x.musics.contains(removed)) {
                                x.musics.remove(removed)
                            }
                        }
                    }
                    for (x in MainActivity.songByAlbum.keys) {
                        if (MainActivity.songByAlbum[x]!![MainActivity.songByAlbum[x]!!.size - 1].id == removed.id) {
                            MainActivity.songByAlbum[x]!!.removeLast()
                            break
                        } else {
                            for (i in 0 until MainActivity.songByAlbum[x]!!.size) {
                                if (MainActivity.songByAlbum[x]!![i].id == removed.id) {
                                    MainActivity.songByAlbum[x]!!.removeAt(i)
                                    break
                                }

                            }
                        }
                    }
                    for (x in MainActivity.songByArtist.keys) {
                        if (MainActivity.songByArtist[x]!![MainActivity.songByArtist[x]!!.size - 1].id == removed.id) {
                            MainActivity.songByArtist[x]!!.removeLast()
                            break
                        } else {
                            for (i in 0 until MainActivity.songByArtist[x]!!.size) {
                                if (MainActivity.songByArtist[x]!![i].id == removed.id) {
                                    MainActivity.songByArtist[x]!!.removeAt(i)
                                    break
                                }

                            }
                        }
                    }
                    if (Player.isMusicListPaInitialized() && Player.musicListPA[Player.songPosition].id == removed.id) {
                        Player.musicListPA.removeAt(Player.songPosition)
                        next()
                    } else if (Player.isMusicListPaInitialized()) {
                        for (i in 0 until Player.musicListPA.size) {
                            if (Player.musicListPA[i].id == removed.id) {
                                if (i < Player.songPosition)
                                    Player.songPosition--
                                Player.musicListPA.removeAt(i)
                                break
                            }
                        }
                    }
                    MainActivity.MusicListMA.remove(removed)
                    if (MainActivity.binding.musicRV.adapter!!.javaClass.toString() == context.getString(
                            R.string.album_adapter_class
                        )
                    ) {
                        musicList = ArrayList()
                        musicList.addAll(
                            MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                                ShowByAlbumDetails.currentAlbum
                            )]!!
                        )
                        ShowByAlbumDetails.adapter.update()
                    } else if (MainActivity.binding.musicRV.adapter!!.javaClass.toString() == context.getString(
                            R.string.artist_adapter_class
                        )
                    ) {
                        musicList = ArrayList()
                        musicList.addAll(
                            MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                                ShowByArtistDetails.currentArtist
                            )]!!
                        )
                        ShowByArtistDetails.adapter.update()
                    } else if (MainActivity.binding.musicRV.adapter!!.javaClass.toString() == context.getString(
                            R.string.music_adapter_class
                        )
                    ) {
                        musicList = ArrayList()
                        musicList.addAll(MainActivity.MusicListMA)
                        MainActivity.musicAdapter.update()
                    }

//                            file.delete()
                }
            }

            return@setOnLongClickListener true
        }

        holder.root.setOnClickListener {
            sendIntent("FavoriteAdapter", position)
        }
    }
}