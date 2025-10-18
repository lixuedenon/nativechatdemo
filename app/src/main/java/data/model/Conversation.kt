// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/Conversation.kt
// 文件类型：Kotlin Data Class (Room Entity)
// 修改内容：新增养成模式相关字段

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

    // 基础字段
    val moduleType: String = "basic",           // 模块类型: basic/training/radar_learn/radar_practice
    val sceneType: String? = null,              // 场景类型: wechat/qq/cafe等
    val favorPoints: String? = null,            // 好感线数据JSON数组
    val reviewMode: String? = null,             // 复盘模式: same/similar/natural
    val originalConversationId: String? = null, // 原对话ID（用于复盘练习）
    val lastReviewRound: Int = 0,               // 上次复盘的轮数
    val customTraits: String? = null,           // 定制特质JSON
    val memoryJson: String? = null,             // 女友养成记忆摘要JSON
    val progressPercent: Int = 0,               // 进度百分比0-100
    val totalTokens: Int = 0,                   // 总Token数

    // 养成模式专用字段
    val isTrainingMode: Boolean = false,        // 是否养成模式
    val trainingEndingType: String? = null,     // 结束类型："sick"生病 | "timetravel"穿越
    val reviveCount: Int = 0,                   // 续命次数 0-3
    val totalTrainingRounds: Int = 0            // 养成模式总轮数（用于判断结束）
)