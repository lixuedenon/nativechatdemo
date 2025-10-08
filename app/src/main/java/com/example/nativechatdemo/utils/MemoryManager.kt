package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.Message
import org.json.JSONObject

object MemoryManager {

    /**
     * 压缩历史对话为摘要
     * 实际使用时需要调用AI API，这里先用模拟实现
     */
    suspend fun compressHistory(
        messages: List<Message>,
        currentFavor: Int,
        characterName: String
    ): String {
        // TODO: 实际实现时调用AI API生成摘要
        // 示例prompt: "请总结以下对话的关键信息，包括：聊过的话题、建立的情感、重要事件等"

        // 模拟生成摘要
        val topics = extractTopics(messages)
        val duration = calculateDuration(messages)

        return JSONObject().apply {
            put("duration", "$duration 天")
            put("topics", topics.joinToString("、"))
            put("favor", currentFavor)
            put("summary", "你们已经聊了${duration}天，主要聊过${topics.joinToString("、")}等话题，${characterName}对你的好感度是${currentFavor}%")
        }.toString()
    }

    /**
     * 提取对话主题（简化版）
     */
    private fun extractTopics(messages: List<Message>): List<String> {
        val keywords = mutableSetOf<String>()
        val topicKeywords = mapOf(
            "音乐" to listOf("音乐", "歌", "唱", "钢琴", "吉他"),
            "运动" to listOf("运动", "跑步", "健身", "篮球", "足球"),
            "美食" to listOf("吃", "美食", "做饭", "烹饪", "餐厅"),
            "电影" to listOf("电影", "看", "影院", "演员"),
            "旅行" to listOf("旅行", "旅游", "去", "风景")
        )

        messages.forEach { message ->
            topicKeywords.forEach { (topic, keys) ->
                if (keys.any { message.content.contains(it) }) {
                    keywords.add(topic)
                }
            }
        }

        return keywords.toList()
    }

    /**
     * 计算对话持续天数
     */
    private fun calculateDuration(messages: List<Message>): Int {
        if (messages.isEmpty()) return 0

        val firstTime = messages.first().timestamp
        val lastTime = messages.last().timestamp
        val dayInMillis = 24 * 60 * 60 * 1000

        return ((lastTime - firstTime) / dayInMillis).toInt() + 1
    }

    /**
     * 检查是否需要压缩
     */
    fun shouldCompress(roundCount: Int, compressInterval: Int = 20): Boolean {
        return roundCount > 0 && roundCount % compressInterval == 0
    }

    /**
     * 解析记忆摘要JSON
     */
    fun parseMemory(memoryJson: String?): MemorySummary? {
        if (memoryJson.isNullOrEmpty()) return null

        return try {
            val json = JSONObject(memoryJson)
            MemorySummary(
                duration = json.optString("duration", ""),
                topics = json.optString("topics", ""),
                favor = json.optInt("favor", 0),
                summary = json.optString("summary", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    data class MemorySummary(
        val duration: String,
        val topics: String,
        val favor: Int,
        val summary: String
    )
}