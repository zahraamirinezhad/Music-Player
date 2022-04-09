package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.*
import com.example.musicplayer.Activity.*
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.databinding.DetailsViewBinding
import com.example.musicplayer.databinding.MoreFeatureBinding
import com.example.musicplayer.databinding.MusicViewBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MusicAdapter(
    private val context: Context,
    var musicList: ArrayList<Music>,
    val playlistDetails: Boolean = false,
    val selectionActivity: Boolean = false,
    val album: Boolean = false,
    var selectAll: Boolean = false
) :
    RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songName
        val album = binding.songAlbum
        val image = binding.imgMV
        val duration = binding.songDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: MyHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

        if (!selectionActivity) {
            if (Player.isMusicListPaInitialized() && Player.musicListPA.size != 0 && musicList[position].id == Player.musicListPA[Player.songPosition].id) {
                holder.root.setBackgroundResource(R.drawable.fragment_background)
            } else {
                holder.root.background = null
            }
        } else {
            if (!selectAll) {
                var exist = false
                for (song in playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics) {
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
        }

        val img = getImageArt(
            musicList[position].path, BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )
        )
        var myImage = if (img != null) {
            BitmapFactory.decodeByteArray(img, 0, img.size)
        } else {
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )
        }

        if (myImage == null) {
            myImage = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )
        }

        val dr: Drawable = BitmapDrawable(myImage)
        holder.image.setImageBitmap((dr as BitmapDrawable).bitmap)

        if (!selectionActivity)
            holder.root.setOnLongClickListener {
                val customDialog =
                    LayoutInflater.from(context).inflate(R.layout.more_feature, holder.root, false)
                val bindingMF = MoreFeatureBinding.bind(customDialog)
                val dialog = getDialogForOnLongClickListener(context, customDialog)
                if (playlistDetails)
                    bindingMF.deleteBtn.text = "Remove"

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
                    getInfoDialog(context, detailsDialog)
                    val str = getDetails(music = musicList[position])
                    binder.detailsTV.text = str
                }

                bindingMF.deleteBtn.setOnClickListener {
                    dialog.dismiss()
                    if (playlistDetails) {
                        if (Player.isMusicListPaInitialized() && Player.musicListPA == playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics && position < Player.songPosition) {
                            Player.songPosition--
                            Player.musicListPA.removeAt(position)
                            playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.removeAt(
                                position
                            )
                        } else if (Player.isMusicListPaInitialized() && Player.musicListPA == playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics && position == Player.songPosition) {
                            Player.musicListPA.removeAt(position)
                            playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.removeAt(
                                position
                            )
                            next()
                        } else {
                            playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.removeAt(
                                position
                            )
                        }

                        refreshPlaylist()
                    } else {
                        val file = File(musicList[position].path)
                        if (file.exists()) {
                            val removed = musicList[position]

                            if (favourite.favoriteSongs.contains(removed)) {
                                favourite.favoriteSongs.remove(removed)
                            }
                            if (playlist.listOfPlaylists.ref.size != 0) {
                                for (x in playlist.listOfPlaylists.ref) {
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
                            if (MainActivity.binding.musicRV.adapter!!.javaClass.toString() == "class com.example.musicplayer.Adaptor.AlbumViewAdapter"
                            ) {
                                musicList = ArrayList()
                                musicList.addAll(
                                    MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                                        ShowByAlbumDetails.currentAlbum
                                    )]!!
                                )
                                ShowByAlbumDetails.adapter.update()
                            } else if (MainActivity.binding.musicRV.adapter!!.javaClass.toString() == "class com.example.musicplayer.Adaptor.MusicAdapter"
                            ) {
                                musicList = ArrayList()
                                musicList.addAll(MainActivity.MusicListMA)
                                MainActivity.musicAdapter.update()
                            }

                            file.delete()
                        }
                    }
                }

                return@setOnLongClickListener true
            }

        when {
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlaylistDetailsAdapter", position)
                }
            }

            album -> {
                holder.root.setOnClickListener {
                    sendIntent("AlbumDetailsAdapter", position)
                }
            }

            selectionActivity -> {
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

            else -> {
                holder.root.setOnClickListener {
                    when {
                        MainActivity.search -> sendIntent("MusicAdapterSearch", position)
                        musicList[position].id == Player.nowPlayingID -> sendIntent(
                            "NowPlaying",
                            position
                        )
                        else -> {
                            sendIntent("MusicAdapter", position)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(list: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(list)
        notifyDataSetChanged()
    }

    fun next() {
//        if (Player.musicService!!.mediaPlayer == null) Player.musicService!!.mediaPlayer =
//            MediaPlayer()
//
//
        if (Player.musicListPA.size != 0) {
            if (Player.songPosition == Player.musicListPA.size) Player.songPosition = 0
            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()

            val img = getImageArt(
                Player.musicListPA[Player.songPosition].path, BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.image_background
                )
            )
            val image = if (img != null) {
                BitmapFactory.decodeByteArray(img, 0, img.size)
            } else {
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.image_background
                )
            }

            val dr: Drawable = BitmapDrawable(image)
            NowPlaying.binding.songImgNP.setImageBitmap((dr as BitmapDrawable).bitmap)

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            NowPlaying.binding.songNameNP.text = Player.musicListPA[Player.songPosition].title

            Player.musicService!!.showNotification(
                playingState(),
                favouriteState(),
                musicState()
            )

            playMusic()
        } else {
            Player.isPlaying = false
            Player.musicListPA = ArrayList()
            Player.songPosition = 0
            Player.musicService!!.audioManager.abandonAudioFocus(Player.musicService)
            Player.musicService!!.stopForeground(true)
            Player.musicService!!.mediaPlayer!!.stop()
        }
    }

    private fun playMusic() {
        Player.isPlaying = true
        Player.musicService!!.mediaPlayer!!.start()
        NowPlaying.binding.playPauseNP.setIconResource(R.drawable.pause_music)
        Player.musicService!!.showNotification(
            playingState(),
            favouriteState(),
            musicState()
        )
        MainActivity.albumAdapter.update()
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, Player::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun addSong(song: Music): Boolean {
        playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.forEachIndexed { index, music ->
            if (song.id == music.id) {
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