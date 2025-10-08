// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/FavorPoint.kt
package com.example.nativechatdemo.data.model

/**
 * 好感线数据点
 */
data class FavorPoint(
    val round: Int,           // 轮次
    val favor: Int,           // 当前好感度（0-100）
    val messageId: String,    // 对应的消息ID
    val reason: String,       // 变化原因
    val timestamp: Long,      // 时间戳（你原来的版本有这个字段）
    val favorChange: Int = 0  // 新增：好感度变化值（正数=增加，负数=减少）
)