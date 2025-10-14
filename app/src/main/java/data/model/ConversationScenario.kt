// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/ConversationScenario.kt
// 文件类型：Kotlin Data Class

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 完整对话场景（学习模式和练习模式通用）
 */
@Entity(tableName = "conversation_scenarios")
data class ConversationScenario(
    @PrimaryKey val id: String,
    val title: String,                  // "初次见面" "咖啡厅约会"
    val targetGender: String,           // "male" 或 "female"
    val category: String,               // "dating" "work" "friend"
    val difficulty: Int,                // 1-5
    val dialogueJson: String,           // 完整对话JSON（10-20轮）
    val keyPointsJson: String,          // 关键点JSON（2-3个）
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 对话的一轮（两句话：对方说+我方回）
 */
data class DialogueTurn(
    val index: Int,                     // 第几轮（1-20）
    val partnerSays: String,            // 对方说的话
    val mySays: String,                 // 我方的正确回答
    val isKeyPoint: Boolean = false     // 是否是关键点
)

/**
 * 关键点（需要用户注意的地方）
 */
data class KeyPoint(
    val atTurnIndex: Int,               // 在第几轮出现
    val warning: String,                // 提示："⚠️ 注意！对方在试探..."
    val correctResponse: String,        // 正确回答
    val wrongOptions: List<String>,     // 3个错误选项（练习模式用）
    val analysis: String,               // 为什么这样回答
    val optionAnalysis: List<OptionAnalysis>  // 每个选项的详细分析（练习模式用）
)

/**
 * 选项分析（练习模式用）
 */
data class OptionAnalysis(
    val optionText: String,
    val analysis: String,
    val score: Int                      // 得分：-10 到 30
)