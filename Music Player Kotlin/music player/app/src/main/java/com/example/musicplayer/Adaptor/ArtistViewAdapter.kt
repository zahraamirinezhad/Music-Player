package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.*
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.Music_Stuff.Constants.Companion.ADD
import com.example.musicplayer.Music_Stuff.Constants.Companion.DAY_FORMATTER
import com.example.musicplayer.Music_Stuff.Constants.Companion.INDEX
import com.example.musicplayer.Music_Stuff.Constants.Companion.ITS_FROM_PLAY_BTN
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSICS_ADDED_SUCCESSFULLY
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYLIST_EXIST
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAYLIST_NAME
import com.example.musicplayer.Music_Stuff.Constants.Companion.PLAY_SONG_FIRST
import com.example.musicplayer.Music_Stuff.Constants.Companion.SELLECT_PLAYLIST
import com.example.musicplayer.R
import com.example.musicplayer.databinding.AddPlaylistDialogBinding
import com.example.musicplayer.databinding.ArtistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class ArtistViewAdapter(
    private val context: Context, private var listOfArtists: LinkedHashMap<String, ArrayList<Music>>
) :
    RecyclerView.Adapter<ArtistViewAdapter.MyHolder>() {
    companion object {
        var currentArtist = 0

        @SuppressLint("StaticFieldLeak")
        lateinit var menu: SelectPlayList
    }

    class MyHolder(binding: ArtistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.artistImg
        val name = binding.artistName
        val root = binding.root
        val more = binding.moreOptionsArtistView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ArtistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged", "RestrictedApi")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = listOfArtists.keys.elementAt(position)

        if (listOfArtists[listOfArtists.keys.elementAt(position)]?.size != 0) {
            if (listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                    0
                )!!.image == null
            ) {
                try {
                    val img = Stuff.getImageArt(
                        listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                            0
                        )!!.path
                    )
                    val image = if (img != null) {
                        BitmapFactory.decodeByteArray(img, 0, img.size)
                    } else {
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.image_background
                        )
                    }
                    listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                        0
                    )!!.image = image
                    holder.image.setImageBitmap(
                        listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                            0
                        )!!.image
                    )
                } catch (e: Exception) {
                    listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                        0
                    )!!.image = BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.image_background
                    )
                    holder.image.setImageBitmap(
                        listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                            0
                        )!!.image
                    )
                }
            } else {
                holder.image.setImageBitmap(
                    listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
                        0
                    )!!.image
                )
            }
        } else {
            val image = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.image_background
            )

            holder.image.setImageBitmap(image)
        }

        holder.more.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.more)
            popupMenu.menuInflater.inflate(R.menu.album_view_menu, popupMenu.menu)
            val menuHelper =
                MenuPopupHelper(context as Activity, popupMenu.menu as MenuBuilder, holder.more)
            menuHelper.setForceShowIcon(true)
            popupMenu.setOnMenuItemClickListener { p0 ->
                when (p0.itemId) {
                    R.id.play_album_view -> {
                        val intent2 = Intent(context, ShowByArtistDetails::class.java)
                        intent2.putExtra(INDEX, position)
                        intent2.putExtra(ITS_FROM_PLAY_BTN, true)
                        ContextCompat.startActivity(context, intent2, null)
                    }
                    R.id.add_to_playlist_album_view -> {
                        currentArtist = position
                        val ft: FragmentManager =
                            (context as FragmentActivity).supportFragmentManager
                        menu = SelectPlayList()
                        menu.show(ft, SELLECT_PLAYLIST)
                    }
                    R.id.add_to_play_next_album_view -> {
                        try {
                            if (PlayNext.playNextList.isEmpty()) {
                                PlayNext.playNextList.add(Player.musicListPA[Player.songPosition])
                                Player.songPosition = 0
                            }

                            for (music in MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                                currentArtist
                            )]!!) {
                                if (!Stuff.doesListContainsThisMusic(
                                        PlayNext.playNextList,
                                        music.id
                                    )
                                )
                                    PlayNext.playNextList.add(music)
                            }
                            Player.musicListPA = java.util.ArrayList()
                            Player.musicListPA.addAll(PlayNext.playNextList)
                            Toast.makeText(context, MUSICS_ADDED_SUCCESSFULLY, Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Snackbar.make(context, holder.root, PLAY_SONG_FIRST, 3000).show()
                        }
                    }
                    R.id.add_to_favourites -> {
                        for (music in MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                            currentArtist
                        )]!!) {
                            if (!Stuff.doesListContainsThisMusic(Favourite.favoriteSongs, music.id))
                                Favourite.favoriteSongs.add(music)
                        }
                        Toast.makeText(context, MUSICS_ADDED_SUCCESSFULLY, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                true
            }
            menuHelper.show()
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, ShowByArtistDetails::class.java)
            intent.putExtra(INDEX, position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return listOfArtists.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateForSort(newList: LinkedHashMap<String, ArrayList<Music>>) {
        listOfArtists = LinkedHashMap()
        for (x in newList.keys)
            listOfArtists[x] = newList[x]!!
        notifyDataSetChanged()
    }

    class SelectPlayList : DialogFragment() {
        var root: View? = null

        @SuppressLint("CutPasteId")
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val rootView: View = inflater.inflate(R.layout.select_playlist, container, false)
            root = rootView
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            rootView.findViewById<RecyclerView>(R.id.selectPlaylist).setHasFixedSize(true)
            rootView.findViewById<RecyclerView>(R.id.selectPlaylist).setItemViewCacheSize(5)
            val adapter =
                SelectPlaylistAdapter(context as Activity, Playlist.listOfPlaylists.ref)
            rootView.findViewById<RecyclerView>(R.id.selectPlaylist).layoutManager =
                LinearLayoutManager(context)
            rootView.findViewById<RecyclerView>(R.id.selectPlaylist).adapter = adapter
            rootView.findViewById<LinearLayout>(R.id.addPlaylistSLP).setOnClickListener {
                customAlertDialog()
            }

            return rootView
        }

        private fun customAlertDialog() {
            val dialog = LayoutInflater.from(context)
                .inflate(
                    R.layout.add_playlist_dialog,
                    root!!.findViewById<LinearLayout>(R.id.addPlaylistSLP),
                    false
                )
            val binder = AddPlaylistDialogBinding.bind(dialog)
            val builder = MaterialAlertDialogBuilder(context as Activity)
            builder.setView(dialog).setTitle(PLAYLIST_NAME)
                .setPositiveButton(ADD) { dialog, _ ->
                    val name = binder.playListNamePL.text
                    val username = binder.userNamePL.text
                    if (name != null && username != null && name.isNotEmpty() && username.isNotEmpty()) {
                        addPlaylist(name.toString(), username.toString())
                        Toast.makeText(context, MUSICS_ADDED_SUCCESSFULLY, Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                        menu.dismiss()
                    }
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }

        private fun addPlaylist(name: String, username: String) {
            var playlistExist = false
            for (i in Playlist.listOfPlaylists.ref) {
                if (i.name == name) {
                    playlistExist = true
                    break
                }
            }

            if (playlistExist) Toast.makeText(context, PLAYLIST_EXIST, Toast.LENGTH_SHORT)
                .show()
            else {
                val tempPlaylist = MyPlaylist()
                tempPlaylist.musics = java.util.ArrayList()
                tempPlaylist.musics.addAll(
                    MainActivity.songByArtist[MainActivity.songByArtist.keys.elementAt(
                        currentArtist
                    )]!!
                )
                tempPlaylist.name = name
                tempPlaylist.createdBy = username
                val calender = Calendar.getInstance().time
                val sdf = SimpleDateFormat(DAY_FORMATTER, Locale.US)
                tempPlaylist.createdOn = sdf.format(calender)
                Playlist.listOfPlaylists.ref.add(tempPlaylist)
            }
        }
    }
}