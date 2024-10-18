package com.example.animatedslidingdrawerlibrary.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.example.animatedslidingdrawerlibrary.AnimatedSlidingDrawerLayout


class HiddenMenuClickConsumer(context: Context?) : View(context) {
    private var menuHost: AnimatedSlidingDrawerLayout? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return menuHost!!.isMenuClosed
    }

    fun setMenuHost(layout: AnimatedSlidingDrawerLayout?) {
        this.menuHost = layout
    }
}