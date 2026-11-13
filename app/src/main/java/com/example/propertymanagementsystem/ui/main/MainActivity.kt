package com.example.propertymanagementsystem.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.databinding.ActivityMainBinding
import com.example.propertymanagementsystem.ui.auth.LoginActivity
import com.example.propertymanagementsystem.util.SessionManager
import com.google.android.material.navigation.NavigationView

/**
 * The main screen of the app.
 *
 * It holds the drawer, the toolbar and the navigation host. Every other
 * screen of the app is a fragment inside the navigation host, so the drawer
 * and the toolbar stay in one place and the back button works normally.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        drawerLayout = binding.drawerLayout

        setSupportActionBar(binding.toolbar)

        // The navigation host is the place where the fragments are shown.
        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHost.navController

        // The screens that are in the drawer are the top level screens. The
        // toolbar shows a back arrow on any screen that is not one of them.
        // The drawer layout is given to the AppBarConfiguration so the
        // button that opens the drawer shows on the toolbar.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dashboardFragment, R.id.aboutFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = binding.navView
        navView.setupWithNavController(navController)

        // The drawer is closed once a screen has been opened.
        navController.addOnDestinationChangedListener { _, _, _ ->
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Show the email of the signed in person in the drawer header.
        binding.navView.getHeaderView(0).findViewById<android.widget.TextView>(R.id.headerEmail)
            .text = session.signedInEmail()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // The log out option is in the toolbar menu.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            session.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Handles the back arrow on the toolbar.
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
