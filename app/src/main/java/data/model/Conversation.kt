// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/Conversation.kt
// 文件类型：Kotlin Data Class
// 修改内容：增加 originalConversationId 字段

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val userId: String,
    val characterId: String,
    val characterName: String,
    val currentFavorability: Int,
    val actualRounds: Int,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,

    // 新增字段
    val moduleType: String = "basic",           // 模块类型: basic/girlfriend/radar_learn/radar_practice/custom
    val sceneType: String? = null,              // 场景类型: wechat/qq/cafe等
    val favorPoints: String? = null,            // 好感线数据JSON数组
    val reviewMode: String? = null,             // 复盘模式: same/similar/natural
    val originalConversationId: String? = null, // 🔥 新增：原对话ID（用于复盘练习）
    val lastReviewRound: Int = 0,               // 上次复盘的轮数
    val customTraits: String? = null,           // 定制特质JSON
    val memoryJson: String? = null,             // 女友养成记忆摘要JSON
    val progressPercent: Int = 0,               // 进度百分比0-100
    val totalTokens: Int = 0                    // 总Token数
)