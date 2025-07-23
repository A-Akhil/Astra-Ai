package com.example.aisecretary.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.aisecretary.data.model.Message

@Dao
interface MessageDaoExtensions {
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): Message?
    
    @Update
    suspend fun updateMessage(message: Message)
}
