package com.example.aisecretary.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for performing operations on the [Message] table in the local Room database.
 */
@Dao
interface MessageDao {

    /**
     * Inserts a message into the database.
     *
     * @param message The message object to insert.
     * @return The ID of the inserted message row.
     */
    @Insert
    suspend fun insertMessage(message: Message): Long

    /**
     * Retrieves all messages from the database, ordered by their timestamp in ascending order.
     *
     * @return A Flow emitting the list of all messages.
     */
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<Message>>

    /**
     * Retrieves all messages marked as important (i.e., where `isImportantInfo` is true).
     *
     * @return A Flow emitting the list of important messages.
     */
    @Query("SELECT * FROM messages WHERE isImportantInfo = 1")
    fun getImportantInfoMessages(): Flow<List<Message>>

    /**
     * Deletes all messages from the database.
     */
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}