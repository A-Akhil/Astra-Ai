package com.example.aisecretary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Represents a single memory stored by the AI.
 *
 * This includes a key-value pair with a timestamp to track when it was added or updated.
 *
 * @property id Unique ID for each memory fact (auto-generated).
 * @property key The name or identifier of the memory.
 * @property value The actual content or detail of the memory.
 * @property timestamp The time the memory was saved (in milliseconds).
 */
@Entity(
    tableName = "memory_facts",
    // No unique constraints - only the primary key (id) should be unique
    indices = [Index("key")] // Index on key for faster searching but not unique
)
data class MemoryFact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)