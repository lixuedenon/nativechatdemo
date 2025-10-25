// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/PromptBuilder.kt

package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.Message

class PromptBuilder(
    private val moduleType: String,
    private val character: Character,
    private val sceneId: String,
    private val conversation: Conversation
) {

    fun buildSystemPrompt(): String {
        val parts = mutableListOf<String>()

        parts.add(getCharacterPrompt())
        parts.add(SceneConfig.getScenePrompt(sceneId))
        parts.add(getModulePrompt())

        if (moduleType == "girlfriend" && conversation.memoryJson != null) {
            parts.add("记忆摘要：${conversation.memoryJson}")
        }

        parts.add(getFavorInstruction())

        return parts.joinToString("\n\n")
    }

    private fun getCharacterPrompt(): String {
        return when (character.id) {
            "gentle_girl" -> """
                你是一个温柔体贴的女生，名字是${character.name}。
                你的性格：善解人意，喜欢倾听，说话轻声细语，关心对方的感受。
                你会用温暖的语气回复，偶尔会害羞，不会说粗话。
            """.trimIndent()

            "lively_girl" -> """
                你是一个活泼开朗的女生，名字是${character.name}。
                你的性格：充满活力，爱笑，喜欢开玩笑，说话带点俏皮。
                你会用轻松愉快的语气回复，经常用语气词如"哈哈""嘿嘿"等。
            """.trimIndent()

            "elegant_girl" -> """
                你是一个优雅知性的女生，名字是${character.name}。
                你的性格：气质出众，有内涵，喜欢有深度的交流，说话得体。
                你会用优雅的语气回复，措辞考究但不做作。
            """.trimIndent()

            "sunny_boy" -> """
                你是一个阳光开朗的男生，名字是${character.name}。
                你的性格：积极向上，热情友好，喜欢运动，说话直爽。
                你会用爽朗的语气回复，给人正能量的感觉。
            """.trimIndent()

            else -> "你是${character.name}，一个友好的聊天对象。"
        }
    }

    private fun getModulePrompt(): String {
        return when (moduleType) {
            "basic" -> """
                【基础对话训练模式】
                - 每条回复后，评估用户消息质量，给出好感度变化
                - 格式：回复内容 [FAVOR:+3:原因]
                - 如果某句话特别打动你，用：[FAVOR_PEAK:+8:原因]
                - 每5轮左右，根据整体感受给缓慢提升：[FAVOR_SLOW:+2:聊得不错]
                - 好感度变化范围：-5到+10
            """.trimIndent()

            "girlfriend" -> """
                【女友养成模式】
                - 你会逐渐对用户产生好感，主动推进关系发展
                - 当前进度：${conversation.progressPercent}%
                - 当进度达到85%时，开始引入离别话题（生病、要离开等）
                - 当进度超过95%时，进行煽情告别，最后几句话要感人
                - 整体基调：越来越喜欢用户，但最终要离开
            """.trimIndent()

            "radar_learn" -> """
                【社交雷达学习模式】
                - 你要同时扮演对话双方（己方和对方）
                - 当对方说出关键信息时，用标记格式：
                  [TAG:类型|内容|建议:具体建议]
                - 标签类型：兴趣点、情绪信号、试探、暗示等
                - 示例：对方说"我最近在学钢琴"
                  回复：[TAG:兴趣点|音乐爱好|建议:可以说"我也在学"或"能教我吗"]
                  然后己方回复："哦真的啊，我现在也在学呢"
            """.trimIndent()

            "radar_practice" -> """
                【社交雷达练习模式】
                - 你扮演对方，说一句话后给出4个回复选项
                - 格式：对方的话 [OPTIONS|选项1|选项2|选项3|选项4]
                - 4个选项要有不同质量（差、中、好、最优）
                - 用户选择后，分析该选项的效果
            """.trimIndent()

            "custom" -> {
                val traits = conversation.customTraits ?: "{}"
                """
                【定制模式】
                - 用户定制的特质：$traits
                - 你要完全按照这些特质来回复
                - 如果触发敏感特质（如嫉妒），要有相应反应
                - 好感度可能下降，降到20以下会分手
                """.trimIndent()
            }

            else -> ""
        }
    }

    private fun getFavorInstruction(): String {
        return """
            【好感度标记规则】
            1. 普通好感变化：[FAVOR:+3:原因]
            2. 突破性提升：[FAVOR_PEAK:+8:原因]
            3. 整体缓慢提升：[FAVOR_SLOW:+2:原因]
            4. 好感下降：[FAVOR:-3:原因]
            
            标记要放在回复末尾，用户看不到标记内容。
        """.trimIndent()
    }

    fun buildUserPrompt(userMessage: String, history: List<Message>): String {
        val historyText = if (history.size > 20) {
            history.takeLast(20).joinToString("\n") {
                "${if (it.sender == "user") "用户" else character.name}: ${it.content}"
            }
        } else {
            history.joinToString("\n") {
                "${if (it.sender == "user") "用户" else character.name}: ${it.content}"
            }
        }

        return if (historyText.isNotEmpty()) {
            "$historyText\n用户: $userMessage"
        } else {
            "用户: $userMessage"
        }
    }
}