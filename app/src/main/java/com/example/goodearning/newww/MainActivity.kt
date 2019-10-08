package com.example.goodearning.newww

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.goodearning.R
import com.example.goodearning.newww.fragments.PointsFragment
import com.example.goodearning.newww.fragments.ReferFragment
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        /* Configures the Side Navigation Drawer */
       setupSideNavigation()
    }

    /* Handles the Navigation Drawer */
    private fun setupSideNavigation() {
        val result = DrawerBuilder().withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(createAccountHeader())
            .addDrawerItems(
                PrimaryDrawerItem().withName("Answer and Earn"),
                PrimaryDrawerItem().withName("Skipped Questions"),
                PrimaryDrawerItem().withName("Points Summary"),
                PrimaryDrawerItem().withName("Refer"),
                PrimaryDrawerItem().withName("Logout")
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    when (position) {
                        1 -> Log.d("main_activity", "answer and earn")
                        2 -> Log.d("main_activity", "skipping questions")
                        3 -> insertFragment(PointsFragment())
                        4 -> insertFragment(ReferFragment())
                        5 -> Log.d("main_activity", "log out")
                    }
                    return false
                }
            })
            .build()
    }

//    private fun insertFragment(Ffragment: Fragment) {
//        // Create a new fragment and specify the fragment to show based on nav item clicked
//        var fragment: Fragment? = null
////        fragment = PointsFragment()
//        fragment = Ffragment
    private fun insertFragment(fragment: Fragment) {
        // Insert the fragment by replacing any existing fragment
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()
    }

    /* Creates the Account Header */
    private fun createAccountHeader() = AccountHeaderBuilder()
        .withActivity(this)
        .withHeaderBackground(R.drawable.nav_header)
        .addProfiles(ProfileDrawerItem().withName("Ankur Goel")
            .withEmail("ankurgoel@gmail.com").withIcon(resources.getDrawable(R.drawable.ic_profile))
        )
        .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
            override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                return false
            }
        })
        .withTextColor(Color.WHITE)
        .build()
}
