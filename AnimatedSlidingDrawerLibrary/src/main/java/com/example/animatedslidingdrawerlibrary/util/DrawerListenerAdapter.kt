package com.example.animatedslidingdrawerlibrary.util

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragListener
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragStateListener


class DrawerListenerAdapter(private val adaptee: DrawerListener, private val drawer: View) :
    DrawerDragListener, DrawerDragStateListener {
    override fun onDrag(progress: Float) {
        adaptee.onDrawerSlide(drawer, progress)
    }

    override fun onDragStart() {
        adaptee.onDrawerStateChanged(DrawerLayout.STATE_DRAGGING)
    }

    override fun onDragEnd(isMenuOpened: Boolean) {
        if (isMenuOpened) {
            adaptee.onDrawerOpened(drawer)
        } else {
            adaptee.onDrawerClosed(drawer)
        }
        adaptee.onDrawerStateChanged(DrawerLayout.STATE_IDLE)
    }
}