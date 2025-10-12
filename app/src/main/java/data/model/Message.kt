package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val characterCount: Int = 0,  // 🔥 改为默认值0（或者在创建时传入content.length）

    // 新增字段
    val favorChange: Int? = null,               // 好感度变化值
    val analysisText: String? = null,           // 复盘分析文本
    val isEffective: Boolean = true,            // 是否为有效对话
    val radarTips: String? = null,              // 社交雷达提示JSON
    val selectedOption: Int? = null             // 练习模式选择的选项（1-4）
)