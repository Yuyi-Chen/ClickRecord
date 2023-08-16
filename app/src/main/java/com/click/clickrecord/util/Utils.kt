package com.click.clickrecord.util

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.click.clickrecord.service.ClickRecordService
import java.io.File
import java.util.ArrayList

object Utils {
    /**
     * 判断当前app在手机中是否开启了允许消息推送
     * @param mContext Context
     * @return Boolean
     */
    fun isNotificationEnabled(context: Context): Boolean {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = mNotificationManager.getNotificationChannel("click-record")
        return !(!mNotificationManager.areNotificationsEnabled() || channel.importance == NotificationManager.IMPORTANCE_NONE)
    }

    /**
     * 打开手机设置页面
     * @param context Context
     */
    fun setNotification(context: Context) {
        val localIntent = Intent()
        localIntent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        localIntent.putExtra("app_uid", context.applicationInfo.uid)
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(localIntent)
    }

    /**
     * 跳转到无障碍页面
     */
    fun accessibilityToSettingPage(context: Context) {
        //开启辅助功能页面
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            e.printStackTrace()
        }
    }

    /**
     * 是否开启无障碍服务
     */
    fun isServiceRunning(serviceName: String, context: Context): Boolean {
        val myManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningService =
            myManager.getRunningServices(1000) as ArrayList<ActivityManager.RunningServiceInfo>
        for (i in runningService.indices) {
            if (runningService[i].service.className == serviceName) {
                return true
            }
        }
        return false
    }

    fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, "RecordFile").apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}