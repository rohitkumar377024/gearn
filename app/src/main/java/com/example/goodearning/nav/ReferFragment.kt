package com.example.goodearning.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.goodearning.R

class ReferFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hideSubmitBtn()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_refer, container, false)
    }

    /* Hides 'Submit' Button of the MainActivity */
    private fun hideSubmitBtn() { activity?.findViewById<Button>(R.id.toolbar_submit_btn)?.visibility = View.GONE }

}
