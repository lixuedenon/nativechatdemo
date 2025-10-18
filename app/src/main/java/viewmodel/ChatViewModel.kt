// 文件路径：app/src/main/java/com/example/nativechatdemo/viewmodel/ChatViewModel.kt
// 文件类型：Kotlin Class (ViewModel)
// 修改内容：修复续命逻辑，防止事件重复触发导致崩溃

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
import com.example.nativechatdemo.utils.TrainingStoryConfig
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

    // 养成模式结束事件
    private val _trainingEndingEvent = MutableStateFlow<TrainingEndingEvent?>(null)
    val trainingEndingEvent: StateFlow<TrainingEndingEvent?> = _trainingEndingEvent

    private var userId: String = ""
    private var character: Character? = null
    private var replayMode: String? = null
    private var originalConversationId: String? = null
    private var originalMessages: List<Message> = emptyList()
    private var moduleType: String = "basic"

    companion object {
        private const val TAG = "ChatViewModel"
    }

    data class TrainingEndingEvent(
        val type: String  // "revive" 或 "final"
    )

    fun initChat(
        userId: String,
        character: Character,
        replayMode: String? = null,
        originalConversationId: String? = null,
        moduleType: String = "basic"
    ) {
        this.userId = userId
        this.character = character
        this.replayMode = replayMode
        this.originalConversationId = originalConversationId
        this.moduleType = moduleType

        Log.d(TAG, "initChat - moduleType: $moduleType, replayMode: $replayMode")

        viewModelScope.launch {
            if (replayMode != null && originalConversationId != null) {
                originalMessages = withContext(Dispatchers.IO) {
                    messageDao.getMessagesByConversationId(originalConversationId)
                }
                Log.d(TAG, "加载原对话消息数: ${originalMessages.size}")
            }

            val isTraining = moduleType == "training"
            val endingType = if (isTraining) {
                TrainingStoryConfig.getRandomEndingType().name.lowercase()
            } else {
                null
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
                moduleType = moduleType,
                reviewMode = replayMode,
                originalConversationId = originalConversationId,
                isTrainingMode = isTraining,
                trainingEndingType = endingType,
                reviveCount = 0,
                totalTrainingRounds = 0
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
                characterCount = welcomeContent.length,
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

            Log.d(TAG, "发送消息，当前轮数: $currentRound, 模块类型: ${currentConv.moduleType}")

            // 基础对话模式：检查45轮上限
            if (!currentConv.isTrainingMode && currentRound >= 45) {
                Log.w(TAG, "已达轮数上限45轮")
                return@launch
            }

            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                conversationId = currentConv.id,
                content = content,
                isUser = true,
                timestamp = System.currentTimeMillis(),
                characterCount = content.length,
                favorChange = null
            )

            withContext(Dispatchers.IO) {
                messageDao.insertMessage(userMessage)
            }

            _messages.value = _messages.value + userMessage

            delay(800)

            val currentMessages = _messages.value

            // 生成AI回复（养成模式需要特殊处理）
            val aiResponse = if (currentConv.isTrainingMode) {
                generateTrainingModeResponse(currentConv, content, currentMessages, currentRound + 1)
            } else if (replayMode != null) {
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
                characterCount = aiResponse.message.length,
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
                favorPoints = convertFavorPointsToJson(_favorPoints.value),
                totalTrainingRounds = if (currentConv.isTrainingMode) newRound else 0
            )

            withContext(Dispatchers.IO) {
                conversationDao.updateConversation(updatedConversation)
            }

            _conversation.value = updatedConversation

            // 养成模式：检查是否触发结束
            if (currentConv.isTrainingMode) {
                checkTrainingEnding(updatedConversation)
            }

            Log.d(TAG, "消息发送完成，当前轮数: $newRound, 好感度: $newFavorability")
        }
    }

    /**
     * 生成养成模式的AI回复
     */
    private fun generateTrainingModeResponse(
        conversation: Conversation,
        userInput: String,
        history: List<Message>,
        nextRound: Int
    ): com.example.nativechatdemo.data.model.AIResponse {
        val shouldIntroduceEnding = shouldIntroduceEnding(nextRound)

        // 如果到了引入结束话题的轮次
        if (shouldIntroduceEnding) {
            return generateEndingIntroResponse(conversation, nextRound)
        }

        // 否则正常生成回复
        return MockAIService.generateResponse(
            userInput = userInput,
            characterId = conversation.characterId,
            currentRound = nextRound,
            conversationHistory = history,
            currentFavorability = conversation.currentFavorability
        )
    }

    /**
     * 判断是否应该引入结束话题
     */
    private fun shouldIntroduceEnding(rounds: Int): Boolean {
        return rounds == 20 || rounds == 45 || rounds == 70 || rounds == 95
    }

    /**
     * 生成引入结束话题的回复
     */
    private fun generateEndingIntroResponse(
        conversation: Conversation,
        rounds: Int
    ): com.example.nativechatdemo.data.model.AIResponse {
        val endingType = TrainingStoryConfig.EndingType.valueOf(conversation.trainingEndingType?.uppercase() ?: "SICK")

        val message = when (endingType) {
            TrainingStoryConfig.EndingType.SICK -> {
                when (rounds) {
                    20 -> "对了...其实我最近身体不太舒服，去医院检查了一下... [FAVOR:+3:关心的话题]"
                    45 -> "医生说我的病情有点严重...可能需要很长时间治疗... [FAVOR:+2:沉重的话题]"
                    70 -> "我真的很珍惜和你在一起的时光...虽然不知道还能陪你多久... [FAVOR:+5:真挚的情感]"
                    95 -> "我可能...真的撑不了太久了...但能认识你，是我最幸运的事... [FAVOR:+8:深情告白]"
                    else -> "嗯... [FAVOR:+1:]"
                }
            }
            TrainingStoryConfig.EndingType.TIMETRAVEL -> {
                when (rounds) {
                    20 -> "我有件事一直没告诉你...其实我不属于这个时空... [FAVOR:+3:神秘的秘密]"
                    45 -> "时空裂缝开始出现了...我可能随时会被召回原来的世界... [FAVOR:+2:担忧的预感]"
                    70 -> "每次和你在一起，我都在和命运抗争...但我不想离开你... [FAVOR:+5:坚定的决心]"
                    95 -> "时空裂缝越来越大了...我真的要回去了...但我永远不会忘记你... [FAVOR:+8:不舍的告别]"
                    else -> "嗯... [FAVOR:+1:]"
                }
            }
        }

        return com.example.nativechatdemo.data.model.AIResponse(
            message = message,
            favorabilityChange = if (rounds >= 90) 8 else if (rounds >= 60) 5 else 3,
            responseTime = System.currentTimeMillis()
        )
    }

    /**
     * 检查养成模式是否触发结束
     * ✅ 修复：防止事件重复触发
     */
    private fun checkTrainingEnding(conversation: Conversation) {
        val rounds = conversation.actualRounds

        // 每25轮触发一次结束判断
        if (rounds % 25 == 0) {
            // ✅ 关键修复：先检查当前是否已经有事件在处理
            if (_trainingEndingEvent.value != null) {
                Log.d(TAG, "已有结束事件在处理中，跳过")
                return
            }

            if (conversation.reviveCount < 3) {
                // 还有续命机会
                Log.d(TAG, "触发续命窗口，当前续命次数: ${conversation.reviveCount}")
                _trainingEndingEvent.value = TrainingEndingEvent("revive")
            } else {
                // 已经续命3次，强制结束
                Log.d(TAG, "已续命3次，强制结束")
                _trainingEndingEvent.value = TrainingEndingEvent("final")
            }

            // ✅ 不要立即重置事件，等UI处理完再重置
        }
    }

    /**
     * 更新续命次数
     * ✅ 修复：更新后立即重置事件
     */
    fun updateReviveCount(newCount: Int) {
        viewModelScope.launch {
            val currentConv = _conversation.value ?: return@launch

            Log.d(TAG, "更新续命次数: ${currentConv.reviveCount} -> $newCount")

            val updatedConv = currentConv.copy(
                reviveCount = newCount,
                updatedAt = System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                conversationDao.updateConversation(updatedConv)
            }

            _conversation.value = updatedConv

            // ✅ 关键修复：更新完续命次数后，立即重置事件
            _trainingEndingEvent.value = null

            Log.d(TAG, "续命次数已更新: $newCount，事件已重置")
        }
    }

    /**
     * ✅ 新增：重置训练事件（供UI调用）
     */
    fun resetTrainingEvent() {
        _trainingEndingEvent.value = null
        Log.d(TAG, "训练事件已手动重置")
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