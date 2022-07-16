package com.example.musicplayer.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.musicplayer.*
import com.example.musicplayer.Adaptor.AlbumViewAdapter
import com.example.musicplayer.Adaptor.ArtistViewAdapter
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Fragment.ShowByAlbum
import com.example.musicplayer.Fragment.ShowByArtist
import com.example.musicplayer.Fragment.ShowByMusic
import com.example.musicplayer.Fragment.ViewPagerAdapter
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.Music_Stuff.Constants.Companion.ALBUM
import com.example.musicplayer.Music_Stuff.Constants.Companion.ARTIST
import com.example.musicplayer.Music_Stuff.Constants.Companion.CLASS
import com.example.musicplayer.Music_Stuff.Constants.Companion.INDEX
import com.example.musicplayer.Music_Stuff.Constants.Companion.MAIN_ACTIVITY
import com.example.musicplayer.Music_Stuff.Constants.Companion.MUSIC
import com.example.musicplayer.Music_Stuff.Constants.Companion.NO
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC_CURRENT_POSITION
import com.example.musicplayer.Music_Stuff.Constants.Companion.RECENT_MUSIC_IS_PLAYING
import com.example.musicplayer.Music_Stuff.Constants.Companion.SHOW_BY
import com.example.musicplayer.Music_Stuff.Constants.Companion.SHOW_BY_PASSWORD
import com.example.musicplayer.Music_Stuff.Constants.Companion.UNKNOWN
import com.example.musicplayer.Music_Stuff.Constants.Companion.WANNA_GO
import com.example.musicplayer.Music_Stuff.Constants.Companion.YES
import com.example.musicplayer.Music_Stuff.Data.Companion.saveAllInfo
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortAllAtBeginning
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByAlbum
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByArtist
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByMusic
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortCommandAlbumSettings
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortCommandSongSettings
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var musicAdapter: MusicAdapter

        @SuppressLint("StaticFieldLeak")
        lateinit var albumAdapter: AlbumViewAdapter

        @SuppressLint("StaticFieldLeak")
        lateinit var artistAdapter: ArtistViewAdapter
        lateinit var binding: ActivityMainBinding
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var MusicListSearch: ArrayList<Music>
        var search: Boolean = false
        var sortBy = 0
        var albumSortBy = 0
        var artistSortBy = 0
        lateinit var songByAlbum: LinkedHashMap<String, ArrayList<Music>>
        lateinit var songByArtist: LinkedHashMap<String, ArrayList<Music>>
        lateinit var allMusicsLyrics: LinkedHashMap<String, String>
    }

    @SuppressLint("RestrictedApi", "DiscouragedPrivateApi")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.blackThemeNav)
        //Initializing Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for nav drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //checking for permission & if permission is granted then initializeLayout
        if (requestRuntimePermission()) {
            initializeLayout()

            try {
                Data.getSavedInfo(this)
                if (Player.musicService == null) {
                    if (Data.playingPlayList.size != 0 && Data.nowPlayingMusicID != UNKNOWN && Data.nowPlayingMusicPositionInSeekBar != -1 && Data.isCurrentMusicPlaying != null) {
                        val intent = Intent(this, Player::class.java)
                        var pos = 0
                        for (i in 0 until Data.playingPlayList.size) {
                            if (Data.playingPlayList[i].id == Data.nowPlayingMusicID) pos = i
                        }
                        intent.putExtra(INDEX, pos)
                        intent.putExtra(CLASS, RECENT_MUSIC)
                        intent.putExtra(
                            RECENT_MUSIC_CURRENT_POSITION,
                            Data.nowPlayingMusicPositionInSeekBar
                        )
                        intent.putExtra(RECENT_MUSIC_IS_PLAYING, Data.isCurrentMusicPlaying)
                        ContextCompat.startActivity(this, intent, null)
                    }
                }
            } catch (e: Exception) {
            }
        }

        binding.favoritesBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, Favourite::class.java)
            startActivity(intent)
        }
        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, Playlist::class.java)
            startActivity(intent)
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.feedback -> startActivity(Intent(this@MainActivity, Feedback::class.java))
                R.id.settings -> startActivity(Intent(this@MainActivity, Settings::class.java))
                R.id.about -> startActivity(Intent(this@MainActivity, About::class.java))
                R.id.exit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle(this.getString(R.string.exit))
                        .setMessage(WANNA_GO)
                        .setPositiveButton(YES) { _, _ ->
//                            exitApplication()
                            finish()
                        }
                        .setNegativeButton(NO) { dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }

            }
            true
        }
//               TODO
//        binding.refreshLayout.setOnRefreshListener {
//            MusicListMA = getAllAudio(this)
//            musicAdapter.updateMusicList(MusicListMA)
//            binding.refreshLayout.isRefreshing = false
//        }

        binding.moreBtn.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.moreBtn)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)
            val menuHelper = MenuPopupHelper(this, popupMenu.menu as MenuBuilder, binding.moreBtn)
            menuHelper.setForceShowIcon(true)
            popupMenu.setOnMenuItemClickListener { p0 ->
                when (p0.itemId) {
                    R.id.shuffle_menu -> {
                        if (MusicListMA.size != 0) {
                            val intent = Intent(this@MainActivity, Player::class.java)
                            intent.putExtra(INDEX, 0)
                            intent.putExtra(CLASS, MAIN_ACTIVITY)
                            startActivity(intent)
                        }
                    }
                    R.id.play_next_menu -> {
                        val intent = Intent(this@MainActivity, PlayNext::class.java)
                        startActivity(intent)
                    }

                    R.id.sort_by_menu -> {
                        sortMusics()
                    }
                }
                true
            }
            menuHelper.show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    private fun initializeLayout() {
        search = false

        sortAllAtBeginning(this)

        val viewPager = binding.musicArtistAlbum
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.add(ShowByArtist(), ARTIST)
        adapter.add(ShowByAlbum(), ALBUM)
        adapter.add(ShowByMusic(), MUSIC)
        viewPager.adapter = adapter
        val tabLayout = binding.musicArtistAlbumTabLayout
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(Position: Int) {
                val editor = getSharedPreferences(SHOW_BY, MODE_PRIVATE).edit()
                editor.putInt(
                    SHOW_BY_PASSWORD,
                    Position
                )
                editor.apply()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })


        val editor = getSharedPreferences(SHOW_BY, MODE_PRIVATE)
        val show = editor.getInt(SHOW_BY_PASSWORD, 0)
        viewPager.currentItem = show
        if (Player.musicService != null) {
            musicAdapter.updateMusicList(MusicListMA)
        }
    }

    //For requesting permission
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                13
            )
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLayout()
            } else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!Player.isPlaying && Player.musicService != null) {
            Stuff.exitApplication()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveAllInfo(this)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        when (binding.musicArtistAlbum.currentItem) {
            2 -> {
                sortCommandSongSettings(this)
            }
            1 -> {
                sortCommandAlbumSettings(this)
            }
            0 -> {
                SortMusics.sortCommandArtistSettings(this)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean = true

            override fun onQueryTextChange(p0: String?): Boolean {
                MusicListSearch = ArrayList()
                if (p0 != null) {
                    val input = p0.lowercase()
                    for (song in MusicListMA) {
                        if (song.title.lowercase().contains(input)) {
                            MusicListSearch.add(song)
                        }
                    }
                    search = true
                    musicAdapter.updateMusicList(MusicListSearch)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun sortMusics() {
        when (binding.musicArtistAlbum.currentItem) {
            2 -> {
                sortByMusic(this)
            }
            1 -> {
                sortByAlbum(this)
            }
            0 -> {
                sortByArtist(this)
            }
        }
    }
}