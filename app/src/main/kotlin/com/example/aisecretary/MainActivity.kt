package com.example.aisecretary

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController

/**
 * This is the main screen (activity) of the app.
 *
 * It shows the app layout and handles screen-to-screen navigation.
 * It also sets up the top app bar (toolbar).
 */
class MainActivity : AppCompatActivity() {

    /** Helps the app switch between screens. */
    private lateinit var navController: NavController

    /** Used to set up the app's top bar and back button behavior. */
    private lateinit var appBarConfiguration: AppBarConfiguration

    /**
     * Called when the app first starts this screen.
     * Sets up the layout, toolbar, and navigation.
     *
     * @param savedInstanceState Data about the previous state of the app (if any).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Set the top app bar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set up navigation between screens
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Define which fragments are top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.chatFragment)
        )

        // Link the action bar with the navigation controller
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Creates the top menu in the toolbar using the menu resource file.
     *
     * @param menu The menu that will be shown in the toolbar.
     * @return true if the menu was created successfully.
     */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Handles clicks on menu items in the toolbar.
     *
     * @param item The selected menu item.
     * @return true if the item was handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                navController.navigate(R.id.action_chatFragment_to_settingsFragment)
                true
            }
            R.id.action_memory -> {
                navController.navigate(R.id.action_chatFragment_to_memoryFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handles the Up button (←) action in the toolbar for navigation.
     *
     * @return true if navigation up was successful.
     */
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}