package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
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
import com.example.musicplayer.R
import com.example.musicplayer.databinding.AddPlaylistDialogBinding
import com.example.musicplayer.databinding.AlbumViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class AlbumViewAdapter(
    private val context: Context, private var listOfAlbums: LinkedHashMap<String, ArrayList<Music>>
) :
    RecyclerView.Adapter<AlbumViewAdapter.MyHolder>() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var menu: SelectPlayList
        var currentAlbum = 0
    }

    class MyHolder(binding: AlbumViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.albumImage
        val name = binding.albumName
        val root = binding.root
        val more = binding.moreOptionsAlbumView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(AlbumViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged", "RestrictedApi")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = listOfAlbums.keys.elementAt(position)

        if (listOfAlbums[listOfAlbums.keys.elementAt(position)]?.size != 0) {
            try {
                val img = getImageArt(
                    listOfAlbums[listOfAlbums.keys.elementAt(position)]?.get(
                        0
                    )!!.path,
                    BitmapFactory.decodeResource(
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

                holder.image.setImageBitmap(image)
            } catch (e: Exception) {
                val image = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.image_background
                )

                holder.image.setImageBitmap(image)
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
                        val intent2 = Intent(context, ShowByAlbumDetails::class.java)
                        intent2.putExtra("index", position)
                        intent2.putExtra("ItsFromPlayBTN", true)
                        ContextCompat.startActivity(context, intent2, null)
                    }
                    R.id.add_to_playlist_album_view -> {
                        currentAlbum = position
                        val ft: FragmentManager =
                            (context as FragmentActivity).supportFragmentManager
                        menu = SelectPlayList()
                        menu.show(ft, "SELECT PLAYLIST")
                    }
                    R.id.add_to_play_next_album_view -> {
                        try {
                            if (PlayNext.playNextList.isEmpty()) {
                                PlayNext.playNextList.add(Player.musicListPA[Player.songPosition])
                                Player.songPosition = 0
                            }

                            PlayNext.playNextList.addAll(
                                MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                                    currentAlbum
                                )]!!
                            )
                            Player.musicListPA = ArrayList()
                            Player.musicListPA.addAll(PlayNext.playNextList)
                        } catch (e: Exception) {
                            Snackbar.make(context, holder.root, "Play A Song First!!", 3000).show()
                        }
                    }
                }
                true
            }
            menuHelper.show()
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, ShowByAlbumDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return listOfAlbums.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll() {
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
                SelectPlaylistAdapter(context as Activity, playlist.listOfPlaylists.ref)
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
            builder.setView(dialog).setTitle("PLAYLIST NAME")
                .setPositiveButton("ADD") { dialog, _ ->
                    val name = binder.playListNamePL.text
                    val username = binder.userNamePL.text
                    if (name != null && username != null && name.isNotEmpty() && username.isNotEmpty()) {
                        addPlaylist(name.toString(), username.toString())
                    }
                    Toast.makeText(context, "Musics Added Successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    menu.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        }

        private fun addPlaylist(name: String, username: String) {
            var playlistExist = false
            for (i in playlist.listOfPlaylists.ref) {
                if (i.name == name) {
                    playlistExist = true
                    break
                }
            }

            if (playlistExist) Toast.makeText(context, "Playlist Exist !!", Toast.LENGTH_SHORT)
                .show()
            else {
                val tempPlaylist = myPlaylist()
                tempPlaylist.name = name
                tempPlaylist.createdBy = username
                tempPlaylist.musics = ArrayList()
                val calender = Calendar.getInstance().time
                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.US)
                tempPlaylist.createdOn = sdf.format(calender)
                tempPlaylist.musics.addAll(
                    MainActivity.songByAlbum[MainActivity.songByAlbum.keys.elementAt(
                        currentAlbum
                    )]!!
                )
                playlist.listOfPlaylists.ref.add(tempPlaylist)
            }
        }
    }
}