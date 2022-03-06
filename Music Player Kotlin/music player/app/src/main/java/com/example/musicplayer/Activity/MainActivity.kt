package com.example.musicplayer.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.*
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.ListOfPlaylists
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.exitApplication
import com.example.musicplayer.Music_Stuff.findMusicById
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    //Creating Binding Object
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var musicAdapter: MusicAdapter
        lateinit var binding: ActivityMainBinding
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var MusicListSearch: ArrayList<Music>
        var search: Boolean = false
        var sortBy = 0;
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
    }

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
            favourite.favoriteSongs = ArrayList()
            val editor = getSharedPreferences("savedInfo", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                favourite.favoriteSongs.addAll(data)
            }

            playlist.listOfPlaylists = ListOfPlaylists()
            val editorPlaylist = getSharedPreferences("savedInfo", MODE_PRIVATE)
            val jsonStringPlaylist = editorPlaylist.getString("Playlists", null)
            val typeTokenPlaylist = object : TypeToken<ListOfPlaylists>() {}.type
            if (jsonStringPlaylist != null) {
                val data: ListOfPlaylists =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, typeTokenPlaylist)
                playlist.listOfPlaylists = data
            }

            val recentMusicIsPlaying = editor.getString("RecentMusicIsPlaying", "false")
            if (!recentMusicIsPlaying.toBoolean()) {
                val editorRecentMusic = getSharedPreferences("savedInfo", MODE_PRIVATE)
                val jsonStringRecentMusic = editorRecentMusic.getString("RecentMusic", null)
                val typeTokenRecentMusic = object : TypeToken<Music>() {}.type
                if (jsonStringRecentMusic != null) {
                    val music: Music =
                        GsonBuilder().create().fromJson(jsonStringRecentMusic, typeTokenRecentMusic)
                    val pos = findMusicById(music)
                    if (pos != -1) {
                        val intent = Intent(this, Player::class.java)
                        intent.putExtra("index", pos)
                        intent.putExtra("class", "RecentMusic")
                        val currentPosition = editor.getString("RecentMusicCurrentPosition", "")
                        intent.putExtra("recentMusicCurrentPosition", currentPosition?.toInt())
                        intent.putExtra("RecentMusicIsPlaying", recentMusicIsPlaying?.toBoolean())
                        ContextCompat.startActivity(this, intent, null)
                    }
                }
            }
        }

        binding.favoritesBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, favourite::class.java)
            startActivity(intent)
        }
        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, playlist::class.java)
            startActivity(intent)

        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.feedback -> startActivity(Intent(this@MainActivity, Feedback::class.java))
                R.id.settings -> startActivity(Intent(this@MainActivity, Settings::class.java))
                R.id.about -> startActivity(Intent(this@MainActivity, About::class.java))
                R.id.exit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("EXIT")
                        .setMessage("Do You Want to Close the App ?")
                        .setPositiveButton("YES") { _, _ ->
//                            exitApplication()
                            finish()
                        }
                        .setNegativeButton("NO") { dialog, _ ->
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

        binding.refreshLayout.setOnRefreshListener {
            MusicListMA = getAllAudio()
            MusicListMA[Player.songPosition].isPlayingOrNot = true
            musicAdapter.updateMusicList(MusicListMA)
            binding.refreshLayout.isRefreshing = false
        }

        binding.moreBtn.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.moreBtn)
            popupMenu.setForceShowIcon(true)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { p0 ->
                when (p0.itemId) {
                    R.id.shuffle_menu -> {
                        if (MusicListMA.size != 0) {
                            val intent = Intent(this@MainActivity, Player::class.java)
                            intent.putExtra("index", 0)
                            intent.putExtra("class", "MainActivity")
                            startActivity(intent)
                        }
                    }
                    R.id.play_next_menu -> {
                        val intent = Intent(this@MainActivity, PlayNext::class.java)
                        startActivity(intent)
                    }

                    R.id.sort_by_menu -> {
                        val menuList = arrayOf("Recently Added", "Song Title", "File Size")
                        var currentSort = sortBy
                        val build = MaterialAlertDialogBuilder(this)
                        build.setTitle("SORT ORDER").setPositiveButton("YES") { _, _ ->
                            if (sortBy != currentSort) {
                                sortBy = currentSort
                                if (Player.isMusicListPaInitialized()) {
                                    val music = Player.musicListPA[Player.songPosition]
                                    MusicListMA[Player.songPosition].isPlayingOrNot = false
                                    musicAdapter.update()
                                    MusicListMA = getAllAudio()
                                    Player.songPosition = findMusicById(music)
                                    MusicListMA[findMusicById(music)].isPlayingOrNot = true
                                    musicAdapter.updateMusicList(MusicListMA)
                                } else {
                                    MusicListMA = getAllAudio()
                                    musicAdapter.updateMusicList(MusicListMA)
                                }
                                val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                                editor.putInt("SORT ORDER", currentSort)
                                editor.apply()

                                if (!musicAdapter.selectionActivity && !musicAdapter.playlistDetails && (!favourite.isAdapterInitialized() || !favourite.adapter.isFavourite) && PlayNext.playNextList.size == 0) {
                                    Player.musicListPA = MusicListMA
                                }
                            }

                        }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
                            currentSort = wich
                        }
                        val customDialog = build.create()
                        customDialog.show()
                        customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(Color.GREEN)
                    }
                }
                true
            }
            popupMenu.show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        search = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortBy = sortEditor.getInt("SORT ORDER", 0)
        MusicListMA = getAllAudio()
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        if (Player.musicService != null) {
            MusicListMA[Player.songPosition].isPlayingOrNot = true
            musicAdapter.updateMusicList(MusicListMA)
        }
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = musicAdapter
    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            sortingList[sortBy], null
        )
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                            ?: "Unknown"
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                        ?: "Unknown"
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                            ?: "Unknown"
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                            ?: "Unknown"
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
            cursor.close()
        }
        return tempList
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
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
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
            exitApplication()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        val editor = getSharedPreferences("savedInfo", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(favourite.favoriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(playlist.listOfPlaylists)
        editor.putString("Playlists", jsonStringPlaylist)
        if (Player.musicService != null) {
            val recentMusic = GsonBuilder().create().toJson(Player.musicListPA[Player.songPosition])
            editor.putString("RecentMusic", recentMusic)
            editor.putString(
                "RecentMusicCurrentPosition",
                Player.musicService!!.mediaPlayer!!.currentPosition.toString()
            )
            editor.putString("RecentMusicIsPlaying", Player.isPlaying.toString())
        }
        editor.apply()

        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("SORT ORDER", 0)
        if (sortBy != sortValue) {
            if (Player.isMusicListPaInitialized()) {
                val music = Player.musicListPA[Player.songPosition]
                MusicListMA[Player.songPosition].isPlayingOrNot = false
                musicAdapter.update()
                MusicListMA = getAllAudio()
                Player.songPosition = findMusicById(music)
                MusicListMA[findMusicById(music)].isPlayingOrNot = true
                musicAdapter.updateMusicList(MusicListMA)
            } else {
                MusicListMA = getAllAudio()
                musicAdapter.updateMusicList(MusicListMA)
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
}
