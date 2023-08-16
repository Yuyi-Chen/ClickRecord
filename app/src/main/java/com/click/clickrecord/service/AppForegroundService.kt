package com.click.clickrecord.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.click.clickrecord.R

class AppForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "click-record")
            .setContentTitle("ClickRecord")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ClickRecord")
            .setContentText("防止进程被系统杀死，请保留该权限")
            .build()
    }
}