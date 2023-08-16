package com.click.clickrecord.activity

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.click.clickrecord.R
import com.click.clickrecord.databinding.ActivityMainBinding
import com.click.clickrecord.dialogfragment.WarningDialogFragment
import com.click.clickrecord.service.AppForegroundService
import com.click.clickrecord.service.ClickRecordService
import com.click.clickrecord.util.Utils
import com.click.clickrecord.viewmodel.ViewModelMain


class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var receiver: MyBroadcastReceiver
    private val RECORD_ACTION = "record-action"
    private val notificationId = 101010

    private var isRecording = false
    private var isShowNotification = false

    private var warningDialogFragment: WarningDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!Utils.isNotificationEnabled(this)) {
            Utils.setNotification(this)
            Utils.showToast(this, "请在设置中允许通知后打开应用")
            finish()
        }

        receiver = MyBroadcastReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(RECORD_ACTION)
        registerReceiver(receiver, filter)

        binding.startRecordBtn.setOnClickListener {
            if (Utils.isServiceRunning(ClickRecordService::class.java.canonicalName, this)) {
                if (!isShowNotification) {
                    if (ClickRecordService.isSaveFinished()) {
                        showWarningDialogFragment()
                    } else {
                        Utils.showToast(this, "录制结果未保存完成")
                    }
                } else {
                    Utils.showToast(this, "请前往通知栏操作")
                }
            } else {
                Utils.accessibilityToSettingPage(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Utils.isServiceRunning(AppForegroundService::class.java.canonicalName, this) && Utils.isNotificationEnabled(this)) {
            try {
                val intent = Intent(this, AppForegroundService::class.java)
                startForegroundService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        ViewModelMain.exceptionExit.value = true
        unregisterReceiver(receiver)
        removeNotification()
        stopService(Intent(this, AppForegroundService::class.java))
        super.onDestroy()
    }

    private fun showWarningDialogFragment() {
        if (warningDialogFragment == null) {
            warningDialogFragment = WarningDialogFragment()
            warningDialogFragment!!.setOnConfirmListener {
                warningDialogFragment!!.dismissAllowingStateLoss()
                sendNotification()
            }
        }
        if (warningDialogFragment?.isAdded == false) {
            warningDialogFragment?.show(supportFragmentManager, null)
        }
    }

    private fun sendNotification() {
        val intent = Intent().apply {
            action = RECORD_ACTION
        }

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification =  NotificationCompat.Builder(this, "click-record")
            .setContentTitle("ClickRecord")
            .setContentText(if (isRecording) "点击结束录制" else "点击开始录制")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        notification.flags = Notification.FLAG_NO_CLEAR
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, notification)
            isShowNotification = true
        }
    }

    private fun removeNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(notificationId)
            isShowNotification = false
        }
    }


    inner class MyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == RECORD_ACTION) {
                isRecording = !isRecording
                if (!isRecording) {
                    Utils.showToast(this@MainActivity, "结束录制")
                    ViewModelMain.isShowFloatView.value = false
                    removeNotification()
                } else {
                    Utils.showToast(this@MainActivity, "开始录制")
                    ViewModelMain.isShowFloatView.value = true
                    sendNotification()
                }
            }
        }
    }
}