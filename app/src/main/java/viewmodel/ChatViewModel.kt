// 文件路径：app/src/main/java/com/example/nativechatdemo/viewmodel/ChatViewModel.kt
// 文件名：ChatViewModel.kt
// 状态：✅ 完全重写 - 接入OpenAI API
// 修改说明：移除MockAIService，改用OpenAIService + PromptBuilder

package com.example.nativechatdemo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.Message
import com.example.nativechatdemo.utils.OpenAIService
import com.example.nativechatdemo.utils.PromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val messageDao = database.messageDao()
    private val conversationDao = database.conversationDao()
    private val characterDao = database.characterDao()

    // ========== 状态管理 ==========

    // 当前对话
    private val _conversation = MutableStateFlow<Conversation?>(null)
    val conversation: StateFlow<Conversation?> = _conversation

    // 消息列表
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // 当前好感度
    private val _currentFavor = MutableStateFlow(50)
    val currentFavor: StateFlow<Int> = _currentFavor

    // AI思考中状态（新增）
    private val _aiTyping = MutableStateFlow(false)
    val aiTyping: StateFlow<Boolean> = _aiTyping

    // 错误信息（新增）
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 加载状态（新增）
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 当前角色（缓存）
    private var currentCharacter: Character? = null

    companion object {
        private const val TAG = "ChatViewModel"
    }

    // ========== 初始化聊天 ==========

    /**
     * 初始化聊天（简化版，只支持基础对话模式）
     */
    fun initChat(
        userId: Long,
        character: Character,
        mode: String = "basic"
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentCharacter = character

                Log.d(TAG, "初始化聊天 - 角色: ${character.name}, 模式: $mode")

                // 创建新对话
                val newConversation = Conversation(
                    id = UUID.randomUUID().toString(),
                    userId = userId.toString(),
                    characterId = character.id.toString(),
                    characterName = character.name,
                    currentFavorability = 50,  // 初始好感度50
                    actualRounds = 0,
                    status = "active",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    moduleType = mode
                )

                // 保存对话到数据库
                withContext(Dispatchers.IO) {
                    conversationDao.insertConversation(newConversation)
                }

                _conversation.value = newConversation
                _currentFavor.value = 50

                // 生成欢迎消息
                val welcomeMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = newConversation.id,
                    sender = "ai",
                    content = generateWelcomeMessage(character),
                    timestamp = System.currentTimeMillis(),
                    type = "text"
                )

                // 保存欢迎消息
                withContext(Dispatchers.IO) {
                    messageDao.insertMessage(welcomeMessage)
                }

                _messages.value = listOf(welcomeMessage)

                Log.d(TAG, "聊天初始化成功")

            } catch (e: Exception) {
                Log.e(TAG, "初始化聊天失败", e)
                _errorMessage.value = "初始化失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== 发送消息 ==========

    /**
     * 发送消息（核心方法 - 调用OpenAI API）
     */
    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val currentConv = _conversation.value ?: run {
                    _errorMessage.value = "对话未初始化"
                    return@launch
                }

                val character = currentCharacter ?: run {
                    _errorMessage.value = "角色信息丢失"
                    return@launch
                }

                // 检查OpenAI是否初始化
                if (!OpenAIService.isInitialized()) {
                    _errorMessage.value = "OpenAI API未配置"
                    return@launch
                }

                Log.d(TAG, "发送消息: $content")

                // 1. 保存用户消息
                val userMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConv.id,
                    sender = "user",
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    type = "text"
                )

                withContext(Dispatchers.IO) {
                    messageDao.insertMessage(userMessage)
                }

                _messages.value = _messages.value + userMessage

                // 2. 显示AI思考中
                _aiTyping.value = true
                delay(500)  // 模拟思考延迟

                // 3. 调用OpenAI API
                val currentRound = currentConv.actualRounds + 1
                val currentMessages = _messages.value

                val aiResponse = callOpenAI(
                    character = character,
                    messages = currentMessages,
                    conversationRound = currentRound,
                    currentFavor = currentConv.currentFavorability
                )

                // 4. 解析AI回复
                val (aiContent, favorChange) = parseAIResponse(aiResponse.content)

                // 5. 计算新好感度
                val newFavor = (currentConv.currentFavorability + favorChange).coerceIn(0, 100)

                // 6. 保存AI消息
                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConv.id,
                    sender = "ai",
                    content = aiContent,
                    timestamp = System.currentTimeMillis(),
                    type = "text"
                )

                withContext(Dispatchers.IO) {
                    messageDao.insertMessage(aiMessage)
                }

                _messages.value = _messages.value + aiMessage

                // 7. 更新对话状态
                val updatedConv = currentConv.copy(
                    currentFavorability = newFavor,
                    actualRounds = currentRound,
                    updatedAt = System.currentTimeMillis(),
                    totalTokens = currentConv.totalTokens + aiResponse.totalTokens
                )

                withContext(Dispatchers.IO) {
                    conversationDao.updateConversation(updatedConv)
                }

                _conversation.value = updatedConv
                _currentFavor.value = newFavor

                Log.d(TAG, "消息发送成功 - 轮数: $currentRound, 好感度: $newFavor (${if(favorChange>0) "+" else ""}$favorChange)")

            } catch (e: com.example.nativechatdemo.utils.OpenAIException) {
                Log.e(TAG, "OpenAI API错误", e)
                _errorMessage.value = e.getFriendlyMessage()
            } catch (e: Exception) {
                Log.e(TAG, "发送消息失败", e)
                _errorMessage.value = "发送失败：${e.message}"
            } finally {
                _aiTyping.value = false
            }
        }
    }

    /**
     * 发送带引用的消息
     */
    fun sendMessageWithQuote(
        content: String,
        quotedMessageId: String,
        quotedContent: String,
        quotedSender: String
    ) {
        viewModelScope.launch {
            try {
                val currentConv = _conversation.value ?: return@launch
                val character = currentCharacter ?: return@launch

                if (!OpenAIService.isInitialized()) {
                    _errorMessage.value = "OpenAI API未配置"
                    return@launch
                }

                // 保存带引用的用户消息
                val userMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConv.id,
                    sender = "user",
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    type = "text",
                    quotedMessageId = quotedMessageId,
                    quotedContent = quotedContent,
                    quotedSender = quotedSender
                )

                withContext(Dispatchers.IO) {
                    messageDao.insertMessage(userMessage)
                }

                _messages.value = _messages.value + userMessage
                _aiTyping.value = true
                delay(500)

                val currentRound = currentConv.actualRounds + 1
                val currentMessages = _messages.value

                val aiResponse = callOpenAI(
                    character = character,
                    messages = currentMessages,
                    conversationRound = currentRound,
                    currentFavor = currentConv.currentFavorability
                )

                val (aiContent, favorChange) = parseAIResponse(aiResponse.content)
                val newFavor = (currentConv.currentFavorability + favorChange).coerceIn(0, 100)

                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = currentConv.id,
                    sender = "ai",
                    content = aiContent,
                    timestamp = System.currentTimeMillis(),
                    type = "text"
                )

                withContext(Dispatchers.IO) {
                    messageDao.insertMessage(aiMessage)
                }

                _messages.value = _messages.value + aiMessage

                val updatedConv = currentConv.copy(
                    currentFavorability = newFavor,
                    actualRounds = currentRound,
                    updatedAt = System.currentTimeMillis(),
                    totalTokens = currentConv.totalTokens + aiResponse.totalTokens
                )

                withContext(Dispatchers.IO) {
                    conversationDao.updateConversation(updatedConv)
                }

                _conversation.value = updatedConv
                _currentFavor.value = newFavor

            } catch (e: Exception) {
                Log.e(TAG, "发送引用消息失败", e)
                _errorMessage.value = "发送失败：${e.message}"
            } finally {
                _aiTyping.value = false
            }
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 调用OpenAI API
     */
    private suspend fun callOpenAI(
        character: Character,
        messages: List<Message>,
        conversationRound: Int,
        currentFavor: Int
    ): com.example.nativechatdemo.utils.AIResponse = withContext(Dispatchers.IO) {

        // 构建messages数组（使用PromptBuilder）
        val messagesArray = PromptBuilder.buildMessages(
            character = character,
            messages = messages,
            conversationRound = conversationRound,
            currentFavor = currentFavor,
            maxHistoryMessages = 20  // 最多保留20条历史消息
        )

        // 调用OpenAI API
        OpenAIService.sendMessage(
            messages = messagesArray,
            temperature = 0.8,  // 稍高的随机性，让回复更自然
            maxTokens = 500     // 限制回复长度
        )
    }

    /**
     * 解析AI回复（提取回复内容和好感度变化）
     *
     * AI回复格式：
     * {
     *   "reply": "实际回复内容",
     *   "favor_change": 3,
     *   "favor_reason": "原因"
     * }
     */
    private fun parseAIResponse(content: String): Pair<String, Int> {
        return try {
            // 尝试解析JSON格式
            val json = JSONObject(content)
            val reply = json.optString("reply", content)
            val favorChange = json.optInt("favor_change", 0)

            Pair(reply, favorChange)
        } catch (e: Exception) {
            // 如果不是JSON格式，直接返回原内容，好感度变化为0
            Log.w(TAG, "AI回复不是JSON格式，使用原始内容", e)
            Pair(content, 0)
        }
    }

    /**
     * 生成欢迎消息
     */
    private fun generateWelcomeMessage(character: Character): String {
        val traits = character.getTraits()

        return when (traits.personalityType) {
            com.example.nativechatdemo.data.model.PersonalityType.CUTE_SOFT ->
                "嗨~ 很高兴认识你呢 😊"
            com.example.nativechatdemo.data.model.PersonalityType.LIVELY_CHEERFUL ->
                "哈哈你好呀！终于等到你了！✨"
            com.example.nativechatdemo.data.model.PersonalityType.MATURE_GENTLE ->
                "你好，很高兴认识你 😊"
            com.example.nativechatdemo.data.model.PersonalityType.COOL_ELEGANT ->
                "你好。"
            com.example.nativechatdemo.data.model.PersonalityType.STRAIGHTFORWARD ->
                "嘿！什么事？"
            com.example.nativechatdemo.data.model.PersonalityType.LITERARY_INTROVERTED ->
                "嗯...你好"
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}