// 文件路径：app/src/main/java/com/example/nativechatdemo/viewmodel/ChatViewModel.kt
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
import com.example.nativechatdemo.data.model.User
import com.example.nativechatdemo.utils.FavorAnalyzer
import com.example.nativechatdemo.utils.MockAIService
import com.example.nativechatdemo.utils.TextAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val messageDao = database.messageDao()
    private val conversationDao = database.conversationDao()
    private val userDao = database.userDao()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _conversation = MutableStateFlow<Conversation?>(null)
    val conversation: StateFlow<Conversation?> = _conversation.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _favorPoints = MutableStateFlow<List<FavorPoint>>(emptyList())
    val favorPoints: StateFlow<List<FavorPoint>> = _favorPoints.asStateFlow()

    private var currentCharacter: Character? = null

    fun initChat(userId: String, character: Character) {
        currentCharacter = character

        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserById(userId)
                } ?: User(
                    id = userId,
                    username = "测试用户",
                    credits = 100,
                    userLevel = 1,
                    createdAt = System.currentTimeMillis()
                )
                _user.value = user

                val conversation = Conversation(
                    id = "conv_${System.currentTimeMillis()}",
                    userId = userId,
                    characterId = character.id,
                    characterName = character.name,
                    currentFavorability = 10,
                    actualRounds = 0,
                    status = "active",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    try {
                        conversationDao.insertConversation(conversation)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _conversation.value = conversation

                val initialPoint = FavorPoint(
                    round = 0,
                    favor = 10,
                    messageId = "",
                    reason = "",
                    timestamp = System.currentTimeMillis(),
                    favorChange = 0
                )
                _favorPoints.value = listOf(initialPoint)

            } catch (e: Exception) {
                e.printStackTrace()

                _user.value = User(
                    id = userId,
                    username = "测试用户",
                    credits = 100,
                    userLevel = 1,
                    createdAt = System.currentTimeMillis()
                )

                _conversation.value = Conversation(
                    id = "conv_${System.currentTimeMillis()}",
                    userId = userId,
                    characterId = character.id,
                    characterName = character.name,
                    currentFavorability = 10,
                    actualRounds = 0,
                    status = "active",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val initialPoint = FavorPoint(
                    round = 0,
                    favor = 10,
                    messageId = "",
                    reason = "",
                    timestamp = System.currentTimeMillis(),
                    favorChange = 0
                )
                _favorPoints.value = listOf(initialPoint)
            }
        }
    }

    fun sendMessage(content: String) {
        val currentConv = _conversation.value ?: return
        val currentUser = _user.value ?: return
        val character = currentCharacter ?: return

        if (currentUser.credits <= 0) {
            return
        }

        viewModelScope.launch {
            try {
                val userMessage = Message(
                    id = "msg_${System.currentTimeMillis()}",
                    conversationId = currentConv.id,
                    content = content,
                    isUser = true,
                    timestamp = System.currentTimeMillis(),
                    characterCount = content.length
                )

                _messages.value = _messages.value + userMessage

                withContext(Dispatchers.IO) {
                    try {
                        messageDao.insertMessage(userMessage)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _isTyping.value = true

                val updatedUser = currentUser.copy(credits = currentUser.credits - 1)

                withContext(Dispatchers.IO) {
                    try {
                        userDao.updateUser(updatedUser)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                _user.value = updatedUser

                val aiResponseRaw = withContext(Dispatchers.IO) {
                    MockAIService.generateResponse(
                        userInput = content,
                        characterId = character.id,
                        currentRound = currentConv.actualRounds + 1,
                        conversationHistory = _messages.value,
                        currentFavorability = currentConv.currentFavorability
                    )
                }

                val favorChange = FavorAnalyzer.parse(aiResponseRaw.message)
                val cleanMessage = FavorAnalyzer.extractCleanMessage(aiResponseRaw.message)

                Log.d("ChatViewModel", "AI原始回复: ${aiResponseRaw.message}")
                Log.d("ChatViewModel", "解析好感变化: $favorChange")
                Log.d("ChatViewModel", "清理后消息: $cleanMessage")

                val aiMessage = Message(
                    id = "msg_${System.currentTimeMillis() + 1}",
                    conversationId = currentConv.id,
                    content = cleanMessage,
                    isUser = false,
                    timestamp = aiResponseRaw.responseTime,
                    characterCount = cleanMessage.length,
                    favorChange = favorChange?.value
                )

                _messages.value = _messages.value + aiMessage

                withContext(Dispatchers.IO) {
                    try {
                        messageDao.insertMessage(aiMessage)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val newFavorability = (currentConv.currentFavorability + (favorChange?.value ?: aiResponseRaw.favorabilityChange)).coerceIn(0, 100)
                val newRounds = currentConv.actualRounds + 1

                // 修改：每轮都添加柱子
                val newPoint = FavorPoint(
                    round = newRounds,
                    favor = newFavorability,
                    messageId = aiMessage.id,
                    reason = if (favorChange != null && favorChange.isPeak) favorChange.reason else "",
                    timestamp = System.currentTimeMillis(),
                    favorChange = favorChange?.value ?: 0
                )
                _favorPoints.value = _favorPoints.value + newPoint

                Log.d("ChatViewModel", "添加好感点: round=$newRounds, favor=$newFavorability, change=${favorChange?.value ?: 0}, reason=${newPoint.reason}")

                // 保存好感线数据到数据库
                val favorPointsJson = JSONArray().apply {
                    _favorPoints.value.forEach { point ->
                        put(JSONObject().apply {
                            put("round", point.round)
                            put("favor", point.favor)
                            put("messageId", point.messageId)
                            put("reason", point.reason)
                            put("timestamp", point.timestamp)
                            put("favorChange", point.favorChange)
                        })
                    }
                }.toString()

                val updatedConv = currentConv.copy(
                    currentFavorability = newFavorability,
                    actualRounds = newRounds,
                    updatedAt = System.currentTimeMillis(),
                    favorPoints = favorPointsJson
                )

                withContext(Dispatchers.IO) {
                    try {
                        conversationDao.updateConversation(updatedConv)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _conversation.value = updatedConv

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun getEffectiveRounds(): Int {
        return 45
    }
}