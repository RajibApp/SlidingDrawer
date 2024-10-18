package com.example.animatedslidingdrawerlibrary.callback


interface DrawerDragStateListener {
    fun onDragStart()

    fun onDragEnd(isMenuOpened: Boolean)
}