// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/ConfessionTest.kt
// 文件类型：Kotlin Data Class (Room Entity)
// 用途：存储告白测试结果

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "confession_tests")
data class ConfessionTest(
    @PrimaryKey val id: String,
    val userId: String,
    val traitId: String?,                    // 关联的特质ID
    val conversationIds: String,             // JSON数组：相关对话ID列表
    val testType: Int,                       // 1=连续 2=重复 3=多样
    val successRate: Float,                  // 成功率 0-100
    val analysis: String,                    // 详细分析报告
    val suggestions: String,                 // 建议
    val createdAt: Long
)