// 文件路径：app/src/main/java/com/example/nativechatdemo/data/database/AppDatabase.kt
// 文件类型：Kotlin Abstract Class (Room Database)

package com.example.nativechatdemo.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nativechatdemo.data.dao.ConversationDao
import com.example.nativechatdemo.data.dao.MessageDao
import com.example.nativechatdemo.data.dao.UserDao
import com.example.nativechatdemo.data.dao.CharacterDao
import com.example.nativechatdemo.data.dao.ConversationAnalysisDao
import com.example.nativechatdemo.data.dao.ConversationScenarioDao
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.Message
import com.example.nativechatdemo.data.model.User
import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.data.model.ConversationAnalysis
import com.example.nativechatdemo.data.model.ConversationScenario
import com.example.nativechatdemo.utils.MockConversationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Message::class,
        Conversation::class,
        Character::class,
        ConversationAnalysis::class,
        ConversationScenario::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun characterDao(): CharacterDao
    abstract fun conversationAnalysisDao(): ConversationAnalysisDao
    abstract fun conversationScenarioDao(): ConversationScenarioDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            Log.d(TAG, "========== getDatabase 被调用 ==========")

            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE
                if (instance != null) {
                    Log.d(TAG, "返回已有实例")
                    return instance
                }

                Log.d(TAG, "创建新的数据库实例...")

                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_trainer_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()

                INSTANCE = newInstance
                Log.d(TAG, "✅ 数据库实例创建完成")

                newInstance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(TAG, "🔥🔥🔥 数据库 onCreate 被调用！")

                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            populateDatabase(
                                database.characterDao(),
                                database.conversationScenarioDao()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ populateDatabase 失败", e)
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "数据库 onOpen 被调用")

                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            checkAndRepairData(database)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ checkAndRepairData 失败", e)
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        private suspend fun checkAndRepairData(database: AppDatabase) {
            Log.d(TAG, "========== checkAndRepairData 开始 ==========")

            try {
                val characterDao = database.characterDao()
                val scenarioDao = database.conversationScenarioDao()

                val charactersCount = characterDao.getCount()
                val scenariosCount = scenarioDao.getCount()

                Log.d(TAG, "现有角色数: $charactersCount")
                Log.d(TAG, "现有场景数: $scenariosCount")

                if (charactersCount == 0) {
                    Log.w(TAG, "⚠️ 角色数据缺失，重新插入...")
                    insertCharacters(characterDao)
                } else {
                    Log.d(TAG, "✅ 角色数据完整")
                }

                if (scenariosCount == 0) {
                    Log.w(TAG, "⚠️ 场景数据缺失，重新插入...")
                    insertScenarios(scenarioDao)
                } else {
                    Log.d(TAG, "✅ 场景数据完整")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 数据检查失败", e)
                e.printStackTrace()
            }

            Log.d(TAG, "========== checkAndRepairData 结束 ==========")
        }

        private suspend fun populateDatabase(
            characterDao: CharacterDao,
            conversationScenarioDao: ConversationScenarioDao
        ) {
            Log.d(TAG, "========== populateDatabase 开始 ==========")

            try {
                insertCharacters(characterDao)
                insertScenarios(conversationScenarioDao)
            } catch (e: Exception) {
                Log.e(TAG, "❌ populateDatabase 异常", e)
                e.printStackTrace()
            }

            Log.d(TAG, "========== populateDatabase 结束 ==========")
        }

        private suspend fun insertCharacters(characterDao: CharacterDao) {
            val characters = listOf(
                Character(
                    id = "gentle_girl",
                    name = "温柔女生",
                    description = "她温柔体贴，善解人意，喜欢倾听你的心声",
                    avatar = "gentle_girl",
                    type = "gentle",
                    gender = "female",
                    isVip = false
                ),
                Character(
                    id = "lively_girl",
                    name = "活泼女生",
                    description = "她活泼开朗，充满活力，和她聊天总是很有趣",
                    avatar = "lively_girl",
                    type = "lively",
                    gender = "female",
                    isVip = false
                ),
                Character(
                    id = "elegant_girl",
                    name = "优雅女生",
                    description = "她优雅知性，气质出众，喜欢有深度的交流",
                    avatar = "elegant_girl",
                    type = "elegant",
                    gender = "female",
                    isVip = false
                ),
                Character(
                    id = "sunny_girl",
                    name = "阳光女生",
                    description = "她阳光开朗，充满正能量，是个很好的聊天对象",
                    avatar = "sunny_girl",
                    type = "sunny",
                    gender = "female",
                    isVip = false
                ),
                Character(
                    id = "gentle_boy",
                    name = "温柔男生",
                    description = "他温柔体贴，善解人意，是个暖男",
                    avatar = "gentle_boy",
                    type = "gentle",
                    gender = "male",
                    isVip = false
                ),
                Character(
                    id = "lively_boy",
                    name = "活泼男生",
                    description = "他活泼开朗，幽默风趣，和他聊天很开心",
                    avatar = "lively_boy",
                    type = "lively",
                    gender = "male",
                    isVip = false
                ),
                Character(
                    id = "elegant_boy",
                    name = "高冷男生",
                    description = "他神秘高冷，不易接近，但很有魅力",
                    avatar = "elegant_boy",
                    type = "elegant",
                    gender = "male",
                    isVip = false
                ),
                Character(
                    id = "sunny_boy",
                    name = "阳光男生",
                    description = "他阳光积极，充满正能量，很有感染力",
                    avatar = "sunny_boy",
                    type = "sunny",
                    gender = "male",
                    isVip = false
                )
            )

            Log.d(TAG, "开始插入角色...")
            characterDao.insertCharacters(characters)
            Log.d(TAG, "✅ 角色插入完成，数量: ${characters.size}")
        }

        private suspend fun insertScenarios(scenarioDao: ConversationScenarioDao) {
            Log.d(TAG, "开始生成对话场景...")
            val conversationScenarios = MockConversationService.generateAllScenarios()
            Log.d(TAG, "生成对话场景数量: ${conversationScenarios.size}")

            if (conversationScenarios.isEmpty()) {
                Log.e(TAG, "❌ 警告：MockConversationService 返回空列表！")
            } else {
                Log.d(TAG, "开始插入对话场景...")
                scenarioDao.insertScenarios(conversationScenarios)
                Log.d(TAG, "✅ 对话场景插入完成")
            }
        }
    }
}