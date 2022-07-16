package com.example.musicplayer.Music_Stuff

import android.content.ContentUris
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.musicplayer.Activity.Player
import com.example.musicplayer.R

class PlayerMenu : DialogFragment() {
    var root: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.player_menu, container, false)
        root = rootView
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val wmlp = dialog!!.window!!.attributes
        wmlp.gravity = Gravity.BOTTOM or Gravity.CENTER
        wmlp.x = 100
        wmlp.y = 100

        rootView.findViewById<LinearLayout>(R.id.alarm_ringtone_player_menu_FD).setOnClickListener {
            try {
                if (checkSystemWritePermission()) {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        (Player.musicListPA[Player.songPosition].id).toLong()
                    )
                    RingtoneManager.setActualDefaultRingtoneUri(
                        context,
                        RingtoneManager.TYPE_ALARM,
                        uri
                    )
                    Toast.makeText(
                        context,
                        Constants.ALARM_SUCCESSFULLY,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(
                        context,
                        Constants.ALLOW_MODIFY_SYSTEM_SETTINGS_NO,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    Constants.ALARM_UN_SUCCESSFULLY,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        rootView.findViewById<LinearLayout>(R.id.ringtone_player_menu_FD).setOnClickListener {
            try {
                if (checkSystemWritePermission()) {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        (Player.musicListPA[Player.songPosition].id).toLong()
                    )
                    RingtoneManager.setActualDefaultRingtoneUri(
                        context,
                        RingtoneManager.TYPE_RINGTONE,
                        uri
                    )
                    Toast.makeText(
                        context,
                        Constants.RINGTONE_SUCCESSFULLY,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(
                        context,
                        Constants.ALLOW_MODIFY_SYSTEM_SETTINGS_NO,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, Constants.RINGTONE_UN_SUCCESSFULLY, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return rootView
    }

    private fun checkSystemWritePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context))
                return true
            else openAndroidPermissionsMenu()
        }
        return false
    }

    private fun openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse(Constants.PACKAGE + (context?.packageName ?: ""))
            this.startActivity(intent)
        }
    }
}