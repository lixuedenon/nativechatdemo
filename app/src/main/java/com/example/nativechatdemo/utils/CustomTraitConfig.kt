// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/CustomTraitConfig.kt
// 文件类型：Kotlin Object
// 文件状态：【新建】
// 用途：定制模块的特质配置和场景定义

package com.example.nativechatdemo.utils

object CustomTraitConfig {

    /**
     * 场景类型
     */
    enum class ScenarioType(val value: Int, val title: String, val description: String) {
        KNOWN_ESTABLISHED(1, "相识并初步了解", "已经认识，了解对方性格，但还未确立恋爱关系"),
        JUST_MET_PARTIAL(2, "刚刚相识并有初步了解", "刚认识不久，了解部分特质"),
        IDEAL_TYPE(3, "尚未相识但有心仪目标", "还没认识，但心中有理想型"),
        JUST_MET_UNKNOWN(4, "刚刚相识但不了解", "刚认识，对对方一无所知")
    }

    /**
     * 特质维度和选项
     */
    val TRAIT_CATEGORIES = mapOf(
        "性格" to listOf(
            "外向", "内向", "开朗", "文静", "活泼", "稳重",
            "幽默", "认真", "温柔", "独立", "理性", "感性"
        ),
        "兴趣爱好" to listOf(
            "运动", "阅读", "音乐", "美食", "旅行", "游戏",
            "摄影", "绘画", "舞蹈", "电影", "烹饪", "健身"
        ),
        "情感特征" to listOf(
            "敏感", "钝感", "粘人", "独立", "浪漫", "务实",
            "嫉妒心强", "包容", "缺乏安全感", "自信", "情绪稳定", "情绪化"
        ),
        "生活态度" to listOf(
            "事业型", "家庭型", "自由派", "规律型", "随性", "计划性强",
            "节俭", "享受生活", "工作狂", "慢生活", "完美主义", "随遇而安"
        ),
        "社交特点" to listOf(
            "社交达人", "社恐", "选择性社交", "热情", "冷淡", "礼貌",
            "直率", "委婉", "善于倾听", "健谈", "幽默风趣", "严肃"
        )
    )

    /**
     * 根据特质组合生成性格描述
     */
    fun generatePersonalityDescription(traits: List<String>): String {
        val personality = StringBuilder()

        // 分析特质类型
        val outgoing = traits.contains("外向") || traits.contains("活泼") || traits.contains("社交达人")
        val sensitive = traits.contains("敏感") || traits.contains("情绪化")
        val romantic = traits.contains("浪漫")
        val independent = traits.contains("独立")

        // 生成描述
        if (outgoing) {
            personality.append("这是一个性格开朗、善于社交的人，")
        } else {
            personality.append("这是一个性格内向、喜欢安静的人，")
        }

        if (sensitive) {
            personality.append("情感丰富且敏感，需要更多的关心和理解。")
        } else {
            personality.append("情绪稳定，相处起来比较轻松。")
        }

        if (romantic) {
            personality.append("注重浪漫和仪式感，")
        }

        if (independent) {
            personality.append("有自己的生活和空间，不会过分依赖。")
        }

        return personality.toString()
    }

    /**
     * 生成聊天建议
     */
    fun generateChatSuggestions(traits: List<String>): List<String> {
        val suggestions = mutableListOf<String>()

        if (traits.contains("敏感")) {
            suggestions.add("说话要注意语气，避免过于直接的表达")
        }
        if (traits.contains("嫉妒心强")) {
            suggestions.add("避免过多谈论异性朋友或前任")
        }
        if (traits.contains("缺乏安全感")) {
            suggestions.add("多给予肯定和承诺，让对方感到安心")
        }
        if (traits.contains("独立")) {
            suggestions.add("尊重对方的个人空间，不要过分粘人")
        }
        if (traits.contains("浪漫")) {
            suggestions.add("偶尔制造小惊喜，注重节日和纪念日")
        }
        if (traits.contains("理性")) {
            suggestions.add("用逻辑和事实说话，避免过于情绪化")
        }
        if (traits.contains("感性")) {
            suggestions.add("多分享感受和情绪，建立情感连接")
        }

        if (suggestions.isEmpty()) {
            suggestions.add("保持真诚，自然地表达自己")
            suggestions.add("多倾听对方，了解对方的想法")
            suggestions.add("找到共同话题，建立连接")
        }

        return suggestions
    }

    /**
     * 获取告白成功率分析文案
     */
    fun getConfessionAnalysis(successRate: Float, testType: Int): String {
        val rateDesc = when {
            successRate >= 80 -> "非常高"
            successRate >= 60 -> "较高"
            successRate >= 40 -> "中等"
            successRate >= 20 -> "较低"
            else -> "很低"
        }

        val typeDesc = when (testType) {
            1 -> "你们已经连续聊了很多，关系发展顺利"
            2 -> "虽然多次尝试，但进展不够理想"
            3 -> "你尝试了不同类型，还需要找到合适的方向"
            else -> ""
        }

        return "告白成功率${rateDesc}（${successRate.toInt()}%）\n\n$typeDesc"
    }

    /**
     * 生成成功结束语
     */
    fun generateSuccessEnding(traits: List<String>, rounds: Int): String {
        val personality = if (traits.contains("温柔")) "温柔"
            else if (traits.contains("活泼")) "活泼"
            else if (traits.contains("优雅")) "优雅"
            else "有魅力"

        return """
            💕 美好的开始
            
            经过 $rounds 轮的愉快交谈，你已经和这位${personality}的异性建立了良好的关系。
            
            对方对你很感兴趣，你们聊得非常投机。
            
            相信在现实生活中，你也能找到属于自己的幸福！
            
            记住：真诚、尊重、理解是恋爱的基础。
            
            祝你早日找到心仪的伴侣！
        """.trimIndent()
    }

    /**
     * 生成失败结束语
     */
    fun generateFailureEnding(): String {
        return """
            💔 对话结束
            
            很遗憾，这次对话没有达到预期效果。
            
            对方似乎对你失去了兴趣...
            
            不要灰心！每次失败都是学习的机会。
            
            建议：
            • 更多地倾听对方
            • 找到共同话题
            • 保持积极乐观的态度
            • 真诚地表达自己
            
            再试一次吧！
        """.trimIndent()
    }
}