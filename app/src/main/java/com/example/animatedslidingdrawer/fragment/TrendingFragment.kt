package com.example.animatedslidingdrawer.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.animatedslidingdrawer.R

class TrendingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment


        // Set status bar color to match HomeFragment background
        requireActivity().window.statusBarColor = requireContext().getColor(R.color.grey)
        return inflater.inflate(R.layout.fragment_trending, container, false)
    }

}