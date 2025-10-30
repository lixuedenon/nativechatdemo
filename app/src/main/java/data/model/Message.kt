// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/Message.kt
// 文件名：Message.kt
// 类型：Model（Room数据实体类）
// 功能：消息数据模型，存储聊天消息的完整信息
// 依赖：androidx.room（Room数据库注解）
// 引用：被以下文件使用
//   - MessageDao.kt（数据访问层）
//   - ChatViewModel.kt（业务逻辑层）
//   - MessageAdapter.kt（UI适配器）
//   - PromptBuilder.kt（构建AI Prompt）
// 数据库表名：messages
// 创建日期：2025-10-15
// 最后修改：2025-10-29（添加isUser扩展属性）
// 作者：Claude

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 消息实体类（Room数据库表）
 * 存储聊天消息的所有信息
 *
 * 表结构：
 * - id: 主键（UUID）
 * - conversationId: 所属对话ID
 * - sender: 发送者（"user"或"ai"）
 * - content: 消息内容
 * - timestamp: 发送时间戳
 * - type: 消息类型（默认"text"）
 * - quotedMessageId: 引用的消息ID（可选）
 * - quotedContent: 引用的消息内容（可选）
 * - quotedSender: 引用的消息发送者（可选）
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,                         // 消息唯一ID（UUID）

    val conversationId: String,             // 所属对话ID

    val sender: String,                     // 发送者："user"（用户） 或 "ai"（AI角色）

    val content: String,                    // 消息内容

    val timestamp: Long,                    // 发送时间戳（毫秒）

    val type: String = "text",              // 消息类型：text/image/voice等（当前只支持text）

    // ========== 引用功能相关字段 ==========
    val quotedMessageId: String? = null,    // 引用的消息ID（长按消息选择引用时使用）
    val quotedContent: String? = null,      // 引用的消息内容（用于显示）
    val quotedSender: String? = null        // 引用的消息发送者（"user"或"ai"）
) {
    /**
     * 扩展属性：判断是否为用户消息
     *
     * 用途：
     * 1. MessageAdapter中区分用户/AI消息布局（左侧/右侧）
     * 2. PromptBuilder中构建OpenAI messages数组（role: "user"/"assistant"）
     *
     * @return true=用户消息，false=AI消息
     */
    val isUser: Boolean
        get() = sender == "user"
}