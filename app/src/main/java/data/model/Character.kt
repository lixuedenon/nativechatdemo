// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/Character.kt
// 文件名：Character.kt
// 类型：Model（Room数据实体类）
// 功能：角色数据模型，存储AI角色的完整信息，包括基础信息和4维度特征配置
// 依赖：
//   - androidx.room（Room数据库注解）
//   - CharacterTraits.kt（特征配置类）
// 引用：被以下文件使用
//   - CharacterDao.kt（数据访问层）
//   - ChatViewModel.kt（业务逻辑层）
//   - CharacterConfigActivity.kt（配置界面）
//   - PromptBuilder.kt（构建AI Prompt）
// 数据库表名：characters
// 创建日期：2025-10-15
// 最后修改：2025-10-28（扩展支持4维度配置）
// 作者：Claude

package com.example.nativechatdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 角色实体类（Room数据库表）
 * 存储AI角色的所有配置信息
 *
 * 表结构：
 * - id: 主键（自增）
 * - name: 角色名称
 * - gender: 性别（male/female）
 * - avatarRes: 头像资源ID（可选）
 * - traitsJson: 4维度特征配置（JSON字符串）
 * - isTemplate: 是否为预设模板
 * - createdAt: 创建时间
 * - updatedAt: 最后更新时间
 */
@Entity(tableName = "characters")
data class Character(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 基础信息
    val name: String,                       // 角色名称（如："小美"）
    val gender: String,                     // 性别：male/female
    val avatarRes: Int? = null,             // 头像资源ID（可选）

    // 4维度特征配置（JSON格式存储）
    val traitsJson: String,                 // CharacterTraits序列化后的JSON字符串

    // 标识字段
    val isTemplate: Boolean = false,        // 是否为系统预设模板
    val templateType: String? = null,       // 模板类型（如："cute_student"）

    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 解析特征配置为CharacterTraits对象
     * @return CharacterTraits对象
     */
    fun getTraits(): CharacterTraits {
        return try {
            CharacterTraits.fromJson(traitsJson)
        } catch (e: Exception) {
            // 如果解析失败，返回默认配置
            CharacterTraits.createDefault()
        }
    }

    /**
     * 获取角色的简短描述
     * 格式："22岁 学生 可爱软萌型"
     */
    fun getShortDescription(): String {
        val traits = getTraits()
        return "${traits.age}岁 ${traits.occupation.displayName} ${traits.personalityType.displayName}"
    }

    /**
     * 获取角色的详细描述
     */
    fun getDetailedDescription(): String {
        val traits = getTraits()
        return buildString {
            append("${traits.age}岁，${traits.occupation.displayName}，")
            append("${traits.education.displayName}学历。")
            append("性格：${traits.personalityType.displayName}。")
            if (traits.hobbies.isNotEmpty()) {
                append("爱好：${traits.hobbies.joinToString("、") { it.displayName }}。")
            }
        }
    }

    /**
     * 判断是否有共同爱好
     * @param hobby 用户的爱好
     * @return true=有共同爱好
     */
    fun hasCommonHobby(hobby: Hobby): Boolean {
        return getTraits().hobbies.contains(hobby)
    }

    companion object {
        /**
         * 从模板创建角色
         * @param template 角色模板
         * @param customName 自定义名称（可选）
         * @return Character对象
         */
        fun fromTemplate(template: CharacterTemplate, customName: String? = null): Character {
            return Character(
                name = customName ?: template.name,
                gender = "female",  // 默认女性（后续可扩展）
                traitsJson = template.traits.toJson(),
                isTemplate = false,
                templateType = template.name
            )
        }

        /**
         * 创建自定义角色
         * @param name 角色名称
         * @param traits 特征配置
         * @return Character对象
         */
        fun createCustom(name: String, traits: CharacterTraits): Character {
            return Character(
                name = name,
                gender = "female",
                traitsJson = traits.toJson(),
                isTemplate = false,
                templateType = null
            )
        }

        /**
         * 创建默认角色（清纯学妹）
         */
        fun createDefault(name: String = "小美"): Character {
            return Character(
                name = name,
                gender = "female",
                traitsJson = CharacterTraits.createDefault().toJson(),
                isTemplate = false,
                templateType = "default"
            )
        }
    }
}

/**
 * 性别枚举（扩展用）
 */
enum class Gender(val value: String, val displayName: String) {
    MALE("male", "男"),
    FEMALE("female", "女");

    companion object {
        fun fromString(value: String): Gender {
            return values().firstOrNull { it.value == value } ?: FEMALE
        }
    }
}