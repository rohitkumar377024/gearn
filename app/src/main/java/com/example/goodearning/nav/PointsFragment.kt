package com.example.goodearning.nav

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.example.goodearning.R

class PointsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_points, container, false)
    }

    /* Below 2 Functions Are Used for Hiding Skip Menu Item in This Fragment */
    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.menu_item_submit)
        if (item != null)
            item.isVisible = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

}
