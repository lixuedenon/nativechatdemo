// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/MessageDao.kt
// 文件类型：Kotlin Interface (Room DAO)

package com.example.nativechatdemo.data.dao

import androidx.room.*
import com.example.nativechatdemo.data.model.Message

@Dao
interface MessageDao {

    @Insert
    suspend fun insertMessage(message: Message)

    // 🔥 直接改名（如果其他地方也用了这个方法，需要一起改）
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesByConversationId(conversationId: String): List<Message>

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: String)
}