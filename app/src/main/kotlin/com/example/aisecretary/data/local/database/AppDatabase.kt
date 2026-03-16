package com.example.aisecretary.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.local.database.dao.MemoryFactDao
import com.example.aisecretary.data.model.Message
import com.example.aisecretary.data.model.MemoryFact
import android.content.Context

/**
 * The main Room database class for the AI Secretary app.
 *
 * This database holds the [Message] and [MemoryFact] entities and provides access
 * to their respective DAO interfaces: [MessageDao] and [MemoryFactDao].
 */
@Database(entities = [Message::class, MemoryFact::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to MessageDao for database operations on the messages table.
     */
    abstract fun messageDao(): MessageDao

    /**
     * Provides access to [MemoryFactDao] for database operations on the memory_facts table.
     */
    abstract fun memoryFactDao(): MemoryFactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Defines a migration strategy from version 1 to version 2 of the database schema.
         *
         * - Backs up existing data from `memory_facts`.
         * - Drops and recreates the `memory_facts` table with an index on `key`.
         * - Restores the backed-up data.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate memory_facts table with the index
                // First, create a backup of existing data
                database.execSQL("CREATE TABLE IF NOT EXISTS `memory_facts_backup` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO `memory_facts_backup` SELECT * FROM `memory_facts`")
                
                // Drop the old table
                database.execSQL("DROP TABLE IF EXISTS `memory_facts`")
                
                // Create the new table with the index
                database.execSQL("CREATE TABLE IF NOT EXISTS `memory_facts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_memory_facts_key` ON `memory_facts` (`key`)")
                
                // Restore the data
                database.execSQL("INSERT INTO `memory_facts` SELECT * FROM `memory_facts_backup`")
                
                // Drop the backup table
                database.execSQL("DROP TABLE IF EXISTS `memory_facts_backup`")
            }
        }

        /**
         * Returns the singleton instance of [AppDatabase].
         *
         * @param context The application context.
         * @return The initialized [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}