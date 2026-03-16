package com.example.aisecretary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

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
    val timestamp: Long = System.currentTimeMillis(),
    val isReminder: Boolean = false,
    val reminderTime: Long? = null,
    val isCompleted: Boolean = false,
    val reminderType: String? = null
)
