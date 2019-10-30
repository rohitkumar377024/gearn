package com.example.goodearning.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.goodearning.R

class PointsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hideSubmitBtn()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_points, container, false)
    }

    /* Hides 'Submit' Button of the MainActivity */
    private fun hideSubmitBtn() { activity?.findViewById<Button>(R.id.toolbar_submit_btn)?.visibility = View.GONE }
}
