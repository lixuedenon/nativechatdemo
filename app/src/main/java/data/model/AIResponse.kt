// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/AIResponse.kt
package com.example.nativechatdemo.data.model

/**
 * AI回复响应
 */
data class AIResponse(
    val message: String,              // 带标记的完整消息
    val favorabilityChange: Int,      // 好感度变化值
    val responseTime: Long            // 响应时间戳
)