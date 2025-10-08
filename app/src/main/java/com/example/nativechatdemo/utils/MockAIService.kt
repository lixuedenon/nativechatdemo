// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/MockAIService.kt
package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.AIResponse
import com.example.nativechatdemo.data.model.Message
import kotlin.math.abs

object MockAIService {

    /**
     * 生成AI回复（匹配你原来的调用方式）
     */
    fun generateResponse(
        userInput: String,
        characterId: String,
        currentRound: Int,
        conversationHistory: List<Message>,
        currentFavorability: Int
    ): AIResponse {
        // 随机生成好感度变化
        val favorChange = (-3..8).random()
        val newFavorability = (currentFavorability + favorChange).coerceIn(0, 100)

        // 智能触发逻辑
        val isPeak = shouldTriggerPeak(currentFavorability, newFavorability, favorChange)

        // 生成原因文本
        val reason = if (isPeak) {
            getReasonForChange(favorChange, newFavorability, currentFavorability)
        } else {
            ""
        }

        // 生成回复内容
        val responseContent = getResponseContent(characterId, currentRound, favorChange)

        // 构建带标记的消息
        val tag = if (isPeak) "FAVOR_PEAK" else "FAVOR"
        val sign = if (favorChange >= 0) "+" else ""
        val message = "$responseContent [$tag:$sign$favorChange:$reason]"

        return AIResponse(
            message = message,
            favorabilityChange = favorChange,
            responseTime = System.currentTimeMillis()
        )
    }

    /**
     * 判断是否应该触发突破点（红心或裂心）
     */
    private fun shouldTriggerPeak(
        oldFavor: Int,
        newFavor: Int,
        change: Int
    ): Boolean {
        // 条件1：跨越关键阈值（20、40、60、80）
        val thresholds = listOf(20, 40, 60, 80)
        for (threshold in thresholds) {
            if (oldFavor < threshold && newFavor >= threshold) {
                return true
            }
            if (oldFavor >= threshold && newFavor < threshold) {
                return true
            }
        }

        // 条件2：大幅变化（绝对值≥5）
        if (abs(change) >= 5) {
            return true
        }

        // 条件3：其他情况10%随机
        return (1..10).random() == 1
    }

    /**
     * 根据好感度变化生成原因文本
     */
    private fun getReasonForChange(change: Int, newFavor: Int, oldFavor: Int): String {
        return when {
            oldFavor < 80 && newFavor >= 80 -> listOf(
                "你的真诚打动了我",
                "这是我最开心的时刻",
                "你真的很懂我"
            ).random()

            oldFavor < 60 && newFavor >= 60 -> listOf(
                "聊天越来越有趣了",
                "你让我感到很舒服",
                "我开始期待和你聊天"
            ).random()

            oldFavor < 40 && newFavor >= 40 -> listOf(
                "我们的互动变好了",
                "你开始了解我了",
                "聊天氛围不错"
            ).random()

            oldFavor < 20 && newFavor >= 20 -> listOf(
                "总算有点进展",
                "你的表现还可以",
                "开始有点意思了"
            ).random()

            oldFavor >= 80 && newFavor < 80 -> listOf(
                "你让我失望了",
                "刚才的话不太好",
                "我有点不开心"
            ).random()

            oldFavor >= 60 && newFavor < 60 -> listOf(
                "气氛变差了",
                "你怎么这样说话",
                "我不太满意"
            ).random()

            change >= 5 -> listOf(
                "你真会说话！",
                "这句话说到我心里了",
                "你的幽默感很棒",
                "这个话题我喜欢"
            ).random()

            change <= -5 -> listOf(
                "这话有点伤人",
                "我不太喜欢这个话题",
                "你的态度让我不舒服",
                "这样说话不太好"
            ).random()

            change > 0 -> listOf(
                "你的回应不错",
                "聊得还挺开心",
                "继续保持"
            ).random()

            else -> listOf(
                "感觉一般般",
                "没什么特别的",
                "就这样吧"
            ).random()
        }
    }

    /**
     * 生成回复内容
     */
    private fun getResponseContent(characterId: String, round: Int, favorChange: Int): String {
        val responses = when {
            characterId.contains("gentle") -> listOf(
                "嗯，我明白你的意思",
                "你说得对呢",
                "真的吗？好开心~",
                "我也这么觉得",
                "谢谢你愿意和我聊天"
            )
            characterId.contains("lively") -> listOf(
                "哈哈，太有趣了！",
                "哇！真的假的？",
                "这个我超喜欢！",
                "你好好笑哦~",
                "再说点别的吧！"
            )
            characterId.contains("elegant") -> listOf(
                "确实如此。",
                "你的见解很独特。",
                "这个观点很有深度。",
                "我欣赏你的想法。",
                "继续说下去。"
            )
            characterId.contains("sunny") -> listOf(
                "超棒的！",
                "你好厉害！",
                "我好开心~",
                "太好了！",
                "我喜欢这样！"
            )
            else -> listOf(
                "嗯。",
                "好的。",
                "我知道了。",
                "这样啊。",
                "继续吧。"
            )
        }

        return responses.random()
    }
}