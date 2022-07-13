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
import android.provider.MediaStore.VOLUME_EXTERNAL
import android.view.Menu
import android.view.MenuItem
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
import androidx.core.database.getStringOrNull
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
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByAlbum
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByAlbumName
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByAlbumSongAmount
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByArtist
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByArtistName
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByArtistSonAmount
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByMusic
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByMusicAmount
import com.example.musicplayer.Music_Stuff.SortMusics.Companion.sortByName
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.oxycblt.auxio.music.id3GenreName
import org.oxycblt.auxio.music.queryCursor
import org.oxycblt.auxio.music.useQuery
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
            getSavedInfo()
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

        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)

        if (albumSortBy == 1) {
            val list = sortByMusicAmount(songByAlbum)
            songByAlbum = LinkedHashMap()
            for (x in list) {
                songByAlbum[x.key] = x.value
            }
        } else {
            val newList: LinkedHashMap<String, ArrayList<Music>> = sortByName(
                songByAlbum
            )
            songByAlbum = LinkedHashMap()
            songByAlbum = newList
        }
        albumAdapter = AlbumViewAdapter(this, songByAlbum)

        if (artistSortBy == 1) {
            val list = sortByMusicAmount(songByArtist)
            songByArtist = LinkedHashMap()
            for (x in list) {
                songByArtist[x.key] = x.value
            }
        } else {
            val newList: LinkedHashMap<String, ArrayList<Music>> = sortByName(
                songByArtist
            )
            songByArtist = LinkedHashMap()
            songByArtist = newList
        }
        artistAdapter = ArtistViewAdapter(this, songByArtist)

        val viewPager = binding.musicArtistAlbum
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.add(ShowByArtist(), "ARTIST")
        adapter.add(ShowByAlbum(), "ALBUM")
        adapter.add(ShowByMusic(), "MUSIC")
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
                val editor = getSharedPreferences("ShowBy", MODE_PRIVATE).edit()
                editor.putInt(
                    "SHOW BY",
                    Position
                )
                editor.apply()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })


        val editor = getSharedPreferences("ShowBy", MODE_PRIVATE)
        val show = editor.getInt("SHOW BY", 0)
        viewPager.setCurrentItem(show)
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
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.SIZE,
        )
        val cursor = this.contentResolver.queryCursor(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null
        )

//        val cursor = this.contentResolver.query(
//            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null,
//            sortingList[sortBy], null
//        )

        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))
                            ?: "Unknown"
                    val idC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID))
                            .toString()
                    val albumC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))
                            ?: "Unknown"
                    val artistC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))
                            ?: "Unknown"
                    val dateC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED))
                            ?: "Unknown"
                    val sizeC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE))
                    val pathC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
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
                        lyrics = lyricsC,
                        genre = "Unknown",
                        date = dateC,
                        size = sizeC
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
            cursor.close()
        }

        contentResolver.useQuery(
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        ) { genreCursor ->
            val idIndex = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            val nameIndex = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

            while (genreCursor.moveToNext()) {
                val id = genreCursor.getLong(idIndex)
                val name = (genreCursor.getStringOrNull(nameIndex) ?: continue).id3GenreName
                contentResolver.useQuery(
                    MediaStore.Audio.Genres.Members.getContentUri(VOLUME_EXTERNAL, id),
                    arrayOf(MediaStore.Audio.Genres.Members._ID)
                ) { cursor ->
                    val songIdIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members._ID)

                    while (cursor.moveToNext()) {
                        val songId = cursor.getLong(songIdIndex)
                        tempList.find { it.id.toLong() == songId }
                            ?.let { song -> song.genre = name }
                    }
                }
            }
        }

        for (music in tempList) {
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
            val recentMusic =
                GsonBuilder().create().toJson(Player.musicListPA[Player.songPosition])
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

        when (binding.musicArtistAlbum.currentItem) {
            2 -> {
                val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
                val sortValue = sortEditor.getInt("SORT ORDER", 0)
                if (sortBy != sortValue) {
                    when (sortBy) {
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
                    musicAdapter.updateMusicList(MusicListMA)
                }
            }
            1 -> {
                val albumSortEditor = getSharedPreferences("SORTING_ALBUM", MODE_PRIVATE)
                val sortValue = albumSortEditor.getInt("SORT ORDER FOR ALBUM", 0)
                if (albumSortBy != sortValue) {
                    albumSortBy = sortValue
                    when (albumSortBy) {
                        0 -> {
                            sortByAlbumName()
                        }
                        1 -> {
                            sortByAlbumSongAmount()
                        }
                    }
                }
            }
            0 -> {
                val artistSortEditor = getSharedPreferences("SORTING_ARTIST", MODE_PRIVATE)
                val sortValue = artistSortEditor.getInt("SORT ORDER FOR ARTIST", 0)
                if (artistSortBy != sortValue) {
                    artistSortBy = sortValue
                    when (artistSortBy) {
                        0 -> {
                            sortByArtistName()
                        }
                        1 -> {
                            sortByArtistSonAmount()
                        }
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

    private fun getSavedInfo() {
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