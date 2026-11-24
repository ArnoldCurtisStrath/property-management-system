package com.example.propertymanagementsystem.ui.main

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
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
import com.example.propertymanagementsystem.work.WorkScheduler

/**
 * The shell of Sahihi.
 *
 * The four top-level screens (Today, Buildings, Money, More) are reached
 * from the bottom bar. Everything else is a detail screen with a back arrow.
 * The drawer is gone: the agent's main actions are within one thumb.
 *
 * The paper runs edge to edge. The bar carries the gesture inset itself, so
 * there is no dead strip under the labels and no black system band.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var bottomInset = 0

    private val topLevelDestinations = setOf(
        R.id.dashboardFragment,
        R.id.buildingListFragment,
        R.id.paymentHistoryFragment,
        R.id.moreFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        session = SessionManager(this)
        if (!session.isSignedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        WorkScheduler.start(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, bars.top, 0, 0)
            bottomInset = bars.bottom
            binding.bottomNav.updatePadding(bottom = bottomInset)
            applyContentInset()
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHost.navController

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in topLevelDestinations
            binding.toolbar.visibility = if (isTopLevel) android.view.View.GONE
            else android.view.View.VISIBLE
            binding.bottomNav.visibility = if (isTopLevel) android.view.View.VISIBLE
            else android.view.View.GONE
            applyContentInset()
        }
    }

    /**
     * Detail screens have no bottom bar, so they take the gesture inset
     * themselves. Top-level screens stop above the bar.
     */
    private fun applyContentInset() {
        val padding = if (binding.bottomNav.visibility == android.view.View.VISIBLE) 0 else bottomInset
        binding.navHostFragment.updatePadding(bottom = padding)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 100
    }
}
