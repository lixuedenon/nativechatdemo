package com.example.nativechatdemo.data.dao

import androidx.room.*
import com.example.nativechatdemo.data.model.Conversation

@Dao
interface ConversationDao {
    @Insert
    suspend fun insertConversation(conversation: Conversation)

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): Conversation?

    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY updatedAt DESC")
    suspend fun getConversationsByUser(userId: String): List<Conversation>

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}