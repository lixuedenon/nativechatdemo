// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/RadarScenario.kt
// 文件类型：Kotlin Data Class

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 社交雷达场景数据模型
 * 用于学习模式和练习模式的场景数据
 */
@Entity(tableName = "radar_scenarios")
data class RadarScenario(
    @PrimaryKey val id: String,
    val type: String,                   // "learn" 或 "practice"
    val category: String,               // 场景分类：dating/work/friend
    val difficulty: Int,                // 难度等级：1-5
    val targetGender: String,           // 目标性别："male"或"female"（对方的性别）
    val contextDescription: String,     // 场景描述
    val partnerMessage: String,         // 对方说的话
    val correctResponse: String,        // 正确回复（practice模式用）
    val wrongResponses: String,         // 错误回复JSON数组（practice模式用）
    val radarPoints: String,            // 雷区点JSON数组（learn模式用）
    val analysis: String,               // 分析文本
    val createdAt: Long = System.currentTimeMillis()
)