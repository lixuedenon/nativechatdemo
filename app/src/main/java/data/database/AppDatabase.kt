// app/src/main/java/com/example/nativechatdemo/data/database/AppDatabase.kt
package com.example.nativechatdemo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nativechatdemo.data.dao.ConversationDao
import com.example.nativechatdemo.data.dao.MessageDao
import com.example.nativechatdemo.data.dao.UserDao
import com.example.nativechatdemo.data.dao.CharacterDao
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.data.model.Message
import com.example.nativechatdemo.data.model.User
import com.example.nativechatdemo.data.model.Character
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Message::class,
        Conversation::class,
        Character::class  // 🔥 添加Character
    ],
    version = 2,  // 🔥 升级数据库版本
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun characterDao(): CharacterDao  // 🔥 添加CharacterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_trainer_database"
                )
                    .fallbackToDestructiveMigration()  // 🔥 允许破坏性升级
                    .addCallback(DatabaseCallback(context))  // 🔥 添加回调预填充数据
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 数据库创建回调 - 预填充角色数据
         */
        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.characterDao())
                    }
                }
            }
        }

        /**
         * 预填充角色数据
         */
        private suspend fun populateDatabase(characterDao: CharacterDao) {
            // 4个预设角色（对应Flutter版本）
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
                    id = "sunny_boy",
                    name = "阳光男生",
                    description = "他阳光开朗，积极向上，是个很好的聊天对象",
                    avatar = "sunny_boy",
                    type = "sunny",
                    gender = "male",
                    isVip = false
                )
            )

            characterDao.insertCharacters(characters)
        }
    }
}