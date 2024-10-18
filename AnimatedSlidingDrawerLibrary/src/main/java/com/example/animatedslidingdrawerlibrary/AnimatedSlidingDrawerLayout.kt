package com.example.animatedslidingdrawerlibrary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragListener
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragStateListener
import com.example.animatedslidingdrawerlibrary.transform.RootTransformation
import kotlin.math.abs


class AnimatedSlidingDrawerLayout(context: Context?) : FrameLayout(context!!),
    AnimatedSlidingDrawer {
    private val FLING_MIN_VELOCITY =
        ViewConfiguration.get(context!!).scaledMinimumFlingVelocity.toFloat()

    override var isMenuLocked: Boolean = false
    override var isMenuClosed: Boolean = true
        private set
    private var isContentClickableWhenMenuOpened = false

    private var rootTransformation: RootTransformation? = null
    private var rootView: View? = null

    var dragProgress: Float = 0f
        private set
    private var maxDragDistance = 0
    private var dragState = 0

    private val dragHelper: ViewDragHelper
    private var positionHelper: DrawerSlideGravity.Helper? = null

    private val dragListeners: MutableList<DrawerDragListener> =
        ArrayList()
    private val dragStateListeners: MutableList<DrawerDragStateListener> = ArrayList()

    init {
        dragHelper = ViewDragHelper.create(this, ViewDragCallback())
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return ((!isMenuLocked
                && dragHelper.shouldInterceptTouchEvent(ev))
                || shouldBlockClick(ev))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child === rootView) {
                val rootLeft = positionHelper!!.getRootLeft(dragProgress, maxDragDistance)
                child.layout(rootLeft, top, rootLeft + (right - left), bottom)
            } else {
                child.layout(left, top, right, bottom)
            }
        }
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun changeMenuVisibility(animated: Boolean, newDragProgress: Float) {
        isMenuClosed = calculateIsMenuHidden()
        if (animated) {
            val left = positionHelper!!.getLeftToSettle(newDragProgress, maxDragDistance)
            if (dragHelper.smoothSlideViewTo(rootView!!, left, rootView!!.top)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
        } else {
            dragProgress = newDragProgress
            rootTransformation!!.transform(dragProgress, rootView)
            requestLayout()
        }
    }

    override val isMenuOpened: Boolean
        get() = !isMenuClosed

    override val layout: AnimatedSlidingDrawerLayout
        get() = this

    override fun closeMenu() {
        closeMenu(true)
    }

    override fun closeMenu(animated: Boolean) {
        changeMenuVisibility(animated, 0f)
    }

    override fun openMenu() {
        openMenu(true)
    }

    override fun openMenu(animated: Boolean) {
        changeMenuVisibility(animated, 1f)
    }

    fun setRootView(view: View?) {
        rootView = view
    }

    fun setContentClickableWhenMenuOpened(contentClickableWhenMenuOpened: Boolean) {
        isContentClickableWhenMenuOpened = contentClickableWhenMenuOpened
    }

    fun setRootTransformation(transformation: RootTransformation?) {
        rootTransformation = transformation
    }

    fun setMaxDragDistance(maxDragDistance: Int) {
        this.maxDragDistance = maxDragDistance
    }

    fun setGravity(gravity: DrawerSlideGravity) {
        positionHelper = gravity.createHelper()
        positionHelper!!.enableEdgeTrackingOn(dragHelper)
    }

    fun addDragListener(listener: DrawerDragListener) {
        dragListeners.add(listener)
    }

    fun addDragStateListener(listener: DrawerDragStateListener) {
        dragStateListeners.add(listener)
    }

    fun removeDragListener(listener: DrawerDragListener) {
        dragListeners.remove(listener)
    }

    fun removeDragStateListener(listener: DrawerDragStateListener) {
        dragStateListeners.remove(listener)
    }

    private fun shouldBlockClick(event: MotionEvent): Boolean {
        if (isContentClickableWhenMenuOpened) {
            return false
        }
        if (rootView != null && isMenuOpened) {
            rootView!!.getHitRect(tempRect)
            return tempRect.contains(event.x.toInt(), event.y.toInt())
        }
        return false
    }

    private fun notifyDrag() {
        for (listener in dragListeners) {
            listener.onDrag(dragProgress)
        }
    }

    private fun notifyDragStart() {
        for (listener in dragStateListeners) {
            listener.onDragStart()
        }
    }

    private fun notifyDragEnd(isOpened: Boolean) {
        for (listener in dragStateListeners) {
            listener.onDragEnd(isOpened)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = Bundle()
        savedState.putParcelable(EXTRA_SUPER, super.onSaveInstanceState())
        savedState.putInt(EXTRA_IS_OPENED, if (dragProgress > 0.5) 1 else 0)
        savedState.putBoolean(EXTRA_SHOULD_BLOCK_CLICK, isContentClickableWhenMenuOpened)
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle
        super.onRestoreInstanceState(savedState.getParcelable(EXTRA_SUPER))
        changeMenuVisibility(false, savedState.getInt(EXTRA_IS_OPENED, 0).toFloat())
        isMenuClosed = calculateIsMenuHidden()
        isContentClickableWhenMenuOpened = savedState.getBoolean(EXTRA_SHOULD_BLOCK_CLICK)
    }

    private fun calculateIsMenuHidden(): Boolean {
        return dragProgress == 0f
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {
        private var edgeTouched = false

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (isMenuLocked) {
                return false
            }
            val isOnEdge = edgeTouched
            edgeTouched = false
            if (isMenuClosed) {
                return child === rootView && isOnEdge
            } else {
                if (child !== rootView) {
                    dragHelper.captureChildView(rootView!!, pointerId)
                    return false
                }
                return true
            }
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int,
        ) {
            dragProgress = positionHelper!!.getDragProgress(left, maxDragDistance)
            rootTransformation!!.transform(dragProgress, rootView)
            notifyDrag()
            invalidate()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val left =
                if (abs(xvel.toDouble()) < FLING_MIN_VELOCITY) positionHelper!!.getLeftToSettle(
                    dragProgress,
                    maxDragDistance
                ) else positionHelper!!.getLeftAfterFling(xvel, maxDragDistance)
            dragHelper.settleCapturedViewAt(left, rootView!!.top)
            invalidate()
        }

        override fun onViewDragStateChanged(state: Int) {
            if (dragState == ViewDragHelper.STATE_IDLE && state != ViewDragHelper.STATE_IDLE) {
                notifyDragStart()
            } else if (dragState != ViewDragHelper.STATE_IDLE && state == ViewDragHelper.STATE_IDLE) {
                // Check if the menu is hidden/closed
                isMenuClosed = calculateIsMenuHidden() // Make sure this method is defined correctly
                notifyDragEnd(isMenuOpened) // Ensure isMenuOpened is defined and updated correctly
            }
            // Update the current drag state
            dragState = state
        }


        override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
            edgeTouched = true
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return if (child === rootView) maxDragDistance else 0
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return positionHelper!!.clampViewPosition(left, maxDragDistance)
        }
    }

    companion object {
        private const val EXTRA_IS_OPENED = "extra_is_opened"
        private const val EXTRA_SUPER = "extra_super"
        private const val EXTRA_SHOULD_BLOCK_CLICK = "extra_should_block_click"

        private val tempRect = Rect()
    }
}