// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/RadarProgress.kt
// 文件类型：Kotlin Data Class

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 社交雷达用户进度数据模型
 * 记录用户在练习模式中的表现
 */
@Entity(tableName = "radar_progress")
data class RadarProgress(
    @PrimaryKey val id: String,
    val userId: String,
    val scenarioId: String,
    val mode: String,                   // "learn" 或 "practice"
    val selectedOption: Int?,           // 用户选择的选项（1-4，practice模式）
    val isCorrect: Boolean?,            // 是否正确（practice模式）
    val score: Int,                     // 得分
    val completedAt: Long = System.currentTimeMillis()
)