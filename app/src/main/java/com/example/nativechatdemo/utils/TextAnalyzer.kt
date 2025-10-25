// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/TextAnalyzer.kt

package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.Message

/**
 * 文本分析工具类
 * 对应Flutter的TextAnalyzer
 */
object TextAnalyzer {

    /**
     * 计算文字密度系数
     * 根据字符数量返回不同的系数，用于计算有效轮数
     */
    fun calculateDensityCoefficient(characterCount: Int): Double {
        return when {
            characterCount <= 10 -> 0.5  // 很短的消息
            characterCount <= 25 -> 0.8  // 中短消息
            characterCount <= 40 -> 1.0  // 标准长度
            characterCount <= 50 -> 1.2  // 较长消息
            else -> 1.0  // 超长消息按标准计算
        }
    }

    /**
     * 计算有效轮数
     * 基于用户消息的密度系数计算实际对话效果
     */
    fun calculateEffectiveRounds(messages: List<Message>): Int {
        var totalDensity = 0.0

        messages.filter { it.sender == "user" }.forEach { message ->
            totalDensity += calculateDensityCoefficient(message.content.length)
        }

        return totalDensity.toInt()
    }

    /**
     * 分析用户消息质量
     * 返回消息的质量分数 (0-10)
     */
    fun analyzeMessageQuality(message: String): Double {
        var score = 5.0 // 基础分数
        val length = message.length

        // 长度评分
        when {
            length < 3 -> score -= 2.0
            length in 10..40 -> score += 1.0
            length > 50 -> score -= 1.0
        }

        // 问号加分（显示关心和兴趣）
        if (message.contains('?') || message.contains('？')) {
            score += 1.5
        }

        // 情感词汇分析
        val positiveWords = listOf("喜欢", "开心", "有趣", "不错", "很好", "棒", "厉害")
        val negativeWords = listOf("无聊", "烦", "算了", "随便", "不想", "没意思")

        if (positiveWords.any { message.contains(it) }) {
            score += 0.5
        }

        if (negativeWords.any { message.contains(it) }) {
            score -= 1.0
        }

        // 赞美词汇加分
        val compliments = listOf("漂亮", "好看", "聪明", "有趣", "可爱", "温柔", "优雅", "厉害")
        if (compliments.any { message.contains(it) }) {
            score += 1.0
        }

        // 个人分享加分
        if (message.contains("我") && (message.contains("喜欢") || message.contains("觉得"))) {
            score += 0.5
        }

        return score.coerceIn(0.0, 10.0)
    }
}