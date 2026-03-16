package com.example.aisecretary.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.aisecretary.data.model.MemoryFact
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for interacting with the MemoryFact table in the Room database.
 */
@Dao
interface MemoryFactDao {

    /**
     * Inserts a MemoryFact into the database.
     * If a conflict occurs (e.g., duplicate ID), the existing entry is replaced.
     *
     * @param memoryFact The memory fact to be inserted.
     * @return The ID of the inserted row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryFact(memoryFact: MemoryFact): Long

    /**
     * Updates an existing MemoryFact in the database.
     *
     * @param memoryFact The memory fact with updated values.
     */
    @Update
    suspend fun updateMemoryFact(memoryFact: MemoryFact)

    /**
    * Retrieves all MemoryFacts from the database, sorted by the latest timestamp first.
    *
    * @return A [Flow] emitting the list of memory facts.
    */
    @Query("SELECT * FROM memory_facts ORDER BY timestamp DESC")
    fun getAllMemoryFacts(): Flow<List<MemoryFact>>

    /**
     * Searches memory facts where either the key or value contains the given query string.
     *
     * @param query The keyword or phrase to search within key/value fields.
     * @return A [Flow] emitting the list of matching memory facts.
     */
    @Query("SELECT * FROM memory_facts WHERE key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'")
    fun searchMemoryFacts(query: String): Flow<List<MemoryFact>>

    /**
     * Deletes a single MemoryFact from the database by its ID.
     *
     * @param id The ID of the memory fact to delete.
     */
    @Query("DELETE FROM memory_facts WHERE id = :id")
    suspend fun deleteMemoryFact(id: Long)

    /**
     * Deletes all memory facts from the database.
     */
    @Query("DELETE FROM memory_facts")
    suspend fun deleteAllMemoryFacts()
}