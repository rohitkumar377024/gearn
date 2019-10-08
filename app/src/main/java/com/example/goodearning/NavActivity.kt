package com.example.goodearning

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import kotlinx.android.synthetic.main.activity_nav.*

class NavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)

        nav_holder.setOnClickListener { startActivity(Intent(this, QuestionActivity::class.java)) }

       setupSideNavigation()
    }

    private fun setupSideNavigation() {
        // Create the AccountHeader
        val headerResult = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.drawable.nav_header)
            .addProfiles(
                ProfileDrawerItem().withName("Ankur Goel").withEmail("ankurgoel@gmail.com").withIcon(getResources().getDrawable(
                    R.drawable.ic_profile
                ))
            )
            .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                    return false
                }
            })
            .withTextColor(Color.WHITE)
            .build()

        val result = DrawerBuilder().withActivity(this)
            .withAccountHeader(headerResult)
            .addDrawerItems(
                PrimaryDrawerItem().withName("Answer and Earn"),
                PrimaryDrawerItem().withName("Skipped Questions"),
                PrimaryDrawerItem().withName("Points Summary"),
                PrimaryDrawerItem().withName("Refer"),
                PrimaryDrawerItem().withName("Logout")
//                DividerDrawerItem()
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    // do something with the clicked item :D
                    return false
                }
            })
            .build()
    }

    /* cut nails */

}
