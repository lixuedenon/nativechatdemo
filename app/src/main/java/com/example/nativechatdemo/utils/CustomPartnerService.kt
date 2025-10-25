// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/CustomPartnerService.kt

package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.AIResponse
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.Message
import kotlin.random.Random

object CustomPartnerService {

    /**
     * 根据特质生成AI回复
     */
    fun generateCustomResponse(
        userInput: String,
        traits: List<String>,
        currentRound: Int,
        currentFavorability: Int,
        conversationHistory: List<Message>
    ): AIResponse {

        val sentiment = analyzeUserSentiment(userInput)
        val responseStyle = determineResponseStyle(traits)
        var response = generateBaseResponse(userInput, responseStyle, currentRound)
        response = adjustResponseByTraits(response, traits, sentiment)
        val favorChange = calculateFavorChange(traits, sentiment, userInput, currentFavorability)
        val taggedResponse = addFavorTag(response, favorChange)

        return AIResponse(
            message = taggedResponse,
            favorabilityChange = favorChange,
            responseTime = System.currentTimeMillis()
        )
    }

    /**
     * 分析用户输入的情感倾向
     */
    private fun analyzeUserSentiment(input: String): String {
        return when {
            input.contains("喜欢") || input.contains("爱") || input.contains("想你") -> "positive"
            input.contains("讨厌") || input.contains("烦") || input.contains("算了") -> "negative"
            input.contains("吗") || input.contains("呢") || input.contains("什么") -> "question"
            else -> "neutral"
        }
    }

    /**
     * 根据特质决定回复风格
     */
    private fun determineResponseStyle(traits: List<String>): String {
        return when {
            traits.contains("温柔") -> "gentle"
            traits.contains("活泼") || traits.contains("外向") -> "lively"
            traits.contains("高冷") || traits.contains("内向") -> "cool"
            traits.contains("幽默") -> "humorous"
            else -> "normal"
        }
    }

    /**
     * 生成基础回复
     */
    private fun generateBaseResponse(input: String, style: String, round: Int): String {
        val depth = when {
            round < 5 -> "shallow"
            round < 15 -> "medium"
            else -> "deep"
        }

        return when {
            input.contains("做什么") -> {
                when (style) {
                    "gentle" -> "我在想你呢~你在做什么？"
                    "lively" -> "哈哈，在发呆！你呢？"
                    "cool" -> "看书。"
                    else -> "没什么特别的，你呢？"
                }
            }
            input.contains("喜欢") -> {
                when (style) {
                    "gentle" -> "我也喜欢呢~"
                    "lively" -> "真的吗！我也是！"
                    "cool" -> "嗯，还不错。"
                    else -> "是吗，挺好的。"
                }
            }
            else -> {
                when (style) {
                    "gentle" -> "嗯嗯，我明白的~"
                    "lively" -> "哈哈，是这样啊！"
                    "cool" -> "哦。"
                    else -> "嗯，我知道了。"
                }
            }
        }
    }

    /**
     * 根据特质调整回复
     */
    private fun adjustResponseByTraits(response: String, traits: List<String>, sentiment: String): String {
        var adjusted = response

        if (traits.contains("敏感") && sentiment == "negative") {
            adjusted = "你是不是对我有什么不满...？"
        }

        if (traits.contains("嫉妒心强") && response.contains("朋友")) {
            adjusted += "（语气有些不悦）是男生还是女生啊？"
        }

        if (traits.contains("缺乏安全感") && sentiment == "positive") {
            adjusted += "你真的这么想吗？不是骗我的吧？"
        }

        if (traits.contains("浪漫") && sentiment == "positive") {
            adjusted = "听到你这么说，我心里甜甜的~"
        }

        return adjusted
    }

    /**
     * 计算好感度变化
     */
    private fun calculateFavorChange(
        traits: List<String>,
        sentiment: String,
        input: String,
        currentFavor: Int
    ): Int {
        var change = 0

        change = when (sentiment) {
            "positive" -> Random.nextInt(3, 8)
            "negative" -> Random.nextInt(-5, -1)
            "question" -> Random.nextInt(1, 4)
            else -> Random.nextInt(-1, 3)
        }

        if (traits.contains("敏感") && sentiment == "negative") {
            change -= 3
        }

        if (traits.contains("包容") && sentiment == "negative") {
            change += 2
        }

        if (input.contains("一起") || input.contains("约会")) {
            change += 2
        }

        if (currentFavor < 20 && change < 0) {
            change -= 2
        }

        if (currentFavor > 80 && change > 0) {
            change = (change * 0.7).toInt()
        }

        return change.coerceIn(-10, 10)
    }

    /**
     * 添加好感度标记
     */
    private fun addFavorTag(response: String, favorChange: Int): String {
        if (favorChange == 0) return response

        val reason = when {
            favorChange >= 5 -> "甜蜜互动"
            favorChange >= 3 -> "愉快交流"
            favorChange >= 1 -> "正常对话"
            favorChange >= -2 -> "略显尴尬"
            favorChange >= -5 -> "气氛不佳"
            else -> "严重冲突"
        }

        return "$response [FAVOR:${if (favorChange > 0) "+" else ""}$favorChange:$reason]"
    }

    /**
     * 检查是否触发特殊事件（如生气、分手等）
     */
    fun checkSpecialEvent(
        traits: List<String>,
        currentFavor: Int,
        conversationHistory: List<Message>
    ): String? {
        if (currentFavor <= 10) {
            return "breakup"
        }

        val recentMessages = conversationHistory.takeLast(6)
        // 通过解析消息内容判断是否为负面消息
        val negativeMsgCount = recentMessages.count { msg ->
            msg.content.contains("[FAVOR:-") || msg.content.contains("FAVOR_PEAK:-")
        }

        if (negativeMsgCount >= 3) {
            if (traits.contains("敏感")) {
                return "angry"
            }
        }

        return null
    }

    /**
     * 生成特殊事件回复
     */
    fun generateSpecialEventResponse(event: String, traits: List<String>): String {
        return when (event) {
            "breakup" -> {
                if (traits.contains("温柔")) {
                    "对不起...我觉得我们不太合适...祝你找到更好的人..."
                } else if (traits.contains("直率")) {
                    "我们分手吧，这样下去没意思。"
                } else {
                    "我想...我们还是做朋友比较好..."
                }
            }
            "angry" -> {
                if (traits.contains("敏感")) {
                    "你到底有没有在乎过我的感受？我真的很难过..."
                } else if (traits.contains("脾气暴躁")) {
                    "够了！你能不能认真一点！"
                } else {
                    "我需要冷静一下...先不聊了。"
                }
            }
            else -> "......"
        }
    }

    /**
     * 生成告白预测
     */
    fun predictConfessionSuccess(
        userId: String,
        traits: List<String>,
        conversations: List<Conversation>,
        testType: Int
    ): ConfessionPrediction {
        var baseRate = 50f

        when (testType) {
            1 -> {
                val avgFavor = conversations.firstOrNull()?.currentFavorability ?: 50
                baseRate = avgFavor.toFloat()
                if (avgFavor > 70) baseRate += 10
            }
            2 -> {
                baseRate = 40f
                val attempts = conversations.size
                baseRate += attempts * 5
            }
            3 -> {
                baseRate = 30f
            }
        }

        if (traits.contains("浪漫")) baseRate += 10
        if (traits.contains("保守")) baseRate -= 10
        if (traits.contains("独立")) baseRate -= 5
        if (traits.contains("粘人")) baseRate += 5

        val suggestions = mutableListOf<String>()

        if (baseRate < 40) {
            suggestions.add("建议继续培养感情，不要急于表白")
            suggestions.add("多了解对方的兴趣爱好")
            suggestions.add("创造更多美好回忆")
        } else if (baseRate < 70) {
            suggestions.add("可以试探性地表达好感")
            suggestions.add("观察对方的反应")
            suggestions.add("选择合适的时机和场合")
        } else {
            suggestions.add("成功率很高，可以大胆表白")
            suggestions.add("准备一个浪漫的表白方式")
            suggestions.add("选择对方心情好的时候")
        }

        return ConfessionPrediction(
            successRate = baseRate.coerceIn(5f, 95f),
            analysis = CustomTraitConfig.getConfessionAnalysis(baseRate, testType),
            suggestions = suggestions
        )
    }

    data class ConfessionPrediction(
        val successRate: Float,
        val analysis: String,
        val suggestions: List<String>
    )
}