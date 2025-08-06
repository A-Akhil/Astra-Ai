package com.example.aisecretary

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.room.Room
import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.data.local.database.AppDatabase
import com.example.aisecretary.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * This is the main Application class of the app.
 *
 * It runs once when the app starts and is used to:
 * - Create one shared database for the whole app
 * - Create one shared LlamaClient to use the AI model
 * - Track if the app is in the foreground or background
 * - Unload the AI model when the app is closing
 */

class SecretaryApplication : Application() {

    /**
     * A special background task scope for running work outside the UI.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Creates and gives access to the Room database.
     * It only creates one copy and uses it everywhere.
     */
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // This will wipe data if migration fails, but prevents crashes
        .build()
    }

    /**
     * Creates and gives access to the LlamaClient which talks to the AI model.
     */
    val llamaClient: LlamaClient by lazy {
        LlamaClient(AppModule.provideRetrofit())
    }

    /**
     * Keeps track of how many activities (screens) are open.
     */
    private var runningActivities = 0
    
    override fun onCreate() {
        super.onCreate()

        // This checks when an activity (screen) starts, stops, or ends
        // and helps us know when the app goes to background or is closing
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            
            override fun onActivityStarted(activity: Activity) {
                runningActivities++
            }
            
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            
            override fun onActivityStopped(activity: Activity) {
                runningActivities--
                if (runningActivities <= 0) {
                    // App is in background (no screen visible)
                    // We could unload the model later if needed
                }
            }
            
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            
            override fun onActivityDestroyed(activity: Activity) {
                if (activity.isFinishing && runningActivities <= 1) {
                    // This is likely the last activity and it's explicitly finishing (not due to config change)
                    unloadModel()
                }
            }
        })
    }
    /**
     * This removes the AI model from memory in the background
     * to free up space when the app is closing.
     */
    private fun unloadModel() {
        applicationScope.launch {
            llamaClient.unloadModel()
        }
    }
}