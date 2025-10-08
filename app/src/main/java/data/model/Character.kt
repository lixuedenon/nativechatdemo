// app/src/main/java/com/example/nativechatdemo/data/model/Character.kt
package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI角色实体类
 * 对应Flutter的CharacterModel
 */
@Entity(tableName = "characters")
data class Character(
    @PrimaryKey
    val id: String,              // 角色ID
    val name: String,            // 角色名称
    val description: String,     // 角色描述
    val avatar: String,          // 头像资源名（如"gentle_girl"）
    val type: String,            // 角色类型（gentle/lively/elegant/sunny）
    val gender: String,          // 性别（male/female）
    val isVip: Boolean = false   // 是否VIP角色
)

/**
 * 角色类型枚举
 */
enum class CharacterType(val id: String, val displayName: String) {
    GENTLE("gentle_girl", "温柔女生"),
    LIVELY("lively_girl", "活泼女生"),
    ELEGANT("elegant_girl", "优雅女生"),
    SUNNY("sunny_boy", "阳光男生");
    
    companion object {
        fun fromId(id: String): CharacterType? {
            return values().find { it.id == id }
        }
    }
}