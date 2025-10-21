// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/CustomPartnerTrait.kt
// 文件类型：Kotlin Data Class (Room Entity)
// 用途：存储用户定制的角色特质

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_partner_traits")
data class CustomPartnerTrait(
    @PrimaryKey val id: String,
    val userId: String,                    // 用户ID
    val scenarioType: Int,                 // 场景类型 1-4
    val gender: String,                    // 性别 male/female
    val traitTags: String,                 // JSON数组：选中的标签
    val customDescription: String = "",    // 用户自定义描述
    val createdAt: Long,
    val updatedAt: Long,
    val chatCount: Int = 0,               // 使用此特质的聊天次数
    val lastChatDate: Long = 0            // 最后聊天时间
)