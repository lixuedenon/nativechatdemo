// 文件路径：app/src/main/java/com/example/nativechatdemo/viewmodel/ChatViewModel.kt
// 文件类型：Kotlin Class (ViewModel)
// 修改内容：在所有创建Message的地方添加characterCount参数

package com.example.nativechatdemo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.FavorPoint
import com.example.nativechatdemo.data.model.Message
import com.example.nativechatdemo.utils.MockAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val messageDao = database.messageDao()
    private val conversationDao = database.conversationDao()

    private val _conversation = MutableStateFlow<Conversation?>(null)
    val conversation: StateFlow<Conversation?> = _conversation

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _favorPoints = MutableStateFlow<List<FavorPoint>>(emptyList())
    val favorPoints: StateFlow<List<FavorPoint>> = _favorPoints

    private var userId: String = ""
    private var character: Character? = null
    private var replayMode: String? = null
    private var originalConversationId: String? = null
    private var originalMessages: List<Message> = emptyList()

    fun initChat(
        userId: String,
        character: Character,
        replayMode: String? = null,
        originalConversationId: String? = null
    ) {
        this.userId = userId
        this.character = character
        this.replayMode = replayMode
        this.originalConversationId = originalConversationId

        Log.d("ChatViewModel", "initChat - replayMode: $replayMode, originalConversationId: $originalConversationId")

        viewModelScope.launch {
            if (replayMode != null && originalConversationId != null) {
                originalMessages = withContext(Dispatchers.IO) {
                    messageDao.getMessagesByConversationId(originalConversationId)
                }
                Log.d("ChatViewModel", "加载原对话消息数: ${originalMessages.size}")
            }

            val newConversation = Conversation(
                id = UUID.randomUUID().toString(),
                userId = userId,
                characterId = character.id,
                characterName = character.name,
                currentFavorability = 50,
                actualRounds = 0,
                status = "active",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                moduleType = "basic",
                reviewMode = replayMode,
                originalConversationId = originalConversationId
            )

            withContext(Dispatchers.IO) {
                conversationDao.insertConversation(newConversation)
            }

            _conversation.value = newConversation

            val welcomeContent = generateWelcomeMessage(character.name)
            val welcomeMessage = Message(
                id = UUID.randomUUID().toString(),
                conversationId = newConversation.id,
                content = welcomeContent,
                isUser = false,
                timestamp = System.currentTimeMillis(),
                characterCount = welcomeContent.length,  // 🔥 添加
                favorChange = null
            )

            withContext(Dispatchers.IO) {
                messageDao.insertMessage(welcomeMessage)
            }

            _messages.value = listOf(welcomeMessage)

            val initialPoint = FavorPoint(
                round = 0,
                favor = 50,
                messageId = welcomeMessage.id,
                reason = "",
                timestamp = System.currentTimeMillis(),
                favorChange = 0
            )
            _favorPoints.value = listOf(initialPoint)
        }
    }

    private fun generateWelcomeMessage(name: String): String {
        return when {
            name.contains("温柔") -> "你好~很高兴认识你呢"
            name.contains("活泼") -> "嗨！终于等到你啦！"
            name.contains("优雅") || name.contains("高冷") -> "你好，认识你很高兴。"
            name.contains("阳光") -> "嘿！你好呀~"
            else -> "你好~"
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val currentConv = _conversation.value ?: return@launch
            val currentRound = currentConv.actualRounds

            Log.d("ChatViewModel", "发送消息，当前轮数: $currentRound")

            if (currentRound >= 45) {
                Log.w("ChatViewModel", "已达轮数上限45轮")
                return@launch
            }

            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                conversationId = currentConv.id,
                content = content,
                isUser = true,
                timestamp = System.currentTimeMillis(),
                characterCount = content.length,  // 🔥 添加
                favorChange = null
            )

            withContext(Dispatchers.IO) {
                messageDao.insertMessage(userMessage)
            }

            _messages.value = _messages.value + userMessage

            delay(800)

            val currentMessages = _messages.value

            val aiResponse = if (replayMode != null) {
                MockAIService.generateReplayResponse(
                    userInput = content,
                    characterId = currentConv.characterId,
                    currentRound = currentRound + 1,
                    conversationHistory = currentMessages,
                    currentFavorability = currentConv.currentFavorability,
                    replayMode = replayMode!!,
                    originalMessages = originalMessages,
                    currentRoundIndex = currentRound
                )
            } else {
                MockAIService.generateResponse(
                    userInput = content,
                    characterId = currentConv.characterId,
                    currentRound = currentRound + 1,
                    conversationHistory = currentMessages,
                    currentFavorability = currentConv.currentFavorability
                )
            }

            val favorChange = aiResponse.favorabilityChange
            val newFavorability = (currentConv.currentFavorability + favorChange).coerceIn(0, 100)

            val aiMessage = Message(
                id = UUID.randomUUID().toString(),
                conversationId = currentConv.id,
                content = aiResponse.message,
                isUser = false,
                timestamp = System.currentTimeMillis(),
                characterCount = aiResponse.message.length,  // 🔥 添加
                favorChange = favorChange
            )

            withContext(Dispatchers.IO) {
                messageDao.insertMessage(aiMessage)
            }

            _messages.value = _messages.value + aiMessage

            val newRound = currentRound + 1
            val reason = extractReasonFromMessage(aiResponse.message)

            val newPoint = FavorPoint(
                round = newRound,
                favor = newFavorability,
                messageId = aiMessage.id,
                reason = reason,
                timestamp = System.currentTimeMillis(),
                favorChange = favorChange
            )

            _favorPoints.value = _favorPoints.value + newPoint

            val updatedConversation = currentConv.copy(
                currentFavorability = newFavorability,
                actualRounds = newRound,
                updatedAt = System.currentTimeMillis(),
                favorPoints = convertFavorPointsToJson(_favorPoints.value)
            )

            withContext(Dispatchers.IO) {
                conversationDao.updateConversation(updatedConversation)
            }

            _conversation.value = updatedConversation

            Log.d("ChatViewModel", "消息发送完成，当前轮数: $newRound, 好感度: $newFavorability")
        }
    }

    private fun extractReasonFromMessage(message: String): String {
        val regex = """\[FAVOR[_PEAK]*:[+\-]?\d+:(.*?)]""".toRegex()
        val matchResult = regex.find(message)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    private fun convertFavorPointsToJson(points: List<FavorPoint>): String {
        val jsonArray = JSONArray()
        points.forEach { point ->
            val jsonObject = org.json.JSONObject()
            jsonObject.put("round", point.round)
            jsonObject.put("favor", point.favor)
            jsonObject.put("messageId", point.messageId)
            jsonObject.put("reason", point.reason)
            jsonObject.put("timestamp", point.timestamp)
            jsonObject.put("favorChange", point.favorChange)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}