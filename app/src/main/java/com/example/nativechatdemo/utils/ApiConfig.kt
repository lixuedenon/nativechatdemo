// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/ApiConfig.kt
// 文件名：ApiConfig.kt
// 文件类型：object（单例工具类）
// 状态：✅ 新建文件（简化版，无加密依赖）
// 创建日期：2025-10-28
// 作者：Claude
//
// 功能说明：
// 1. 保存和加载OpenAI API Key到本地存储
// 2. 使用普通SharedPreferences存储（简化版）
// 3. 提供API Key的增删改查功能
// 4. 验证API Key格式

package com.example.nativechatdemo.utils

import android.content.Context

/**
 * OpenAI API配置管理工具（简化版）
 *
 * 使用普通SharedPreferences存储API Key
 * 注意：生产环境建议从服务器获取API Key
 */
object ApiConfig {

    // SharedPreferences文件名
    private const val PREFS_NAME = "openai_config"

    // API Key存储键名
    private const val KEY_API_KEY = "api_key"

    // API Key前缀
    private const val API_KEY_PREFIX = "sk-"

    /**
     * 保存API Key到本地
     *
     * @param context 上下文
     * @param apiKey OpenAI API Key
     * @return 是否保存成功
     */
    fun saveApiKey(context: Context, apiKey: String): Boolean {
        // 验证API Key格式
        if (!isValidApiKey(apiKey)) {
            return false
        }

        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_API_KEY, apiKey)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从本地加载API Key
     *
     * @param context 上下文
     * @return API Key，如果不存在返回null
     */
    fun loadApiKey(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_API_KEY, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 删除保存的API Key
     *
     * @param context 上下文
     * @return 是否删除成功
     */
    fun clearApiKey(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_API_KEY)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查是否已保存API Key
     *
     * @param context 上下文
     * @return 是否存在API Key
     */
    fun hasApiKey(context: Context): Boolean {
        val apiKey = loadApiKey(context)
        return !apiKey.isNullOrEmpty() && isValidApiKey(apiKey)
    }

    /**
     * 验证API Key格式是否正确
     *
     * OpenAI API Key格式：
     * - 以 "sk-" 开头
     * - 长度在20-100个字符之间
     *
     * @param apiKey 要验证的API Key
     * @return 是否有效
     */
    fun isValidApiKey(apiKey: String?): Boolean {
        if (apiKey.isNullOrEmpty()) {
            return false
        }

        // 检查前缀
        if (!apiKey.startsWith(API_KEY_PREFIX)) {
            return false
        }

        // 检查长度
        if (apiKey.length < 20 || apiKey.length > 100) {
            return false
        }

        return true
    }

    /**
     * 获取脱敏的API Key（用于显示）
     *
     * 例如：sk-xxxxx...xxxxx（只显示前后各6个字符）
     *
     * @param apiKey 完整的API Key
     * @return 脱敏后的字符串
     */
    fun getMaskedApiKey(apiKey: String?): String {
        if (apiKey.isNullOrEmpty() || apiKey.length < 15) {
            return "sk-***"
        }

        val prefix = apiKey.substring(0, 6)
        val suffix = apiKey.substring(apiKey.length - 6)
        return "$prefix...${suffix}"
    }

    /**
     * 更新API Key
     *
     * @param context 上下文
     * @param newApiKey 新的API Key
     * @return 是否更新成功
     */
    fun updateApiKey(context: Context, newApiKey: String): Boolean {
        clearApiKey(context)
        return saveApiKey(context, newApiKey)
    }
}