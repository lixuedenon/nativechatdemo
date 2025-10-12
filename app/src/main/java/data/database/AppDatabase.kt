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
import com.example.nativechatdemo.data.dao.RadarScenarioDao
import com.example.nativechatdemo.data.dao.RadarProgressDao
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.Message
import com.example.nativechatdemo.data.model.User
import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.data.model.ConversationAnalysis
import com.example.nativechatdemo.data.model.RadarScenario
import com.example.nativechatdemo.data.model.RadarProgress
import com.example.nativechatdemo.utils.MockRadarService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Message::class,
        Conversation::class,
        Character::class,
        ConversationAnalysis::class,
        RadarScenario::class,
        RadarProgress::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun characterDao(): CharacterDao
    abstract fun conversationAnalysisDao(): ConversationAnalysisDao
    abstract fun radarScenarioDao(): RadarScenarioDao
    abstract fun radarProgressDao(): RadarProgressDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Volatile
        private var isInitializing = false

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

                CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    checkAndInsertData(newInstance)
                }

                newInstance
            }
        }

        private suspend fun checkAndInsertData(database: AppDatabase) {
            if (isInitializing) {
                Log.d(TAG, "已经在初始化中，跳过")
                return
            }

            isInitializing = true

            try {
                Log.d(TAG, "========== 开始检查场景数据 ==========")

                val radarDao = database.radarScenarioDao()
                val femaleLearnScenarios = radarDao.getScenariosByTypeAndGender("learn", "female")
                val maleLearnScenarios = radarDao.getScenariosByTypeAndGender("learn", "male")
                val femalePracticeScenarios = radarDao.getScenariosByTypeAndGender("practice", "female")
                val malePracticeScenarios = radarDao.getScenariosByTypeAndGender("practice", "male")

                Log.d(TAG, "女生学习场景: " + femaleLearnScenarios.size)
                Log.d(TAG, "男生学习场景: " + maleLearnScenarios.size)
                Log.d(TAG, "女生练习场景: " + femalePracticeScenarios.size)
                Log.d(TAG, "男生练习场景: " + malePracticeScenarios.size)

                val totalScenarios = femaleLearnScenarios.size + maleLearnScenarios.size +
                                   femalePracticeScenarios.size + malePracticeScenarios.size

                if (totalScenarios == 0) {
                    Log.w(TAG, "⚠️ 场景数据为空，开始生成和插入...")

                    val scenarios = MockRadarService.generateAllScenarios()
                    Log.d(TAG, "生成的场景总数: " + scenarios.size)

                    if (scenarios.isEmpty()) {
                        Log.e(TAG, "❌ MockRadarService 返回空列表！")
                    } else {
                        scenarios.forEachIndexed { index, scenario ->
                            Log.d(TAG, "场景[" + index + "]: id=" + scenario.id + ", type=" + scenario.type + ", gender=" + scenario.targetGender)
                        }

                        Log.d(TAG, "开始插入场景数据...")
                        radarDao.insertScenarios(scenarios)
                        Log.d(TAG, "✅ 场景数据插入完成")

                        delay(200)
                        val verifyCount = radarDao.getScenariosByTypeAndGender("learn", "female").size +
                                        radarDao.getScenariosByTypeAndGender("learn", "male").size +
                                        radarDao.getScenariosByTypeAndGender("practice", "female").size +
                                        radarDao.getScenariosByTypeAndGender("practice", "male").size
                        Log.d(TAG, "验证插入结果 - 总场景数: " + verifyCount)
                    }
                } else {
                    Log.d(TAG, "✅ 场景数据已存在，总数: " + totalScenarios)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 检查数据失败", e)
                e.printStackTrace()
            } finally {
                isInitializing = false
                Log.d(TAG, "========== 数据检查完成 ==========")
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
                                database.radarScenarioDao()
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
            }
        }

        private suspend fun populateDatabase(
            characterDao: CharacterDao,
            radarScenarioDao: RadarScenarioDao
        ) {
            Log.d(TAG, "========== populateDatabase 开始 ==========")

            try {
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
                Log.d(TAG, "✅ 角色插入完成，数量: " + characters.size)

                Log.d(TAG, "开始生成场景...")
                val radarScenarios = MockRadarService.generateAllScenarios()
                Log.d(TAG, "生成场景数量: " + radarScenarios.size)

                if (radarScenarios.isEmpty()) {
                    Log.e(TAG, "❌ 警告：MockRadarService 返回空列表！")
                } else {
                    Log.d(TAG, "开始插入场景...")
                    radarScenarioDao.insertScenarios(radarScenarios)
                    Log.d(TAG, "✅ 场景插入完成")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ populateDatabase 异常", e)
                e.printStackTrace()
            }

            Log.d(TAG, "========== populateDatabase 结束 ==========")
        }
    }
}