package com.example.musicplayer.Music_Stuff

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.Music_Stuff.Constants.Companion.LYRICS_ADDED_SUCCESSFULLY
import com.example.musicplayer.Music_Stuff.Constants.Companion.NO_LYRICS
import com.example.musicplayer.R

class playin_song_lyrics : Fragment() {
    @SuppressLint("CutPasteId", "RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playin_song_lyrics, container, false)

        if (Player.musicListPA[Player.songPosition].lyrics != null && Player.musicListPA[Player.songPosition].lyrics != "") {
            rootView.findViewById<EditText>(R.id.lyrics)
                .setText(Player.musicListPA[Player.songPosition].lyrics)
        }

        rootView.findViewById<ImageButton>(R.id.lyrics_options).setOnClickListener {
            val popupMenu = PopupMenu(
                context as Activity,
                rootView.findViewById<ImageButton>(R.id.lyrics_options)
            )
            popupMenu.menuInflater.inflate(R.menu.lyrics_menu, popupMenu.menu)
            val menuHelper = MenuPopupHelper(
                context as Activity,
                popupMenu.menu as MenuBuilder,
                rootView.findViewById<ImageButton>(R.id.lyrics_options)
            )
            menuHelper.setForceShowIcon(true)
            popupMenu.setOnMenuItemClickListener { p0 ->
                when (p0.itemId) {
                    R.id.seek_for_lyrics -> {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q=" + Player.musicListPA[Player.songPosition].title + "+lyrics")
                        )
                        startActivity(intent)
                    }

                    R.id.save_lyrics -> {
                        if (rootView.findViewById<EditText>(R.id.lyrics).text.toString() != "") {
                            Player.musicListPA[Player.songPosition].lyrics =
                                rootView.findViewById<EditText>(R.id.lyrics).text.toString()
                            Toast.makeText(
                                context,
                                LYRICS_ADDED_SUCCESSFULLY,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                NO_LYRICS,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                true
            }
            menuHelper.show()
        }
        return rootView
    }
}