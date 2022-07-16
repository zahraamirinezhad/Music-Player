package com.example.musicplayer.Music_Stuff

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.Activity.MainActivity
import com.example.musicplayer.Adaptor.AlbumViewAdapter
import com.example.musicplayer.Adaptor.ArtistViewAdapter
import com.example.musicplayer.Adaptor.MusicAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class SortMusics {
    companion object {

        fun sortByArtistName() {
            val newList: LinkedHashMap<String, ArrayList<Music>> =
                sortByName(
                    MainActivity.songByArtist
                )
            MainActivity.songByArtist = LinkedHashMap()
            MainActivity.songByArtist = newList
            MainActivity.artistAdapter.updateForSort(MainActivity.songByArtist)
        }

        fun sortByArtistSonAmount() {
            val list = sortByMusicAmount(MainActivity.songByArtist)
            MainActivity.songByArtist = LinkedHashMap()
            for (x in list) {
                MainActivity.songByArtist[x.key] = x.value
            }
            MainActivity.artistAdapter.updateForSort(MainActivity.songByArtist)
        }

        fun sortByArtist(context: Context) {
            val menuList = arrayOf(Constants.ARTIST_NAME, Constants.SONG_AMOUNT)
            var currentSort = MainActivity.artistSortBy
            val build = MaterialAlertDialogBuilder(context)
            build.setTitle(Constants.SORT_ORDER).setPositiveButton(Constants.YES) { _, _ ->
                if (MainActivity.artistSortBy != currentSort) {
                    MainActivity.artistSortBy = currentSort
                    when (MainActivity.artistSortBy) {
                        0 -> {
                            sortByArtistName()
                        }
                        1 -> {
                            sortByArtistSonAmount()
                        }
                    }
                    val editor =
                        context.getSharedPreferences(
                            Constants.SORTING_ARTIST,
                            AppCompatActivity.MODE_PRIVATE
                        ).edit()
                    editor.putInt(Constants.SORT_ORDER_FOR_ARTIST, MainActivity.artistSortBy)
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

        fun sortByAlbumName() {
            val newList: LinkedHashMap<String, ArrayList<Music>> =
                sortByName(
                    MainActivity.songByAlbum
                )
            MainActivity.songByAlbum = LinkedHashMap()
            MainActivity.songByAlbum = newList
            MainActivity.albumAdapter.updateForSort(MainActivity.songByAlbum)
        }

        fun sortByAlbumSongAmount() {
            val list = sortByMusicAmount(MainActivity.songByAlbum)
            MainActivity.songByAlbum = LinkedHashMap()
            for (x in list) {
                MainActivity.songByAlbum[x.key] = x.value
            }
            MainActivity.albumAdapter.updateForSort(MainActivity.songByAlbum)
        }

        fun sortByAlbum(context: Context) {
            val menuList = arrayOf(Constants.ALBUM_NAME, Constants.SONG_AMOUNT)
            var currentSort = MainActivity.albumSortBy
            val build = MaterialAlertDialogBuilder(context)
            build.setTitle(Constants.SORT_ORDER).setPositiveButton(Constants.YES) { _, _ ->
                if (MainActivity.albumSortBy != currentSort) {
                    MainActivity.albumSortBy = currentSort
                    when (MainActivity.albumSortBy) {
                        0 -> {
                            sortByAlbumName()
                        }
                        1 -> {
                            sortByAlbumSongAmount()
                        }
                    }
                    val editor =
                        context.getSharedPreferences(
                            Constants.SORTING_ALBUM,
                            AppCompatActivity.MODE_PRIVATE
                        ).edit()
                    editor.putInt(Constants.SORT_ORDER_FOR_ALBUM, MainActivity.albumSortBy)
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

        fun sortByMusic(context: Context) {
            val menuList =
                arrayOf(Constants.RECEANTLY_ADDED, Constants.SONG_TITLE, Constants.FILE_SIZE)
            var currentSort = MainActivity.sortBy
            val build = MaterialAlertDialogBuilder(context)
            build.setTitle(Constants.SORT_ORDER).setPositiveButton(Constants.YES) { _, _ ->
                if (MainActivity.sortBy != currentSort) {
                    MainActivity.sortBy = currentSort
                    when (MainActivity.sortBy) {
                        0 -> sortAllMusics(
                            MainActivity.MusicListMA, 0, MainActivity.MusicListMA.size - 1,
                            MainActivity.sortBy
                        )
                        1 -> sortAllMusics(
                            MainActivity.MusicListMA, 0, MainActivity.MusicListMA.size - 1,
                            MainActivity.sortBy
                        )
                        2 -> sortAllMusics(
                            MainActivity.MusicListMA, 0, MainActivity.MusicListMA.size - 1,
                            MainActivity.sortBy
                        )
                    }
                    MainActivity.musicAdapter.updateMusicList(MainActivity.MusicListMA)
                    val editor =
                        context.getSharedPreferences(
                            Constants.SORTING,
                            AppCompatActivity.MODE_PRIVATE
                        )
                            .edit()
                    editor.putInt(Constants.SORT_ORDER, currentSort)
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

        fun sortByMusicAmount(listOfAlbums: LinkedHashMap<String, ArrayList<Music>>): LinkedHashMap<String, ArrayList<Music>> {
            val companyFounderSet: ArrayList<Map.Entry<String, ArrayList<Music>>> = ArrayList()
            companyFounderSet.addAll(listOfAlbums.entries)

            val companyFounderListEntry: List<Map.Entry<String, ArrayList<Music>>> = ArrayList(
                companyFounderSet
            )
            Collections.sort(
                companyFounderListEntry
            ) { p0, p1 -> p1!!.value.size.compareTo(p0!!.value.size) }

            listOfAlbums.clear()

            for ((key, value) in companyFounderListEntry) {
                listOfAlbums[key] = value
            }

            return listOfAlbums
        }

        fun sortAllMusics(arr: ArrayList<Music>, low: Int, high: Int, sortType: Int) {
            if (low < high) {
                val pi = partitionAllMusic(arr, low, high, sortType)
                sortAllMusics(arr, low, pi - 1, sortType)
                sortAllMusics(arr, pi + 1, high, sortType)
            }
        }

        private fun partitionAllMusic(
            arr: ArrayList<Music>,
            low: Int,
            high: Int,
            sortType: Int
        ): Int {
            val pivot = arr[high]
            var i = low - 1
            for (j in low until high) {
                when (sortType) {
                    0 -> {
                        if (arr[j].date.lowercase() > pivot.date.lowercase()) {
                            i++
                            swapMusic(arr, i, j)
                        }
                    }
                    1 -> {
                        if (arr[j].title.lowercase() > pivot.title.lowercase()) {
                            i++
                            swapMusic(arr, i, j)
                        }
                    }
                    2 -> {
                        if (arr[j].size > pivot.size) {
                            i++
                            swapMusic(arr, i, j)
                        }
                    }
                }
            }
            swapMusic(arr, i + 1, high)
            return i + 1
        }

        private fun swapMusic(arr: ArrayList<Music>, i: Int, j: Int) {
            val temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }

        fun sortByName(arr: LinkedHashMap<String, ArrayList<Music>>): LinkedHashMap<String, ArrayList<Music>> {
            val names: ArrayList<String> = ArrayList()
            names.addAll(arr.keys)
            sortNames(names, 0, names.size - 1)
            val newList: LinkedHashMap<String, ArrayList<Music>> = LinkedHashMap()
            for (x in names) {
                newList[x] = arr[x]!!
            }
            return newList
        }

        private fun sortNames(arr: ArrayList<String>, low: Int, high: Int) {
            if (low < high) {
                val pi = partitionByName(arr, low, high)
                sortNames(arr, low, pi - 1)
                sortNames(arr, pi + 1, high)
            }
        }

        private fun partitionByName(arr: ArrayList<String>, low: Int, high: Int): Int {
            val pivot = arr[high]
            var i = low - 1
            for (j in low until high) {
                if (arr[j].lowercase() > pivot.lowercase()) {
                    i++
                    swapMusicByName(arr, i, j)
                }
            }
            swapMusicByName(arr, i + 1, high)
            return i + 1
        }

        private fun swapMusicByName(arr: ArrayList<String>, i: Int, j: Int) {
            val temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }

        fun sortCommandArtistSettings(context: Context) {
            val artistSortEditor = context.getSharedPreferences(
                Constants.SORTING_ARTIST,
                AppCompatActivity.MODE_PRIVATE
            )
            val sortValue = artistSortEditor.getInt(Constants.SORT_ORDER_FOR_ARTIST, 0)
            if (MainActivity.artistSortBy != sortValue) {
                MainActivity.artistSortBy = sortValue
                when (MainActivity.artistSortBy) {
                    0 -> {
                        sortByArtistName()
                    }
                    1 -> {
                        sortByArtistSonAmount()
                    }
                }
            }
        }

        fun sortCommandAlbumSettings(context: Context) {
            val albumSortEditor = context.getSharedPreferences(
                Constants.SORTING_ALBUM,
                AppCompatActivity.MODE_PRIVATE
            )
            val sortValue = albumSortEditor.getInt(Constants.SORT_ORDER_FOR_ALBUM, 0)
            if (MainActivity.albumSortBy != sortValue) {
                MainActivity.albumSortBy = sortValue
                when (MainActivity.albumSortBy) {
                    0 -> {
                        sortByAlbumName()
                    }
                    1 -> {
                        sortByAlbumSongAmount()
                    }
                }
            }
        }

        fun sortCommandSongSettings(context: Context) {
            val sortEditor =
                context.getSharedPreferences(Constants.SORT_ORDER, AppCompatActivity.MODE_PRIVATE)
            val sortValue = sortEditor.getInt(Constants.SORT_ORDER, 0)
            if (MainActivity.sortBy != sortValue) {
                when (MainActivity.sortBy) {
                    2 -> {
                        sortByMusic(context)
                    }
                    1 -> {
                        sortByAlbum(context)
                    }
                    0 -> {
                        sortByArtist(context)
                    }
                }
                MainActivity.musicAdapter.updateMusicList(MainActivity.MusicListMA)
            }
        }

        fun sortAllAtBeginning(context: Context) {
            val sortEditor =
                context.getSharedPreferences(Constants.SORTING, AppCompatActivity.MODE_PRIVATE)
            MainActivity.sortBy = sortEditor.getInt(Constants.SORT_ORDER, 0)

            val albumSortEditor = context.getSharedPreferences(
                Constants.SORTING_ALBUM,
                AppCompatActivity.MODE_PRIVATE
            )
            MainActivity.albumSortBy = albumSortEditor.getInt(Constants.SORT_ORDER_FOR_ALBUM, 0)

            val artistSortEditor = context.getSharedPreferences(
                Constants.SORTING_ARTIST,
                AppCompatActivity.MODE_PRIVATE
            )
            MainActivity.artistSortBy = artistSortEditor.getInt(Constants.SORT_ORDER_FOR_ARTIST, 0)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MainActivity.MusicListMA = Data.getAllAudio(context)
            }
            when (MainActivity.sortBy) {
                0 -> sortAllMusics(
                    MainActivity.MusicListMA, 0, MainActivity.MusicListMA.size - 1,
                    MainActivity.sortBy
                )
                1 -> sortAllMusics(
                    MainActivity.MusicListMA, 0, MainActivity.MusicListMA.size - 1,
                    MainActivity.sortBy
                )
                2 -> sortAllMusics(
                    MainActivity.MusicListMA, 0, MainActivity.MusicListMA.size - 1,
                    MainActivity.sortBy
                )
            }

            MainActivity.musicAdapter = MusicAdapter(context, MainActivity.MusicListMA)

            if (MainActivity.albumSortBy == 1) {
                val list = sortByMusicAmount(MainActivity.songByAlbum)
                MainActivity.songByAlbum = LinkedHashMap()
                for (x in list) {
                    MainActivity.songByAlbum[x.key] = x.value
                }
            } else {
                val newList: LinkedHashMap<String, ArrayList<Music>> = sortByName(
                    MainActivity.songByAlbum
                )
                MainActivity.songByAlbum = LinkedHashMap()
                MainActivity.songByAlbum = newList
            }
            MainActivity.albumAdapter = AlbumViewAdapter(context, MainActivity.songByAlbum)

            if (MainActivity.artistSortBy == 1) {
                val list = sortByMusicAmount(MainActivity.songByArtist)
                MainActivity.songByArtist = LinkedHashMap()
                for (x in list) {
                    MainActivity.songByArtist[x.key] = x.value
                }
            } else {
                val newList: LinkedHashMap<String, ArrayList<Music>> = sortByName(
                    MainActivity.songByArtist
                )
                MainActivity.songByArtist = LinkedHashMap()
                MainActivity.songByArtist = newList
            }
            MainActivity.artistAdapter = ArtistViewAdapter(context, MainActivity.songByArtist)
        }
    }
}