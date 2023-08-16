package com.click.clickrecord.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.click.clickrecord.util.ScreenUtil
import com.click.clickrecord.util.Utils
import com.click.clickrecord.viewmodel.ViewModelMain
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class ClickRecordService : AccessibilityService(), LifecycleOwner {
    private lateinit var windowManager: WindowManager
    private var floatView: View? = null
    private val outMetrics = DisplayMetrics()
    private val mLifecycleRegistry = LifecycleRegistry(this)
    private lateinit var layoutParam: WindowManager.LayoutParams

    private var path: Path? = null
    private var isMove = false
    private var startTime = 0L
    private var lastTime = -1L

    private var startPosition = arrayOfNulls<Float>(2)

    private val timeFormatter = "yyyy-MM-dd-HH-mm-ss-SSS"

    private val circlePaint = Paint()
    private val textPaint = Paint()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // do nothing
    }

    override fun onInterrupt() {
        // do nothing
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }


    override fun onCreate() {
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        ViewModelMain.isShowFloatView.observe(this) {
            if (it) {
                showFlotView()
                result.clear()
            } else {
                removeFloatView()
                result.removeLast()
                saveResult()
            }
        }
        ViewModelMain.exceptionExit.observe(this) {
            removeFloatView()
            result.clear()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFlotView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(outMetrics)
        layoutParam = WindowManager.LayoutParams()
        layoutParam.apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = PixelFormat.TRANSPARENT
        }
        floatView = View(this)
        floatView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path = Path()
                    isMove = false
                    startTime = System.currentTimeMillis()
                    startPosition[0] = event.rawX
                    startPosition[1] = event.rawY
                    path?.moveTo(event.rawX, event.rawY)
                }
                MotionEvent.ACTION_MOVE -> {
                    isMove = true
                }
                MotionEvent.ACTION_UP -> {
                    path?.let {
                        val callback = object : GestureResultCallback() {
                            override fun onCompleted(gestureDescription: GestureDescription?) {
                                layoutParam.flags =
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                windowManager.updateViewLayout(v, layoutParam)
                            }

                            override fun onCancelled(gestureDescription: GestureDescription?) {
                                layoutParam.flags =
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                windowManager.updateViewLayout(v, layoutParam)
                            }
                        }
                        if (isMove) {
                            path?.lineTo(event.rawX, event.rawY)
                        } else {
                            if (!isSame(event.rawX, event.rawY)) {
                                result.add(arrayOf(event.rawX, event.rawY))
                                lastTime = System.currentTimeMillis()
                            }
                        }
                        val gestureDescription = GestureDescription.Builder()
                            .addStroke(
                                GestureDescription.StrokeDescription(
                                    path!!,
                                    0,
                                    abs(System.currentTimeMillis() - startTime)
                                )
                            )
                            .build()
                        layoutParam.flags =
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        windowManager.updateViewLayout(v, layoutParam)
                        dispatchGesture(gestureDescription, callback, null)
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    val fullMetrics = ScreenUtil.getMetricsFull(this)
                    val metrics = ScreenUtil.getMetrics(this)
                    if (startPosition[0]!! < fullMetrics.heightPixels - metrics.heightPixels) {
                        if (startPosition[1]!! <= (fullMetrics.widthPixels / 2)) {
                            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                        } else {
                            performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
                        }
                    }
                }
                else -> {
                    Log.d("cpw-test", "${event.action}")
                    // do nothing
                }
            }
            false
        }
        windowManager.addView(floatView, layoutParam)
    }

    private fun removeFloatView() {
        if (!isNull(floatView)) {
            if (!isNull(floatView?.windowToken)) {
                if (!isNull(windowManager)) {
                    windowManager.removeView(floatView)
                }
            }
        }
    }

    private fun saveResult() {
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.parseColor("#662979FF")

        textPaint.isAntiAlias = true
        textPaint.color = Color.BLACK
        textPaint.strokeWidth = 5f
        textPaint.textSize = 16f

        val metrics = ScreenUtil.getMetricsFull(this)
        val bitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        for ((index, point) in result.withIndex()) {
            canvas.drawCircle(point[0], point[1], 17f, circlePaint)
            val text = (index + 1).toString()
            val textBounds = Rect()
             textPaint.getTextBounds(text, 0, text.length, textBounds)
            canvas.drawText(text, point[0] - (textBounds.width() / 2), point[1] + (textBounds.height() / 2), textPaint)
        }
        val file = File(Utils.getOutputDirectory(this), "${
            SimpleDateFormat(timeFormatter, Locale.CHINA)
            .format(System.currentTimeMillis())}.png")
        val fileOutputStream = FileOutputStream(file)
        if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) {
            fileOutputStream.flush()
            fileOutputStream.close()
        }
        result.clear()
        Utils.showToast(this, "录制结果保存成功")
    }

    private fun isNull(any: Any?): Boolean = any == null

    private fun isSame(x: Float, y: Float): Boolean {
        return if (result.isEmpty()) {
            false
        } else {
            abs(result.last()[0] - x) < 1 && abs(result.last()[1] - y) < 1 && System.currentTimeMillis() - lastTime < 1000
        }
    }

    override fun getLifecycle() = mLifecycleRegistry

    companion object {
        private val result = mutableListOf<Array<Float>>()

        fun isSaveFinished() = result.isEmpty()
    }

}