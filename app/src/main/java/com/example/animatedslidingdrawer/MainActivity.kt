package com.example.animatedslidingdrawer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.animatedslidingdrawer.databinding.ActivityMainBinding
import com.example.animatedslidingdrawer.databinding.DrawerLayoutBinding
import com.example.animatedslidingdrawer.fragment.HomeFragment
import com.example.animatedslidingdrawer.fragment.TrendingFragment
import com.example.animatedslidingdrawerlibrary.AnimatedSlidingDrawer
import com.example.animatedslidingdrawerlibrary.DrawerSlideGravity
import com.example.animatedslidingdrawerlibrary.SlidingRootNavBuilder
import com.example.animatedslidingdrawerlibrary.callback.DrawerDragListener
import com.example.animatedslidingdrawerlibrary.transform.CustomRootTransformation

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var animatedSlidingDrawer: AnimatedSlidingDrawer? = null
    private lateinit var drawerBinding: DrawerLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        replaceFragment(HomeFragment())

        // Initialize SlidingRootNav
        animatedSlidingDrawer = SlidingRootNavBuilder(this)
            .withMenuOpened(false)  // Initial state of the menu
            .withContentClickableWhenMenuOpened(true)
            .withSavedState(savedInstanceState)
            .withDragDistance(200)  // Distance for dragging
            .withRootViewScale(0.8f)  // Scale of the root view
            .withRootViewElevation(10)  // Elevation of the root view
            .withGravity(DrawerSlideGravity.RIGHT)  // Direction of the sliding menu
            .addRootTransformation(CustomRootTransformation())
            .withMenuLayout(R.layout.drawer_layout)  // Layout of the menu
            .addDragListener(object : DrawerDragListener {
                override fun onDrag(progress: Float) {
                    // Detect when drawer is closed
                    if (progress == 0f) {
                       // updateIcons(0)
                        replaceFragment(HomeFragment())
                        binding.bottomBar.selectedItemId = R.id.home
                    }
                }
            })
            .inject()

        // Bind drawer layout views
        val drawerView = animatedSlidingDrawer?.layout?.findViewById<View>(R.id.drawer_layout_root)
        if (drawerView != null) {
            drawerBinding = DrawerLayoutBinding.bind(drawerView)
        } else {
            Log.e("HomeMainActivity", "drawerView is null!")
            // Handle the null case, possibly by showing a user-friendly message or handling the error.
        }

        setUpBottomBar()
    }

    private fun setUpBottomBar() {
        binding.bottomBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> {
                    replaceFragment(HomeFragment())
                 //   binding.bottomBar.menu.findItem(R.id.home).icon?.setTint(getColor(R.color.darkRed))

                }
                R.id.trending -> {
                    replaceFragment(TrendingFragment())
                 //   binding.bottomBar.menu.findItem(R.id.trending).icon?.setTint(getColor(R.color.darkRed))

                }
                R.id.menu -> {
                    animatedSlidingDrawer?.openMenu()
                    window.statusBarColor = getColor(R.color.mainTextBlue)
                 //   binding.bottomBar.menu.findItem(R.id.menu).icon?.setTint(getColor(R.color.darkRed))

                }

                else -> {
                    Toast.makeText(this , "Something Went Wrong" , Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    //MAKE THE FUNCTION TO REPLACE THE FRAGMENT
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainFrameLayout , fragment)
        fragmentTransaction.commit()
    }
}