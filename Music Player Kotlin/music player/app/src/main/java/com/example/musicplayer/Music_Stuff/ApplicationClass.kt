package com.example.musicplayer.Music_Stuff

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.musicplayer.Music_Stuff.Constants.Companion.IMPORTANT_CHANNEL
import com.example.musicplayer.Music_Stuff.Constants.Companion.NOW_PLAYING_SONG

class ApplicationClass : Application() {
    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
        const val REPEAT = "repeat"
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                NOW_PLAYING_SONG,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = IMPORTANT_CHANNEL
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}