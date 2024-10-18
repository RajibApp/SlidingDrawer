package com.example.animatedslidingdrawerlibrary.transform

import android.os.Build
import android.view.View
import com.example.animatedslidingdrawerlibrary.util.SideNavUtils


class ElevationTransformation(private val endElevation: Float) : RootTransformation {

    override fun transform(dragProgress: Float, rootView: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val elevation = SideNavUtils.evaluate(
                dragProgress, START_ELEVATION,
                endElevation
            )
            if (rootView != null) {
                rootView.elevation = elevation
            }
        }
    }
    companion object {
        private const val START_ELEVATION = 0f
    }


}