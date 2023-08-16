package com.click.clickrecord

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.click.clickrecord.service.AppForegroundService

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("click-record", "service", NotificationManager.IMPORTANCE_HIGH)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}