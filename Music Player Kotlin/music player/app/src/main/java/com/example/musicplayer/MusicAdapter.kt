package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.DetailsViewBinding
import com.example.musicplayer.databinding.MoreFeatureBinding
import com.example.musicplayer.databinding.MusicViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

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

        val img = getImageArt(
            musicList[position].path, BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
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
        holder.image.setImageBitmap((dr as BitmapDrawable).bitmap)

        if (!selectionActivity)
            holder.root.setOnLongClickListener {
                val customDialog =
                    LayoutInflater.from(context).inflate(R.layout.more_feature, holder.root, false)
                val bindingMF = MoreFeatureBinding.bind(customDialog)
                val dialog = MaterialAlertDialogBuilder(context).setView(customDialog)
                    .create()
                dialog.show()
                dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

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
                    val dDialog = MaterialAlertDialogBuilder(context)
                        .setBackground(ColorDrawable(0x99000000.toInt()))
                        .setView(detailsDialog)
                        .setPositiveButton("OK") { self, _ -> self.dismiss() }
                        .setCancelable(false)
                        .create()
                    dDialog.show()
                    dDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                    dDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                    val str = SpannableStringBuilder().bold { append("DETAILS\n\nName: ") }
                        .append(musicList[position].title)
                        .bold { append("\n\nDuration: ") }
                        .append(DateUtils.formatElapsedTime(musicList[position].duration / 1000))
                        .bold { append("\n\nLocation: ") }.append(musicList[position].path)
                    binder.detailsTV.text = str
                }

                bindingMF.deleteBtn.setOnClickListener {
                    dialog.dismiss()
                    if (playlistDetails) {
                        playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.removeAt(
                            position
                        )
                        Player.songPosition--
                        Player.musicListPA =
                            playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics
                        refreshPlaylist()
                    } else {
                        val file = File(musicList[position].path)
                        if (file.exists()) {
                            if (favourite.favoriteSongs.contains(musicList[position])) {
                                favourite.favoriteSongs.remove(musicList[position])
                            }
                            if (playlist.listOfPlaylists.ref.size != 0) {
                                for (x in playlist.listOfPlaylists.ref) {
                                    if (x.musics.contains(musicList[position])) {
                                        x.musics.remove(musicList[position])
                                    }
                                }
                            }
                            MainActivity.MusicListMA.remove(musicList[position])
                            updateMusicList(MainActivity.MusicListMA)
                            Player.songPosition--
                            Player.musicListPA = MainActivity.MusicListMA
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