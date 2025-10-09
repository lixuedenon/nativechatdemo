// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/ConversationAnalysis.kt
// 文件类型：Kotlin Data Class

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 对话分析数据模型
 * 用于存储复盘页面的逐条分析结果
 */
@Entity(tableName = "conversation_analysis")
data class ConversationAnalysis(
    @PrimaryKey val id: String,
    val conversationId: String,     // 关联的对话ID
    val round: Int,                 // 第几轮（有效对话的轮次）
    val userMessageId: String,      // 用户消息ID
    val aiMessageId: String,        // AI消息ID
    val userMessage: String,        // 用户说了什么
    val aiMessage: String,          // AI说了什么
    val analysis: String,           // AI的分析
    val suggestion: String,         // AI的建议
    val createdAt: Long             // 创建时间
)