// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/OpenAIService.kt
// 文件名：OpenAIService.kt
// 类型：Service（单例服务类）
// 功能：封装OpenAI API调用，负责发送消息、接收AI回复、错误处理、重试逻辑和Token统计
// 依赖：
//   - kotlinx.coroutines（协程，异步处理）
//   - org.json（JSON解析）
//   - java.net.HttpURLConnection（网络请求）
// 引用：被以下文件调用
//   - ChatViewModel.kt（聊天业务逻辑）
// API文档：https://platform.openai.com/docs/api-reference/chat
// 创建日期：2025-10-28
// 最后修改：2025-10-29
// 作者：Claude

package com.example.nativechatdemo.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * OpenAI API服务
 * 单例模式，负责与OpenAI API通信
 *
 * 使用方式：
 * 1. 先调用 initialize(apiKey) 初始化API Key
 * 2. 调用 sendMessage() 发送消息并获取回复
 * 3. 调用 testConnection() 测试API连接
 */
object OpenAIService {

    // ========== 配置参数 ==========
    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private const val DEFAULT_MODEL = "gpt-3.5-turbo"  // 默认模型
    private const val TIMEOUT = 30000                   // 超时时间：30秒
    private const val MAX_RETRIES = 2                   // 最大重试次数

    private var apiKey: String = ""
    private var currentModel: String = DEFAULT_MODEL

    // ========== 初始化 ==========

    /**
     * 初始化API Key
     * @param key OpenAI API Key（格式：sk-xxx）
     */
    fun initialize(key: String) {
        apiKey = key.trim()
    }

    /**
     * 设置使用的模型
     * @param model 模型名称（如：gpt-3.5-turbo, gpt-4）
     */
    fun setModel(model: String) {
        currentModel = model
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return apiKey.isNotEmpty() && apiKey.startsWith("sk-")
    }

    // ========== 核心方法：发送消息 ==========

    /**
     * 发送消息到OpenAI并获取回复
     *
     * @param messages 消息历史数组（JSON格式）
     *   格式：[
     *     {"role": "system", "content": "你是..."},
     *     {"role": "user", "content": "用户消息"},
     *     {"role": "assistant", "content": "AI回复"}
     *   ]
     * @param temperature 回复的随机性（0.0-2.0），默认0.7
     *   - 0.0 = 完全确定性，每次回复相同
     *   - 1.0 = 平衡
     *   - 2.0 = 非常随机
     * @param maxTokens 最大Token数（可选，null=不限制）
     * @return AIResponse对象（包含回复内容、Token统计等）
     */
    suspend fun sendMessage(
        messages: JSONArray,
        temperature: Double = 0.7,
        maxTokens: Int? = null
    ): AIResponse = withContext(Dispatchers.IO) {

        // 检查初始化
        if (!isInitialized()) {
            throw IllegalStateException("OpenAI API未初始化，请先调用initialize()")
        }

        // 重试逻辑
        var lastException: Exception? = null
        repeat(MAX_RETRIES + 1) { attempt ->
            try {
                return@withContext callAPI(messages, temperature, maxTokens)
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES) {
                    // 等待后重试（指数退避）
                    kotlinx.coroutines.delay(1000L * (attempt + 1))
                }
            }
        }

        // 所有重试都失败，抛出异常
        throw lastException ?: Exception("未知错误")
    }

    /**
     * 内部方法：实际调用API
     */
    private fun callAPI(
        messages: JSONArray,
        temperature: Double,
        maxTokens: Int?
    ): AIResponse {

        // 1. 构建请求体
        val requestBody = JSONObject().apply {
            put("model", currentModel)
            put("messages", messages)
            put("temperature", temperature)
            if (maxTokens != null) {
                put("max_tokens", maxTokens)
            }
        }

        // 2. 建立连接
        val connection = URL(API_URL).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT
            readTimeout = TIMEOUT
            doOutput = true
            doInput = true

            // 设置请求头
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }

        try {
            // 3. 发送请求
            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            // 4. 读取响应
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 成功响应
                val response = BufferedReader(
                    InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)
                ).use { it.readText() }

                return parseResponse(response)

            } else {
                // 错误响应
                val errorResponse = BufferedReader(
                    InputStreamReader(connection.errorStream ?: connection.inputStream, StandardCharsets.UTF_8)
                ).use { it.readText() }

                throw OpenAIException(responseCode, errorResponse)
            }

        } finally {
            connection.disconnect()
        }
    }

    /**
     * 解析API响应
     */
    private fun parseResponse(response: String): AIResponse {
        val json = JSONObject(response)

        // 提取回复内容
        val choices = json.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        val content = message.getString("content")

        // 提取Token统计
        val usage = json.getJSONObject("usage")
        val promptTokens = usage.getInt("prompt_tokens")
        val completionTokens = usage.getInt("completion_tokens")
        val totalTokens = usage.getInt("total_tokens")

        // 提取finish_reason
        val finishReason = firstChoice.optString("finish_reason", "stop")

        return AIResponse(
            content = content.trim(),
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
            finishReason = finishReason,
            model = json.optString("model", currentModel)
        )
    }

    // ========== 工具方法 ==========

    /**
     * 测试API连接
     * @return 测试消息（如："Hello World"）
     */
    suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        if (!isInitialized()) {
            throw IllegalStateException("API Key未配置")
        }

        val testMessages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", "Say 'Hello World' in Chinese")
            })
        }

        val response = sendMessage(testMessages, temperature = 0.0)
        return@withContext response.content
    }

    /**
     * 估算Token数量（粗略估算）
     * 中文：1个字约等于1.5-2个Token
     * 英文：1个单词约等于1-2个Token
     *
     * 注意：这只是粗略估算，实际应使用tiktoken库
     */
    fun estimateTokens(text: String): Int {
        // 统计中文字符数
        val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
        // 统计英文单词数（简化处理）
        val englishWords = text.split("\\s+".toRegex()).size

        return (chineseChars * 1.8 + englishWords * 1.5).toInt()
    }

    /**
     * 计算API成本（美元）
     * GPT-3.5-turbo定价：
     * - Input: $0.0015 / 1K tokens
     * - Output: $0.002 / 1K tokens
     */
    fun calculateCost(promptTokens: Int, completionTokens: Int): Double {
        val inputCost = promptTokens / 1000.0 * 0.0015
        val outputCost = completionTokens / 1000.0 * 0.002
        return inputCost + outputCost
    }
}

// ========== 数据类 ==========

/**
 * AI回复响应
 */
data class AIResponse(
    val content: String,            // AI回复内容
    val promptTokens: Int,          // 输入Token数
    val completionTokens: Int,      // 输出Token数
    val totalTokens: Int,           // 总Token数
    val finishReason: String,       // 完成原因：stop/length/content_filter
    val model: String               // 使用的模型
) {
    /**
     * 计算本次调用成本（美元）
     */
    fun getCost(): Double {
        return OpenAIService.calculateCost(promptTokens, completionTokens)
    }

    /**
     * 格式化成本显示
     */
    fun getFormattedCost(): String {
        val cost = getCost()
        return String.format("$%.4f", cost)
    }
}

/**
 * OpenAI API异常
 */
class OpenAIException(
    val code: Int,
    val errorBody: String
) : Exception("OpenAI API错误 (HTTP $code): $errorBody") {

    /**
     * 获取友好的错误提示
     */
    fun getFriendlyMessage(): String {
        return when (code) {
            401 -> "API Key无效或已过期，请检查配置"
            429 -> "请求过于频繁，请稍后再试"
            500, 502, 503 -> "OpenAI服务器错误，请稍后再试"
            else -> {
                // 尝试解析错误信息
                try {
                    val json = JSONObject(errorBody)
                    val error = json.optJSONObject("error")
                    error?.optString("message") ?: "未知错误"
                } catch (e: Exception) {
                    "网络请求失败，请检查网络连接"
                }
            }
        }
    }
}