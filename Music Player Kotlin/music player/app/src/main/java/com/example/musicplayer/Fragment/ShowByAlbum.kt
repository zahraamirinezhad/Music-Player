package com.example.musicplayer.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.R

class ShowByAlbum : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_show_by_album, container, false)

        val musicRv = root.findViewById<RecyclerView>(R.id.showByAlbumRV)

        musicRv.setHasFixedSize(true)
        musicRv.setItemViewCacheSize(13)
        musicRv.layoutManager = GridLayoutManager(context, 2)
        musicRv.adapter = MainActivity.albumAdapter

        return root
    }
}