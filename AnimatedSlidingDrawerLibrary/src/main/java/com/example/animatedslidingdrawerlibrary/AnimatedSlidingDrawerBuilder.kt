package com.example.animatedslidingdrawerlibrary

import android.R
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragListener
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragStateListener
import com.example.animatedslidingdrawerlibrary.transform.CompositeTransformation
import com.example.animatedslidingdrawerlibrary.transform.ElevationTransformation
import com.example.animatedslidingdrawerlibrary.transform.RootTransformation
import com.example.animatedslidingdrawerlibrary.transform.ScaleTransformation
import com.example.animatedslidingdrawerlibrary.transform.YTranslationTransformation
import com.example.animatedslidingdrawerlibrary.util.ActionBarToggleAdapter
import com.example.animatedslidingdrawerlibrary.util.DrawerListenerAdapter
import com.example.animatedslidingdrawerlibrary.util.HiddenMenuClickConsumer
import java.util.Arrays


class SlidingRootNavBuilder(private val activity: Activity) {
    private var contentView: ViewGroup? = null

    private var menuView: View? = null
    private var menuLayoutRes = 0

    private val transformations: MutableList<RootTransformation> = ArrayList()

    private val dragListeners: MutableList<DrawerDragListener>

    private val dragStateListeners: MutableList<DrawerDragStateListener>

    private var dragDistance: Int

    private var toolbar: Toolbar? = null

    private var gravity: DrawerSlideGravity

    private var isMenuOpened = false

    private var isMenuLocked = false

    private var isContentClickableWhenMenuOpened: Boolean

    private var savedState: Bundle? = null

    init {
        this.dragListeners = ArrayList<DrawerDragListener>()
        this.dragStateListeners = ArrayList<DrawerDragStateListener>()
        this.gravity = DrawerSlideGravity.LEFT
        this.dragDistance = dpToPx(DEFAULT_DRAG_DIST_DP)
        this.isContentClickableWhenMenuOpened = true
    }

    fun withMenuView(view: View?): SlidingRootNavBuilder {
        menuView = view
        return this
    }

    fun withMenuLayout(@LayoutRes layout: Int): SlidingRootNavBuilder {
        menuLayoutRes = layout
        return this
    }

    fun withToolbarMenuToggle(tb: Toolbar?): SlidingRootNavBuilder {
        toolbar = tb
        return this
    }

    fun withGravity(g: DrawerSlideGravity): SlidingRootNavBuilder {
        gravity = g
        return this
    }

    fun withContentView(cv: ViewGroup?): SlidingRootNavBuilder {
        contentView = cv
        return this
    }

    fun withMenuLocked(locked: Boolean): SlidingRootNavBuilder {
        isMenuLocked = locked
        return this
    }

    fun withSavedState(state: Bundle?): SlidingRootNavBuilder {
        savedState = state
        return this
    }

    fun withMenuOpened(opened: Boolean): SlidingRootNavBuilder {
        isMenuOpened = opened
        return this
    }

    fun withContentClickableWhenMenuOpened(clickable: Boolean): SlidingRootNavBuilder {
        isContentClickableWhenMenuOpened = clickable
        return this
    }

    fun withDragDistance(dp: Int): SlidingRootNavBuilder {
        return withDragDistancePx(dpToPx(dp))
    }

    fun withDragDistancePx(px: Int): SlidingRootNavBuilder {
        dragDistance = px
        return this
    }

    fun withRootViewScale(@FloatRange(from = 0.01) scale: Float) = apply {
        transformations.add(ScaleTransformation(scale))
    }

    fun withRootViewElevation(elevation: Int) = withRootViewElevationPx(dpToPx(elevation))

    fun withRootViewElevationPx(elevation: Int) = apply {
        if (elevation >= 0) {
            transformations.add(ElevationTransformation(elevation.toFloat()))
        } else {
            throw IllegalArgumentException("Elevation must be non-negative")
        }
    }


    fun withRootViewYTranslation(translation: Int): SlidingRootNavBuilder {
        return withRootViewYTranslationPx(dpToPx(translation))
    }

    fun withRootViewYTranslationPx(translation: Int): SlidingRootNavBuilder {
        transformations.add(YTranslationTransformation(translation.toFloat()))
        return this
    }

    fun addRootTransformation(transformation: RootTransformation): SlidingRootNavBuilder {
        transformations.add(transformation)
        return this
    }

    fun addDragListener(dragListener: DrawerDragListener): SlidingRootNavBuilder {
        dragListeners.add(dragListener)
        return this
    }

    fun addDragStateListener(dragStateListener: DrawerDragStateListener): SlidingRootNavBuilder {
        dragStateListeners.add(dragStateListener)
        return this
    }

    fun inject(): AnimatedSlidingDrawer {
        val contentView = getContentView()

        val oldRoot = contentView!!.getChildAt(0)
        contentView.removeAllViews()

        val newRoot: AnimatedSlidingDrawerLayout = createAndInitNewRoot(oldRoot)

        val menu = getMenuViewFor(newRoot)

        initToolbarMenuVisibilityToggle(newRoot, menu)

        val clickConsumer = HiddenMenuClickConsumer(activity)
        clickConsumer.setMenuHost(newRoot)

        newRoot.addView(menu)
        newRoot.addView(clickConsumer)
        newRoot.addView(oldRoot)

        contentView.addView(newRoot)

        if (savedState == null && isMenuOpened) {
            newRoot.openMenu(false)
        }

        newRoot.isMenuLocked = isMenuLocked

        return newRoot
    }

    private fun createAndInitNewRoot(oldRoot: View): AnimatedSlidingDrawerLayout {
        val newRoot: AnimatedSlidingDrawerLayout = AnimatedSlidingDrawerLayout(activity)
        newRoot.setId(R.id.content)
        newRoot.setRootTransformation(createCompositeTransformation())
        newRoot.setMaxDragDistance(dragDistance)
        newRoot.setGravity(gravity)
        newRoot.setRootView(oldRoot)
        newRoot.setContentClickableWhenMenuOpened(isContentClickableWhenMenuOpened)
        for (l in dragListeners) {
            newRoot.addDragListener(l)
        }
        for (l in dragStateListeners) {
            newRoot.addDragStateListener(l)
        }
        return newRoot
    }

    private fun getContentView(): ViewGroup? {
        if (contentView == null) {
            contentView = activity.findViewById<View>(R.id.content) as ViewGroup
        }
        check(contentView!!.childCount == 1) { activity.getString(R.string.ok) }
        return contentView
    }

    private fun getMenuViewFor(parent: AnimatedSlidingDrawerLayout): View? {
        if (menuView == null) {
            check(menuLayoutRes != 0) { activity.getString(R.string.no) }
            menuView = LayoutInflater.from(activity).inflate(menuLayoutRes, parent, false)
        }
        return menuView
    }

    private fun createCompositeTransformation(): RootTransformation {
        return if (transformations.isEmpty()) {
            CompositeTransformation(
                Arrays.asList(
                    ScaleTransformation(DEFAULT_END_SCALE),
                    ElevationTransformation(dpToPx(DEFAULT_END_ELEVATION_DP).toFloat())
                )
            )
        } else {
            CompositeTransformation(transformations)
        }
    }

    protected fun initToolbarMenuVisibilityToggle(sideNav: AnimatedSlidingDrawerLayout, drawer: View?) {
        if (toolbar != null) {
            val dlAdapter = ActionBarToggleAdapter(activity)
            dlAdapter.setAdaptee(sideNav)
            val toggle = ActionBarDrawerToggle(
                activity, dlAdapter, toolbar,
                R.string.copy,
                R.string.copy
            )
            toggle.syncState()
            val listenerAdapter = DrawerListenerAdapter(toggle, drawer!!)
            sideNav.addDragListener(listenerAdapter)
            sideNav.addDragStateListener(listenerAdapter)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return Math.round(activity.resources.displayMetrics.density * dp)
    }

    companion object {
        private const val DEFAULT_END_SCALE = 0.65f
        private const val DEFAULT_END_ELEVATION_DP = 8
        private const val DEFAULT_DRAG_DIST_DP = 180
    }
}
