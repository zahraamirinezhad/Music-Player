package com.example.musicplayer.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityAboutBinding

class About : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.blackThemeNav)
        //Initializing Binding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "ABOUT"
        binding.aboutText.text = aboutText()
    }

    private fun aboutText(): String {
        val about = "Developed By Zahra Amirinezhad \n\n" +
                "My Contacts : \n" +
                "https://www.linkedin.com/in/zahra-amirinezhad-896240231/ \n\n" +
                "Some of My Projects : \n" +
                "https://github.com/zahraamirinezhad/"
        return about
    }
}