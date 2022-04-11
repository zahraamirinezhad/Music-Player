package com.example.musicplayer.Activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.Adaptor.PlayNextAdapter
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityPlayNextBinding

class PlayNext : AppCompatActivity() {
    companion object {
        var playNextList: ArrayList<Music> = ArrayList()
    }

    private lateinit var binding: ActivityPlayNextBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackTheme)
        binding = ActivityPlayNextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playNextRV.setHasFixedSize(true)
        binding.playNextRV.setItemViewCacheSize(13)
        binding.playNextRV.layoutManager = GridLayoutManager(this, 2)
        binding.playNextRV.adapter = PlayNextAdapter(this, playNextList)

        if (playNextList.isNotEmpty())
            binding.instructionPN.visibility = View.GONE

        binding.backBtnPN.setOnClickListener {
            finish()
        }
    }
}