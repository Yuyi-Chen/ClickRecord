package com.click.clickrecord.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenUtil {
    /**
     * 获取当前屏幕的尺寸大小，不包含状态栏
     * @param context
     * @return
     */
    fun getMetrics(context: Context): DisplayMetrics {
        val metrics = DisplayMetrics()
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(metrics)
        return metrics
    }

    /**
     * 获取当前屏幕包含状态栏的尺寸大小
     * @param context
     * @return
     */
    fun getMetricsFull(context: Context): DisplayMetrics {
        val metrics = DisplayMetrics()
        val windowMgr = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowMgr.defaultDisplay.getRealMetrics(metrics)
        return metrics
    }

    /**
     * 根据手机分辨率从dp转成px
     *
     * @param context
     * @param dpValue
     * @return
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}