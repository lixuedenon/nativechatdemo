// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/MockAIService.kt
// 文件类型：Kotlin Object
// 修改内容：增加详细日志用于调试

package com.example.nativechatdemo.utils

import android.util.Log
import com.example.nativechatdemo.data.model.AIResponse
import com.example.nativechatdemo.data.model.Message
import kotlin.math.abs

object MockAIService {

    private const val TAG = "MockAIService"

    /**
     * 生成AI回复（保持原有功能）
     */
    fun generateResponse(
        userInput: String,
        characterId: String,
        currentRound: Int,
        conversationHistory: List<Message>,
        currentFavorability: Int
    ): AIResponse {
        val favorChange = (-3..8).random()
        val newFavorability = (currentFavorability + favorChange).coerceIn(0, 100)

        val isPeak = shouldTriggerPeak(currentFavorability, newFavorability, favorChange)

        val reason = if (isPeak) {
            getReasonForChange(favorChange, newFavorability, currentFavorability)
        } else {
            ""
        }

        val responseContent = getResponseContent(characterId, currentRound, favorChange)

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
     * 🔥 生成对话分析
     */
    fun generateAnalysis(
        messages: List<Message>,
        characterName: String,
        finalFavor: Int
    ): String {
        Log.d(TAG, "=== generateAnalysis 开始 ===")
        Log.d(TAG, "输入消息数: ${messages.size}")
        Log.d(TAG, "角色名称: $characterName")
        Log.d(TAG, "最终好感度: $finalFavor")

        // 🔥 测试阶段：不过滤，显示所有对话
        val effectiveMessages = messages

        Log.d(TAG, "有效消息数（不过滤）: ${effectiveMessages.size}")

        val analysisArray = mutableListOf<String>()

        var round = 1
        var i = 0

        Log.d(TAG, "开始遍历消息...")

        while (i < effectiveMessages.size - 1) {
            val userMsg = effectiveMessages[i]
            val aiMsg = effectiveMessages[i + 1]

            Log.d(TAG, "检查位置[$i] 和 [${i+1}]:")
            Log.d(TAG, "  [$i] isUser=${userMsg.isUser}, content='${userMsg.content}'")
            Log.d(TAG, "  [${i+1}] isUser=${aiMsg.isUser}, content='${aiMsg.content}'")

            if (userMsg.isUser && !aiMsg.isUser) {
                Log.d(TAG, "  ✅ 匹配！这是第${round}轮对话")

                val analysis = generateSingleAnalysis(
                    round = round,
                    userMessage = userMsg.content,
                    aiMessage = aiMsg.content,
                    favorChange = aiMsg.favorChange ?: 0,
                    characterName = characterName
                )
                analysisArray.add(analysis)
                Log.d(TAG, "  ✅ 第${round}轮分析已生成")
                round++
            } else {
                Log.d(TAG, "  ❌ 不匹配，跳过")
            }

            i += 2
        }

        Log.d(TAG, "遍历完成！最终生成了 ${analysisArray.size} 轮分析")

        val result = "[${analysisArray.joinToString(",")}]"
        Log.d(TAG, "返回JSON长度: ${result.length}")

        return result
    }

    /**
     * 过滤无效对话（测试阶段不使用）
     */
    private fun filterEffectiveMessages(messages: List<Message>): List<Message> {
        val ineffectivePatterns = listOf(
            "你好", "您好", "hi", "hello", "嗨",
            "再见", "拜拜", "bye", "886",
            "嗯", "哦", "啊", "呃",
            "谢谢", "多谢", "感谢",
            "在吗", "在不在"
        )

        return messages.filter { message ->
            val content = message.content.trim().lowercase()
            content.length > 3 && !ineffectivePatterns.any { content.contains(it) }
        }
    }

    /**
     * 生成单轮对话的分析
     */
    private fun generateSingleAnalysis(
        round: Int,
        userMessage: String,
        aiMessage: String,
        favorChange: Int,
        characterName: String
    ): String {
        Log.d(TAG, "    生成第${round}轮的分析文本...")

        val analysis = generateAnalysisText(userMessage, favorChange)
        val suggestion = generateSuggestionText(userMessage, favorChange, characterName)

        return """
        {
            "round": $round,
            "userMessage": "${escapeJson(userMessage)}",
            "aiMessage": "${escapeJson(aiMessage)}",
            "analysis": "${escapeJson(analysis)}",
            "suggestion": "${escapeJson(suggestion)}"
        }
        """.trimIndent()
    }

    /**
     * 生成分析文本
     */
    private fun generateAnalysisText(userMessage: String, favorChange: Int): String {
        return when {
            favorChange >= 5 -> {
                listOf(
                    "这句话非常棒！你展现了良好的情商和沟通技巧。",
                    "说得太好了！你的回复充满了情绪价值，让对方感到被重视。",
                    "完美的回复！你成功地抓住了对方的兴趣点。"
                ).random() + when {
                    userMessage.contains("?") || userMessage.contains("？") ->
                        "通过提问的方式引导对话，展现了你的好奇心和对对方的关注。"
                    userMessage.contains("喜欢") || userMessage.contains("爱") ->
                        "表达了积极的情感，让对话氛围更加温暖。"
                    userMessage.length > 20 ->
                        "详细的表达让对方感受到你的真诚，这是建立信任的关键。"
                    else ->
                        "简洁有力的表达恰到好处，避免了啰嗦。"
                }
            }
            favorChange in 1..4 -> {
                listOf(
                    "不错的回复，对话氛围保持得很好。",
                    "这个回应还可以，对方感觉舒服。",
                    "及格的表现，对话在正常进行。"
                ).random() + "但仍有提升空间，可以尝试更有深度的交流。"
            }
            favorChange == 0 -> {
                "这个回复比较平淡，没有推进对话深度。" + when {
                    userMessage.length < 5 -> "过于简短，可能让对方觉得你不够用心。"
                    userMessage.endsWith("。") -> "陈述句容易让对话陷入停滞，建议多用疑问句。"
                    else -> "缺少情绪价值和信息价值，建议增加一些个人观点或提问。"
                }
            }
            favorChange in -4..-1 -> {
                "这个回复有些问题，可能让对方不太舒服。" + when {
                    userMessage.contains("但是") || userMessage.contains("不过") ->
                        "转折词的使用显得有些否定对方，容易引起反感。"
                    userMessage.count { it == '我' } > userMessage.count { it == '你' } ->
                        "过多地谈论自己，显得以自我为中心，要多关注对方。"
                    else -> "表达方式可能不够婉转，要注意措辞。"
                }
            }
            else -> {
                "这是一个很大的失误！" + when {
                    userMessage.contains("丑") || userMessage.contains("胖") || userMessage.contains("矮") ->
                        "涉及外貌评价是社交禁忌，即使开玩笑也要避免。"
                    userMessage.contains("别人") || userMessage.contains("某某") ->
                        "在谈话中提及其他异性容易引起不适和嫉妒。"
                    userMessage.contains("算了") || userMessage.contains("随便") ->
                        "消极的态度会让对方感觉被冷落。"
                    else -> "这样的表达方式伤害了对方的感受，需要特别注意。"
                }
            }
        }
    }

    /**
     * 生成建议文本
     */
    private fun generateSuggestionText(
        userMessage: String,
        favorChange: Int,
        characterName: String
    ): String {
        return when {
            favorChange >= 5 -> {
                "继续保持！你可以在此基础上深入这个话题，或者自然地引入一个新的有趣话题。"
            }
            favorChange in 1..4 -> {
                val suggestions = listOf(
                    "可以尝试：'${generateBetterReply(userMessage, "deep")}' —— 这样更能展现你的思考深度。",
                    "更好的方式：'${generateBetterReply(userMessage, "emotional")}' —— 增加情绪价值。",
                    "建议改为：'${generateBetterReply(userMessage, "question")}' —— 用提问推进对话。"
                )
                suggestions.random()
            }
            favorChange == 0 -> {
                "建议改为：'${generateBetterReply(userMessage, "improve")}' —— 这样既有回应，又能推进对话。" +
                "记住：好的对话要有'一来一往'，既要回应对方，也要引导话题。"
            }
            favorChange < 0 -> {
                "建议改为：'${generateBetterReply(userMessage, "safe")}' —— 这样更安全，不会引起负面情绪。" +
                "避免使用否定词、消极词、对比词，多使用肯定、共情、鼓励的表达方式。"
            }
            else -> "继续努力，多注意对方的反应。"
        }
    }

    /**
     * 生成更好的回复示例
     */
    private fun generateBetterReply(original: String, type: String): String {
        return when (type) {
            "deep" -> "这很有意思，我也有类似的想法，你觉得...（继续深入话题）"
            "emotional" -> "哈哈是啊，我特别能理解你的感受！"
            "question" -> "嗯嗯，那你平时喜欢做什么呢？"
            "improve" -> "是啊，而且我觉得...（表达自己的观点）"
            "safe" -> "你说得对，我也这么认为。"
            else -> "好的，我明白了。"
        }
    }

    /**
     * 转义JSON字符串
     */
    private fun escapeJson(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * 判断是否应该触发突破点
     */
    private fun shouldTriggerPeak(
        oldFavor: Int,
        newFavor: Int,
        change: Int
    ): Boolean {
        val thresholds = listOf(20, 40, 60, 80)
        for (threshold in thresholds) {
            if (oldFavor < threshold && newFavor >= threshold) {
                return true
            }
            if (oldFavor >= threshold && newFavor < threshold) {
                return true
            }
        }

        if (abs(change) >= 5) {
            return true
        }

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