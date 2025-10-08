package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.Message

object TokenCounter {

    /**
     * 估算文本的Token数
     * 简化算法：1个中文字≈1.5 token，1个英文词≈1 token
     */
    fun estimate(text: String): Int {
        if (text.isEmpty()) return 0

        // 统计中文字符数
        val chineseCount = text.count { it.code in 0x4E00..0x9FA5 }

        // 统计英文单词数（简单按空格分割）
        val englishWords = text.replace("""[^\w\s]""".toRegex(), "")
            .split("""\s+""".toRegex())
            .filter { it.isNotEmpty() && it[0].code < 128 }
            .size

        return (chineseCount * 1.5 + englishWords).toInt()
    }

    /**
     * 估算对话历史的总Token数
     */
    fun estimateConversation(messages: List<Message>): Int {
        return messages.sumOf { estimate(it.content) }
    }

    /**
     * 检查是否需要压缩记忆
     * @param currentTokens 当前Token数
     * @param maxTokens 最大Token限制（默认3000）
     */
    fun shouldCompress(currentTokens: Int, maxTokens: Int = 3000): Boolean {
        return currentTokens > maxTokens
    }

    /**
     * 计算对话进度百分比
     * @param currentTokens 当前Token数
     * @param maxTokens 最大Token限制（默认5000）
     */
    fun calculateProgress(currentTokens: Int, maxTokens: Int = 5000): Int {
        return ((currentTokens.toFloat() / maxTokens) * 100).toInt().coerceIn(0, 100)
    }
}