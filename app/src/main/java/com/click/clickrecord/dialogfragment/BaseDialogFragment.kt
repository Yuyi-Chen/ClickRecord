package com.click.clickrecord.dialogfragment

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.click.clickrecord.R
import com.click.clickrecord.util.ScreenUtil

open class BaseDialogFragment : DialogFragment() {
    private var onDismiss: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme)
    }

    override fun onStart() {
        super.onStart()
        val windowParams = dialog?.window?.attributes
        windowParams?.width = getWidth()
        windowParams?.height = getHeight()
        windowParams?.gravity = getGravity()
        dialog?.window?.attributes = windowParams
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val mDismissed = DialogFragment::class.java.getDeclaredField("mDismissed")
            mDismissed.isAccessible = true
            mDismissed[this] = false
            val mShownByMe = DialogFragment::class.java.getDeclaredField("mShownByMe")
            mShownByMe.isAccessible = true
            mShownByMe[this] = true
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    fun showDialogFragment(activity: Activity?, fragmentManager: FragmentManager) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }
        closeExistSameDialog(this, fragmentManager)
        show(fragmentManager, this.javaClass.simpleName + this.hashCode())
        fragmentManager.executePendingTransactions()
    }

    fun setOnDismissListener(listener: () -> Unit) {
        this.onDismiss = listener
    }

    protected fun getWidth(): Int {
        return WindowManager.LayoutParams.MATCH_PARENT
    }

    protected fun getHeight(): Int {
        return ScreenUtil.getMetrics(requireContext()).heightPixels
    }

    protected fun getGravity(): Int {
        return Gravity.CENTER
    }

    private fun closeExistSameDialog(
        dialogFragment: DialogFragment,
        fragmentManager: FragmentManager
    ) {
        val fragment = fragmentManager.findFragmentByTag(dialogFragment.javaClass.simpleName)
        if (fragment is DialogFragment) {
            fragment.dismissAllowingStateLoss()
        }
    }
}