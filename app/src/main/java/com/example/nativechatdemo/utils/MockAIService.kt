// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/MockAIService.kt

package com.example.nativechatdemo.utils

import android.util.Log
import com.example.nativechatdemo.data.model.AIResponse
import com.example.nativechatdemo.data.model.Message
import kotlin.math.abs

object MockAIService {

    private const val TAG = "MockAIService"

    /**
     * 生成AI回复（正常模式）
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
     * 从消息内容中提取好感度变化
     */
    private fun extractFavorChange(message: Message): Int {
        val regex = """\[FAVOR[_PEAK]*:([+\-]?\d+):.*?]""".toRegex()
        val matchResult = regex.find(message.content)
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    /**
     * 生成复盘模式的AI回复
     */
    fun generateReplayResponse(
        userInput: String,
        characterId: String,
        currentRound: Int,
        conversationHistory: List<Message>,
        currentFavorability: Int,
        replayMode: String,
        originalMessages: List<Message>,
        currentRoundIndex: Int
    ): AIResponse {
        Log.d(TAG, "复盘模式: $replayMode, 当前轮次: $currentRound")

        return when (replayMode) {
            "same" -> generateSameReply(
                characterId,
                currentRound,
                currentFavorability,
                originalMessages,
                currentRoundIndex
            )
            "similar" -> generateSimilarReply(
                userInput,
                characterId,
                currentRound,
                currentFavorability,
                originalMessages,
                currentRoundIndex
            )
            "natural" -> generateNaturalReply(
                userInput,
                characterId,
                currentRound,
                conversationHistory,
                currentFavorability,
                originalMessages,
                currentRoundIndex
            )
            else -> generateResponse(userInput, characterId, currentRound, conversationHistory, currentFavorability)
        }
    }

    /**
     * 相同回复模式：完全重复原对话的AI回复
     */
    private fun generateSameReply(
        characterId: String,
        currentRound: Int,
        currentFavorability: Int,
        originalMessages: List<Message>,
        currentRoundIndex: Int
    ): AIResponse {
        val originalAiMessageIndex = (currentRoundIndex + 1) * 2

        val originalAiMessage = if (originalAiMessageIndex < originalMessages.size) {
            originalMessages[originalAiMessageIndex]
        } else {
            null
        }

        return if (originalAiMessage != null && originalAiMessage.sender != "user") {
            val favorChange = extractFavorChange(originalAiMessage)

            AIResponse(
                message = originalAiMessage.content,
                favorabilityChange = favorChange,
                responseTime = System.currentTimeMillis()
            )
        } else {
            Log.w(TAG, "找不到原对话的AI消息，使用默认回复")
            generateDefaultReply(characterId, currentRound, currentFavorability)
        }
    }

    /**
     * 相近回复模式：引导回到原话题，但用不同的表达
     */
    private fun generateSimilarReply(
        userInput: String,
        characterId: String,
        currentRound: Int,
        currentFavorability: Int,
        originalMessages: List<Message>,
        currentRoundIndex: Int
    ): AIResponse {
        val originalUserMessageIndex = currentRoundIndex * 2

        val originalUserMessage = if (originalUserMessageIndex < originalMessages.size) {
            originalMessages[originalUserMessageIndex].content
        } else {
            null
        }

        val isOnTopic = originalUserMessage?.let {
            userInput.contains(it.take(5)) || it.contains(userInput.take(5))
        } ?: false

        val favorChange = if (isOnTopic) {
            (2..8).random()
        } else {
            (-2..5).random()
        }

        val newFavorability = (currentFavorability + favorChange).coerceIn(0, 100)

        val responseContent = if (isOnTopic) {
            getResponseContent(characterId, currentRound, favorChange)
        } else {
            val guideResponses = listOf(
                "嗯...对了，刚才我们说到哪了？",
                "诶，我们换个话题吧，聊聊${originalUserMessage?.take(10) ?: "之前的话题"}？",
                "哈哈，突然想起来${originalUserMessage?.take(10) ?: "刚才的事"}~"
            )
            guideResponses.random()
        }

        val isPeak = shouldTriggerPeak(currentFavorability, newFavorability, favorChange)
        val reason = if (isPeak) {
            if (isOnTopic) "你成功回到了主题" else "话题有点分散"
        } else {
            ""
        }

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
     * 自然回复模式：自由发挥，但会适时提及原话题
     */
    private fun generateNaturalReply(
        userInput: String,
        characterId: String,
        currentRound: Int,
        conversationHistory: List<Message>,
        currentFavorability: Int,
        originalMessages: List<Message>,
        currentRoundIndex: Int
    ): AIResponse {
        val shouldMentionOriginal = (1..100).random() <= 20

        val favorChange = (-3..8).random()
        val newFavorability = (currentFavorability + favorChange).coerceIn(0, 100)

        val responseContent = if (shouldMentionOriginal && originalMessages.isNotEmpty()) {
            val randomOriginalMsg = originalMessages.filter { it.sender != "user" }.randomOrNull()
            val originalTopic = randomOriginalMsg?.content?.take(15) ?: ""

            val mentionResponses = listOf(
                "${getResponseContent(characterId, currentRound, favorChange)} 对了，$originalTopic",
                "嗯嗯~话说回来，$originalTopic",
                "${getResponseContent(characterId, currentRound, favorChange)} 突然想起$originalTopic"
            )
            mentionResponses.random()
        } else {
            getResponseContent(characterId, currentRound, favorChange)
        }

        val isPeak = shouldTriggerPeak(currentFavorability, newFavorability, favorChange)
        val reason = if (isPeak) {
            getReasonForChange(favorChange, newFavorability, currentFavorability)
        } else {
            ""
        }

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
     * 默认回复（当找不到原消息时）
     */
    private fun generateDefaultReply(
        characterId: String,
        currentRound: Int,
        currentFavorability: Int
    ): AIResponse {
        val favorChange = (0..5).random()
        val responseContent = getResponseContent(characterId, currentRound, favorChange)

        return AIResponse(
            message = "$responseContent [FAVOR:+$favorChange:]",
            favorabilityChange = favorChange,
            responseTime = System.currentTimeMillis()
        )
    }

    /**
     * 生成首次对话分析（修复：动态寻找配对）
     */
    fun generateAnalysis(
        messages: List<Message>,
        characterName: String,
        finalFavor: Int
    ): String {
        Log.d(TAG, "=== generateAnalysis 开始 ===")
        Log.d(TAG, "输入消息数: ${messages.size}")

        val analysisArray = mutableListOf<String>()

        var round = 1
        var i = 0

        while (i < messages.size) {
            // 跳过欢迎消息（第一条非用户消息）
            if (i == 0 && messages[i].sender != "user") {
                i++
                continue
            }

            val currentMsg = messages[i]

            // 如果当前是用户消息，找下一条AI消息
            if (currentMsg.sender == "user") {
                // 向后查找第一条AI消息
                var aiMsgIndex = i + 1
                while (aiMsgIndex < messages.size && messages[aiMsgIndex].sender == "user") {
                    aiMsgIndex++
                }

                if (aiMsgIndex < messages.size) {
                    val aiMsg = messages[aiMsgIndex]

                    Log.d(TAG, "✅ 匹配第${round}轮: user='${currentMsg.content}', ai='${aiMsg.content}'")

                    val analysis = generateSingleAnalysis(
                        round = round,
                        userMessage = currentMsg.content,
                        aiMessage = aiMsg.content,
                        favorChange = extractFavorChange(aiMsg),
                        characterName = characterName
                    )
                    analysisArray.add(analysis)
                    round++

                    // 跳到AI消息的下一条
                    i = aiMsgIndex + 1
                } else {
                    i++
                }
            } else {
                // 如果当前是AI消息，跳过
                i++
            }
        }

        Log.d(TAG, "遍历完成！最终生成了 ${analysisArray.size} 轮分析")

        return "[${analysisArray.joinToString(",")}]"
    }

    /**
     * 生成二次复盘分析（修复：动态寻找配对）
     */
    fun generateSecondReviewAnalysis(
        currentMessages: List<Message>,
        originalMessages: List<Message>,
        characterName: String,
        finalFavor: Int
    ): String {
        Log.d(TAG, "=== generateSecondReviewAnalysis 开始 ===")
        Log.d(TAG, "当前消息数: ${currentMessages.size}, 原消息数: ${originalMessages.size}")

        val analysisArray = mutableListOf<String>()

        var round = 1
        var i = 0

        while (i < currentMessages.size) {
            // 跳过欢迎消息
            if (i == 0 && currentMessages[i].sender != "user") {
                i++
                continue
            }

            val currentMsg = currentMessages[i]

            if (currentMsg.sender == "user") {
                // 向后查找第一条AI消息
                var aiMsgIndex = i + 1
                while (aiMsgIndex < currentMessages.size && currentMessages[aiMsgIndex].sender == "user") {
                    aiMsgIndex++
                }

                if (aiMsgIndex < currentMessages.size) {
                    val aiMsg = currentMessages[aiMsgIndex]

                    // 找对应的原对话（也用动态查找）
                    val originalUserMsg = findOriginalUserMessage(originalMessages, round)

                    val analysis = generateSecondSingleAnalysis(
                        round = round,
                        userMessage = currentMsg.content,
                        aiMessage = aiMsg.content,
                        originalUserMessage = originalUserMsg?.content,
                        favorChange = extractFavorChange(aiMsg),
                        characterName = characterName
                    )
                    analysisArray.add(analysis)
                    round++

                    i = aiMsgIndex + 1
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        Log.d(TAG, "遍历完成！最终生成了 ${analysisArray.size} 轮分析")

        return "[${analysisArray.joinToString(",")}]"
    }

    /**
     * 查找原对话中第n轮的用户消息
     */
    private fun findOriginalUserMessage(messages: List<Message>, targetRound: Int): Message? {
        var round = 1
        var i = 0

        while (i < messages.size) {
            if (i == 0 && messages[i].sender != "user") {
                i++
                continue
            }

            val currentMsg = messages[i]

            if (currentMsg.sender == "user") {
                if (round == targetRound) {
                    return currentMsg
                }
                round++
            }
            i++
        }

        return null
    }

    /**
     * 生成单轮对话的分析（首次复盘）
     */
    private fun generateSingleAnalysis(
        round: Int,
        userMessage: String,
        aiMessage: String,
        favorChange: Int,
        characterName: String
    ): String {
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
     * 生成单轮对话的分析（二次复盘 - 更友好）
     */
    private fun generateSecondSingleAnalysis(
        round: Int,
        userMessage: String,
        aiMessage: String,
        originalUserMessage: String?,
        favorChange: Int,
        characterName: String
    ): String {
        val analysis = generateSecondAnalysisText(userMessage, originalUserMessage, favorChange)
        val suggestion = generateSecondSuggestionText(userMessage, favorChange)

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
     * 生成分析文本（首次复盘）
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
     * 生成分析文本（二次复盘 - 更友好，多夸奖）
     */
    private fun generateSecondAnalysisText(
        userMessage: String,
        originalUserMessage: String?,
        favorChange: Int
    ): String {
        val isImproved = originalUserMessage?.let { userMessage != it } ?: true

        val baseAnalysis = when {
            favorChange >= 5 -> {
                listOf(
                    "👏 太棒了！这次回答非常出色！",
                    "🎉 完美的回复！你的进步很明显！",
                    "✨ 说得非常好！这就是高情商的表现！"
                ).random()
            }
            favorChange in 1..4 -> {
                listOf(
                    "😊 不错哦！这个回答挺好的！",
                    "👍 做得好！这样回答是对的！",
                    "💪 很棒！继续保持这个水平！"
                ).random()
            }
            favorChange == 0 -> {
                listOf(
                    "🤔 这个回答还可以，但可以更好！",
                    "💭 嗯...这样回答也不错，试试别的方式？",
                    "📚 不错的尝试，我们来看看更好的方式！"
                ).random()
            }
            else -> {
                listOf(
                    "😅 这次稍微有点偏差，没关系，我们调整一下！",
                    "🎯 离目标还有一点距离，再优化一下！",
                    "💡 换个角度试试，效果会更好！"
                ).random()
            }
        }

        val improvement = if (isImproved && originalUserMessage != null) {
            "相比第一次（$originalUserMessage），这次你做了调整，"
        } else {
            ""
        }

        return "$baseAnalysis $improvement${getPositiveReason(userMessage, favorChange)}"
    }

    /**
     * 获取积极的原因解释
     */
    private fun getPositiveReason(userMessage: String, favorChange: Int): String {
        return when {
            userMessage.contains("?") || userMessage.contains("？") ->
                "通过提问引导对话，展现了你的关注！"
            userMessage.contains("喜欢") || userMessage.contains("爱") ->
                "表达了真挚的情感，很有感染力！"
            userMessage.length > 20 ->
                "回答得很详细，显示出你的用心！"
            userMessage.contains("哈") || userMessage.contains("笑") ->
                "幽默感满分，氛围很好！"
            else ->
                "这样的表达方式很自然，对方会感到舒服！"
        }
    }

    /**
     * 生成建议文本（首次复盘）
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
     * 生成建议文本（二次复盘 - 提供更好的替代示例）
     */
    private fun generateSecondSuggestionText(
        userMessage: String,
        favorChange: Int
    ): String {
        return when {
            favorChange >= 5 -> {
                val alt1 = generateAlternativeReply(userMessage, "humorous")
                val alt2 = generateAlternativeReply(userMessage, "caring")
                val alt3 = generateAlternativeReply(userMessage, "playful")
                val alternatives = listOf(
                    "你还可以这样说：$alt1，增加一点幽默感！",
                    "换个说法也不错：$alt2，更显关怀！",
                    "或者试试：$alt3，俏皮一点也很好！"
                )
                "💡 ${alternatives.random()}"
            }
            favorChange in 1..4 -> {
                val alt1 = generateAlternativeReply(userMessage, "deeper")
                val alt2 = generateAlternativeReply(userMessage, "emotional")
                val alt3 = generateAlternativeReply(userMessage, "creative")
                val alternatives = listOf(
                    "试试这样说：$alt1，会更有深度！",
                    "这样表达可能更好：$alt2，情感更饱满！",
                    "换个角度：$alt3，更有创意！"
                )
                "💡 ${alternatives.random()}"
            }
            else -> {
                val alt1 = generateAlternativeReply(userMessage, "positive")
                val alt2 = generateAlternativeReply(userMessage, "gentle")
                val alt3 = generateAlternativeReply(userMessage, "engaging")
                val alternatives = listOf(
                    "不如这样说：$alt1，更积极正面！",
                    "建议改成：$alt2，语气更温和！",
                    "这样可能更好：$alt3，更有吸引力！"
                )
                "💡 ${alternatives.random()}"
            }
        }
    }

    /**
     * 生成更好的回复示例（首次复盘用）
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
     * 生成替代回复示例（二次复盘用 - 更具体、更实用）
     */
    private fun generateAlternativeReply(original: String, style: String): String {
        return when (style) {
            "humorous" -> "哈哈，${original}！你这么说我都忍不住笑了~"
            "caring" -> "${original}，你要照顾好自己哦~"
            "playful" -> "嘿嘿，${original}！你猜我会怎么做？"
            "deeper" -> "${original}，这让我想到一个有趣的问题..."
            "emotional" -> "真的吗？${original}！我特别能理解这种感觉！"
            "creative" -> "${original}，不过换个角度看，是不是也可以..."
            "positive" -> "${original}，而且这样还能..."
            "gentle" -> "嗯嗯，${original}呢~"
            "engaging" -> "${original}！对了，你有没有..."
            else -> original
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