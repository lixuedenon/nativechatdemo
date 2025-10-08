package com.example.nativechatdemo.utils

object FavorAnalyzer {

    data class FavorChange(
        val value: Int,          // 变化值
        val reason: String,      // 原因
        val isPeak: Boolean,     // 是否是突破点
        val isSlow: Boolean      // 是否是缓慢提升
    )

    /**
     * 解析AI回复中的好感度标记
     * 格式：[FAVOR:+3:原因] 或 [FAVOR_PEAK:+8:原因] 或 [FAVOR_SLOW:+2:原因]
     */
    fun parse(aiResponse: String): FavorChange? {
        val regex = """\[FAVOR(_PEAK|_SLOW)?:([+-]?\d+):([^\]]+)\]""".toRegex()
        val match = regex.find(aiResponse) ?: return null

        val type = match.groupValues[1]
        val value = match.groupValues[2].toIntOrNull() ?: 0
        val reason = match.groupValues[3]

        return FavorChange(
            value = value,
            reason = reason,
            isPeak = type == "_PEAK",
            isSlow = type == "_SLOW"
        )
    }

    /**
     * 移除AI回复中的好感度标记，返回纯文本
     */
    fun extractCleanMessage(aiResponse: String): String {
        return aiResponse
            .replace("""\[FAVOR[^\]]+\]""".toRegex(), "")
            .trim()
    }

    /**
     * 解析社交雷达标记
     * 格式：[TAG:类型|内容|建议:具体建议]
     */
    fun parseRadarTag(aiResponse: String): RadarTag? {
        val regex = """\[TAG:([^|]+)\|([^|]+)\|建议:([^\]]+)\]""".toRegex()
        val match = regex.find(aiResponse) ?: return null

        return RadarTag(
            type = match.groupValues[1],
            content = match.groupValues[2],
            suggestion = match.groupValues[3]
        )
    }

    /**
     * 解析练习模式选项
     * 格式：[OPTIONS|选项1|选项2|选项3|选项4]
     */
    fun parseOptions(aiResponse: String): List<String>? {
        val regex = """\[OPTIONS\|([^|]+)\|([^|]+)\|([^|]+)\|([^\]]+)\]""".toRegex()
        val match = regex.find(aiResponse) ?: return null

        return listOf(
            match.groupValues[1],
            match.groupValues[2],
            match.groupValues[3],
            match.groupValues[4]
        )
    }

    data class RadarTag(
        val type: String,        // 标签类型：兴趣点、情绪信号等
        val content: String,     // 具体内容
        val suggestion: String   // 回复建议
    )
}