package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.*
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.NowPlaying
import com.example.musicplayer.Music_Stuff.Stuff
import com.example.musicplayer.R
import com.example.musicplayer.databinding.MusicViewBinding

open class Adapter(
    val context: Context,
    var musicList: ArrayList<Music>,
    var selectAll: Boolean = false
) :
    RecyclerView.Adapter<Adapter.MyHolder>() {

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
    }

    fun getSongImage(pos: Int): Bitmap {
        val img = Stuff.getImageArt(
            musicList[pos].path, BitmapFactory.decodeResource(
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
        return myImage
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
        if (Player.musicListPA.size != 0) {
            if (Player.songPosition == Player.musicListPA.size) Player.songPosition = 0
            Player.musicService!!.mediaPlayer!!.reset()
            Player.musicService!!.mediaPlayer!!.setDataSource(Player.musicListPA[Player.songPosition].path)
            Player.musicService!!.mediaPlayer!!.prepare()

            val img = Stuff.getImageArt(
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

            NowPlaying.binding.songImgNP.setImageBitmap(image)

            Player.binding.seekMusic.progress = 0
            Player.binding.seekMusic.max = Player.musicService!!.mediaPlayer!!.duration
            Player.binding.seekMusicStart.text =
                Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.currentPosition.toLong())
            Player.binding.seekMusicEnd.text =
                Stuff.formatDuration(Player.musicService!!.mediaPlayer!!.duration.toLong())

            NowPlaying.binding.songNameNP.text = Player.musicListPA[Player.songPosition].title

            Player.musicService!!.showNotification(
                Stuff.playingState(),
                Stuff.favouriteState(),
                Stuff.musicState()
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
            Stuff.playingState(),
            Stuff.favouriteState(),
            Stuff.musicState()
        )
    }

    fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, Player::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    fun addSong(song: Music): Boolean {
        Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.forEachIndexed { index, music ->
            if (song.id == music.id) {
                Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.removeAt(index)
                return false
            }
        }
        Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        musicList = ArrayList()
        musicList = Playlist.listOfPlaylists.ref[PlaylistDetails.currentPlaylist].musics
        notifyDataSetChanged()
    }
}