package com.example.animatedslidingdrawerlibrary.transform

import android.view.View


interface RootTransformation {
    fun transform(dragProgress: Float, rootView: View?)
}