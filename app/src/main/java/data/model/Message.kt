// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/Message.kt

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val type: String = "text",

    // 引用相关字段
    val quotedMessageId: String? = null,
    val quotedContent: String? = null,
    val quotedSender: String? = null
)