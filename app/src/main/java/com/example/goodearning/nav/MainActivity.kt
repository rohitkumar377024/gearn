package com.example.goodearning.nav

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.goodearning.R
import com.example.goodearning.auth.LoginActivity
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

        /* Sets Toolbar as the SupportActionBar for this Activity */
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
                PrimaryDrawerItem().withName("Answer and Earn").withIdentifier(13),
                PrimaryDrawerItem().withName("Skipped Questions"),
                PrimaryDrawerItem().withName("Points Summary"),
                PrimaryDrawerItem().withName("Refer"),
                PrimaryDrawerItem().withName("Logout")
            )
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                    when (position) {
                        1 -> insertFragment(AnswerAndEarnFragment(), "Answer and Earn")
                        2 -> insertFragment(SkippedQuestionsFragment(), "Skipped Questions")
                        3 -> insertFragment(PointsFragment(), "Points Summary")
                        4 -> insertFragment(ReferFragment(), "Refer")
                        5 -> logout()
                    }
                    return false
                }
            })
            .build()

        /* Initially Loading Up Answer and Earn Fragment */
        result.setSelection(13)
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

    /* Insert Fragment by Replacing Any Existing Fragment (if any) */
    private fun insertFragment(fragment: Fragment, titleToBeSet: String){
        supportFragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()
        /* Sets the Title of the Toolbar as per the New Fragment Transaction */
        title = titleToBeSet
    }

    /* Logging Out */
    private fun logout() {
        Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
