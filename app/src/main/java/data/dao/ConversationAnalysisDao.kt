// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/ConversationAnalysisDao.kt
// 文件类型：Kotlin Interface (Room DAO)

package com.example.nativechatdemo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.nativechatdemo.data.model.ConversationAnalysis

@Dao
interface ConversationAnalysisDao {

    /**
     * 批量插入分析数据
     */
    @Insert
    suspend fun insertAll(analyses: List<ConversationAnalysis>)

    /**
     * 根据对话ID查询所有分析数据
     */
    @Query("SELECT * FROM conversation_analysis WHERE conversationId = :conversationId ORDER BY round ASC")
    suspend fun getAnalysisByConversationId(conversationId: String): List<ConversationAnalysis>

    /**
     * 删除指定对话的所有分析数据
     */
    @Query("DELETE FROM conversation_analysis WHERE conversationId = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)
}