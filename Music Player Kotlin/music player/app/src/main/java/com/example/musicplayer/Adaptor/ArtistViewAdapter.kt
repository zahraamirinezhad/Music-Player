package com.example.musicplayer.Adaptor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.Music_Stuff.Music
import com.example.musicplayer.Music_Stuff.getImageArt
import com.example.musicplayer.R
import com.example.musicplayer.Activity.ShowByArtistDetails
import com.example.musicplayer.databinding.ArtistViewBinding


class ArtistViewAdapter(
    private val context: Context, private var listOfArtists: LinkedHashMap<String, ArrayList<Music>>
) :
    RecyclerView.Adapter<ArtistViewAdapter.MyHolder>() {

    class MyHolder(binding: ArtistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.artistImg
        val name = binding.artistName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(ArtistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("NotifyDataSetChanged", "RestrictedApi")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = listOfArtists.keys.elementAt(position)

        if (listOfArtists[listOfArtists.keys.elementAt(position)]?.size != 0) {
            try {
                val img = getImageArt(
                    listOfArtists[listOfArtists.keys.elementAt(position)]?.get(
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


        holder.root.setOnClickListener {
            val intent = Intent(context, ShowByArtistDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return listOfArtists.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAll() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateForSort(newList: LinkedHashMap<String, ArrayList<Music>>) {
        listOfArtists = LinkedHashMap()
        for (x in newList.keys)
            listOfArtists[x] = newList[x]!!
        notifyDataSetChanged()
    }
}