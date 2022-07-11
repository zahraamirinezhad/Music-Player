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
import android.widget.TextView
import android.widget.Toast
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.*
import com.example.musicplayer.Adaptor.AlbumViewAdapter
import com.example.musicplayer.Adaptor.ArtistViewAdapter
import com.example.musicplayer.Adaptor.MusicAdapter
import com.example.musicplayer.Music_Stuff.*
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

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
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
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
            Favourite.favoriteSongs = ArrayList()
            val editor = getSharedPreferences("savedInfo", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                Favourite.favoriteSongs.addAll(data)
            }

            Playlist.listOfPlaylists = ListOfPlaylists()
            val editorPlaylist = getSharedPreferences("savedInfo", MODE_PRIVATE)
            val jsonStringPlaylist = editorPlaylist.getString("Playlists", null)
            val typeTokenPlaylist = object : TypeToken<ListOfPlaylists>() {}.type
            if (jsonStringPlaylist != null) {
                val data: ListOfPlaylists =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, typeTokenPlaylist)
                Playlist.listOfPlaylists = data
            }

            val recentMusicIsPlaying = editor.getString("RecentMusicIsPlaying", "false")
            if (!recentMusicIsPlaying.toBoolean()) {
                val editorRecentMusic = getSharedPreferences("savedInfo", MODE_PRIVATE)
                val jsonStringRecentMusic = editorRecentMusic.getString("RecentMusic", null)
                val typeTokenRecentMusic = object : TypeToken<Music>() {}.type
                if (jsonStringRecentMusic != null) {
                    val music: Music =
                        GsonBuilder().create().fromJson(jsonStringRecentMusic, typeTokenRecentMusic)
                    val pos = Stuff.findMusicById(music)
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
            musicAdapter.updateMusicList(MusicListMA)
            binding.refreshLayout.isRefreshing = false
        }

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
                        when (binding.musicRV.adapter) {
                            is MusicAdapter -> {
                                val menuList = arrayOf("Recently Added", "Song Title", "File Size")
                                var currentSort = sortBy
                                val build = MaterialAlertDialogBuilder(this)
                                build.setTitle("SORT ORDER").setPositiveButton("YES") { _, _ ->
                                    if (sortBy != currentSort) {
                                        sortBy = currentSort
                                        MusicListMA = getAllAudio()
                                        musicAdapter.updateMusicList(MusicListMA)
                                        val editor =
                                            getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                                        editor.putInt("SORT ORDER", currentSort)
                                        editor.apply()
                                    }

                                }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
                                    currentSort = wich
                                }
                                val customDialog = build.create()
                                customDialog.show()
                                customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(Color.GREEN)
                            }
                            is AlbumViewAdapter -> {
                                val menuList = arrayOf("Album Name", "Song Amount")
                                var currentSort = albumSortBy
                                val build = MaterialAlertDialogBuilder(this)
                                build.setTitle("SORT ORDER").setPositiveButton("YES") { _, _ ->
                                    if (albumSortBy != currentSort) {
                                        albumSortBy = currentSort
                                        when (albumSortBy) {
                                            0 -> {
                                                val newList: LinkedHashMap<String, ArrayList<Music>> =
                                                    Stuff.sortByName(
                                                        songByAlbum
                                                    )
                                                songByAlbum = LinkedHashMap()
                                                songByAlbum = newList
                                                albumAdapter.updateForSort(songByAlbum)
                                            }
                                            1 -> {
                                                val list = Stuff.sortByMusicAmount(songByAlbum)
                                                songByAlbum = LinkedHashMap()
                                                for (x in list) {
                                                    songByAlbum[x.key] = x.value
                                                }
                                                albumAdapter.updateForSort(songByAlbum)
                                            }
                                        }
                                        val editor =
                                            getSharedPreferences("SORTING_ALBUM", MODE_PRIVATE).edit()
                                        editor.putInt("SORT ORDER FOR ALBUM", albumSortBy)
                                        editor.apply()
                                    }
                                }.setSingleChoiceItems(menuList, currentSort) { _, wich ->
                                    currentSort = wich
                                }
                                val customDialog = build.create()
                                customDialog.show()
                                customDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(Color.GREEN)
                            }
                            is ArtistViewAdapter -> {
                                val menuList = arrayOf("Artist Name", "Song Amount")
                                var currentSort = artistSortBy
                                val build = MaterialAlertDialogBuilder(this)
                                build.setTitle("SORT ORDER").setPositiveButton("YES") { _, _ ->
                                    if (artistSortBy != currentSort) {
                                        artistSortBy = currentSort
                                        when (artistSortBy) {
                                            0 -> {
                                                val newList: LinkedHashMap<String, ArrayList<Music>> =
                                                    Stuff.sortByName(
                                                        songByArtist
                                                    )
                                                songByArtist = LinkedHashMap()
                                                songByArtist = newList
                                                artistAdapter.updateForSort(songByArtist)
                                            }
                                            1 -> {
                                                val list = Stuff.sortByMusicAmount(songByArtist)
                                                songByArtist = LinkedHashMap()
                                                for (x in list) {
                                                    songByArtist[x.key] = x.value
                                                }
                                                artistAdapter.updateForSort(songByArtist)
                                            }
                                        }
                                        val editor =
                                            getSharedPreferences("SORTING_ARTIST", MODE_PRIVATE).edit()
                                        editor.putInt("SORT ORDER FOR ARTIST", artistSortBy)
                                        editor.apply()
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
                    }
                }
                true
            }
            menuHelper.show()
        }

        binding.showByBtn.setOnClickListener {
            showButtonSheetDialog()
        }

    }

    private fun showButtonSheetDialog() {
        val dialog = BottomSheetDialog(this@MainActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog_show_by)
        dialog.show()
        dialog.findViewById<TextView>(R.id.showByAlbums)?.setOnClickListener {
            if (binding.musicRV.adapter !is AlbumViewAdapter) {
                binding.showByType.text = getString(R.string.album)
                binding.musicRV.layoutManager = GridLayoutManager(this, 2)
                binding.musicRV.adapter = albumAdapter
                val editor = getSharedPreferences("ShowBy", MODE_PRIVATE).edit()
                editor.putString("SHOW BY", (binding.musicRV.adapter as Any).javaClass.toString())
                editor.apply()
            }
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.showBySongs)?.setOnClickListener {
            if (binding.musicRV.adapter !is MusicAdapter) {
                binding.showByType.text = getString(R.string.song)
                binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.musicRV.adapter = musicAdapter
                val editor = getSharedPreferences("ShowBy", MODE_PRIVATE).edit()
                editor.putString("SHOW BY", (binding.musicRV.adapter as Any).javaClass.toString())
                editor.apply()
            }
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.showByArtist)?.setOnClickListener {
            if (binding.musicRV.adapter !is ArtistViewAdapter) {
                binding.showByType.text = getString(R.string.artist)
                binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.musicRV.adapter = artistAdapter
                val editor = getSharedPreferences("ShowBy", MODE_PRIVATE).edit()
                editor.putString("SHOW BY", (binding.musicRV.adapter as Any).javaClass.toString())
                editor.apply()
            }
            dialog.dismiss()
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

        val albumSortEditor = getSharedPreferences("SORTING_ALBUM", MODE_PRIVATE)
        albumSortBy = albumSortEditor.getInt("SORT ORDER FOR ALBUM", 0)

        val artistSortEditor = getSharedPreferences("SORTING_ARTIST", MODE_PRIVATE)
        artistSortBy = artistSortEditor.getInt("SORT ORDER FOR ARTIST", 0)

        MusicListMA = getAllAudio()
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)

        if (albumSortBy == 1) {
            val list = Stuff.sortByMusicAmount(songByAlbum)
            songByAlbum = LinkedHashMap()
            for (x in list) {
                songByAlbum[x.key] = x.value
            }
        } else {
            val newList: LinkedHashMap<String, ArrayList<Music>> = Stuff.sortByName(
                songByAlbum
            )
            songByAlbum = LinkedHashMap()
            songByAlbum = newList
        }
        albumAdapter = AlbumViewAdapter(this, songByAlbum)

        if (artistSortBy == 1) {
            val list = Stuff.sortByMusicAmount(songByArtist)
            songByArtist = LinkedHashMap()
            for (x in list) {
                songByArtist[x.key] = x.value
            }
        } else {
            val newList: LinkedHashMap<String, ArrayList<Music>> = Stuff.sortByName(
                songByArtist
            )
            songByArtist = LinkedHashMap()
            songByArtist = newList
        }
        artistAdapter = ArtistViewAdapter(this, songByArtist)

        val editor = getSharedPreferences("ShowBy", MODE_PRIVATE)
        val show = editor.getString("SHOW BY", "")
        if (show == getString(R.string.music_adapter_class) || show.equals("") || show == null) {
            binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.musicRV.adapter = musicAdapter
            binding.showByType.text = getString(R.string.song)
        } else if (show == getString(R.string.album_adapter_class)) {
            binding.musicRV.layoutManager = GridLayoutManager(this@MainActivity, 2)
            binding.musicRV.adapter = albumAdapter
            binding.showByType.text = getString(R.string.album)
        } else if (show == getString(R.string.artist_adapter_class)) {
            binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.musicRV.adapter = artistAdapter
            binding.showByType.text = getString(R.string.artist)
        }
        if (Player.musicService != null) {
            musicAdapter.updateMusicList(MusicListMA)
        }
    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(): ArrayList<Music> {
        allMusicsLyrics = LinkedHashMap()
        val editor = getSharedPreferences("savedInfo", MODE_PRIVATE)
        val jsonString = editor.getString("AllMusicsLyrics", null)
        val typeToken = object : TypeToken<LinkedHashMap<String, String>>() {}.type
        if (jsonString != null) {
            allMusicsLyrics = GsonBuilder().create().fromJson(jsonString, typeToken)

        }
        songByAlbum = LinkedHashMap()
        songByArtist = LinkedHashMap()
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
                    val lyricsC: String? =
                        if (allMusicsLyrics.containsKey(idC)) allMusicsLyrics[idC] else null
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC,
                        lyrics = lyricsC
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                        if (songByAlbum.containsKey(music.album)) {
                            songByAlbum[music.album]?.add(music)
                        } else {
                            songByAlbum[music.album] = ArrayList()
                            songByAlbum[music.album]?.add(music)
                        }

                        if (songByArtist.containsKey(music.artist)) {
                            songByArtist[music.artist]?.add(music)
                        } else {
                            songByArtist[music.artist] = ArrayList()
                            songByArtist[music.artist]?.add(music)
                        }
                    }
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
            Stuff.exitApplication()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        val editor = getSharedPreferences("savedInfo", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(Favourite.favoriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(Playlist.listOfPlaylists)
        editor.putString("Playlists", jsonStringPlaylist)
        if (Player.musicService != null && Player.isMusicListPaInitialized() && Player.musicListPA.size != 0) {
            val recentMusic = GsonBuilder().create().toJson(Player.musicListPA[Player.songPosition])
            editor.putString("RecentMusic", recentMusic)
            editor.putString(
                "RecentMusicCurrentPosition",
                Player.musicService!!.mediaPlayer!!.currentPosition.toString()
            )
            editor.putString("RecentMusicIsPlaying", Player.isPlaying.toString())
        }
        for (music in MusicListMA) {
            if (music.lyrics != null && music.lyrics != "") allMusicsLyrics.put(
                music.id,
                music.lyrics!!
            )
        }
        val lyricsJsonString = GsonBuilder().create().toJson(allMusicsLyrics)
        editor.putString("AllMusicsLyrics", lyricsJsonString)
        editor.apply()

        if (binding.musicRV.adapter is MusicAdapter) {
            val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
            val sortValue = sortEditor.getInt("SORT ORDER", 0)
            if (sortBy != sortValue) {
                if (Player.isMusicListPaInitialized()) {
                    val music = Player.musicListPA[Player.songPosition]
                    musicAdapter.update()
                    MusicListMA = getAllAudio()
                    Player.songPosition = Stuff.findMusicById(music)
                    musicAdapter.updateMusicList(MusicListMA)
                } else {
                    MusicListMA = getAllAudio()
                    musicAdapter.updateMusicList(MusicListMA)
                }
            }
        } else if (binding.musicRV.adapter is AlbumViewAdapter) {
            val albumSortEditor = getSharedPreferences("SORTING_ALBUM", MODE_PRIVATE)
            val sortValue = albumSortEditor.getInt("SORT ORDER FOR ALBUM", 0)
            if (albumSortBy != sortValue) {
                albumSortBy = sortValue
                when (albumSortBy) {
                    0 -> {
                        val newList: LinkedHashMap<String, ArrayList<Music>> =
                            Stuff.sortByName(
                                songByAlbum
                            )
                        songByAlbum = LinkedHashMap()
                        songByAlbum = newList
                        albumAdapter.updateForSort(songByAlbum)
                    }
                    1 -> {
                        val list = Stuff.sortByMusicAmount(songByAlbum)
                        songByAlbum = LinkedHashMap()
                        for (x in list) {
                            songByAlbum[x.key] = x.value
                        }
                        albumAdapter.updateForSort(songByAlbum)
                    }
                }
            }
        }else if (binding.musicRV.adapter is ArtistViewAdapter) {
            val albumSortEditor = getSharedPreferences("SORTING_ARTIST", MODE_PRIVATE)
            val sortValue = albumSortEditor.getInt("SORT ORDER FOR ARTIST", 0)
            if (artistSortBy != sortValue) {
                artistSortBy = sortValue
                when (artistSortBy) {
                    0 -> {
                        val newList: LinkedHashMap<String, ArrayList<Music>> =
                            Stuff.sortByName(
                                songByArtist
                            )
                        songByArtist = LinkedHashMap()
                        songByArtist = newList
                        artistAdapter.updateForSort(songByArtist)
                    }
                    1 -> {
                        val list = Stuff.sortByMusicAmount(songByArtist)
                        songByArtist = LinkedHashMap()
                        for (x in list) {
                            songByArtist[x.key] = x.value
                        }
                        artistAdapter.updateForSort(songByArtist)
                    }
                }
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
