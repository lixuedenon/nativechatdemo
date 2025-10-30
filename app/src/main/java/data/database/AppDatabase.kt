// 文件路径：app/src/main/java/com/example/nativechatdemo/data/database/AppDatabase.kt
// 文件名：AppDatabase.kt
// 状态：✅ 简化版 - 只保留基础对话模块需要的4个Entity

package com.example.nativechatdemo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nativechatdemo.data.model.*
import com.example.nativechatdemo.data.dao.*

/**
 * 应用数据库（简化版 - 基础对话模块）
 *
 * 当前版本：10
 * 包含的表：
 * - characters：角色配置表
 * - conversations：对话记录表
 * - messages：消息记录表
 * - users：用户表
 */
@Database(
    entities = [
        Character::class,
        Conversation::class,
        Message::class,
        User::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // ========== DAOs ==========
    abstract fun characterDao(): CharacterDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "nativechat_database"

        /**
         * 获取数据库单例
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_9_10)  // 添加迁移策略
                    .fallbackToDestructiveMigration()  // 开发阶段：失败则重建
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * 数据库迁移：版本9 → 版本10
         */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Character表：添加新字段traitsJson
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS characters_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        gender TEXT NOT NULL,
                        avatarRes INTEGER,
                        traitsJson TEXT NOT NULL,
                        isTemplate INTEGER NOT NULL DEFAULT 0,
                        templateType TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // 迁移旧数据（为旧角色创建默认配置）
                database.execSQL("""
                    INSERT INTO characters_new (
                        id, name, gender, avatarRes, traitsJson, 
                        isTemplate, templateType, createdAt, updatedAt
                    )
                    SELECT 
                        id, name, gender, avatarRes,
                        '{"age":22,"occupation":"STUDENT","education":"BACHELOR",' ||
                        '"personalityType":"CUTE_SOFT","profanityLevel":"NONE",' ||
                        '"emojiLevel":"NORMAL","hobbies":["MOVIE_TV"],' ||
                        '"proactivity":5,"openness":5,"chatHabit":"NORMAL"}' as traitsJson,
                        0 as isTemplate,
                        NULL as templateType,
                        createdAt,
                        updatedAt
                    FROM characters
                """.trimIndent())

                database.execSQL("DROP TABLE IF EXISTS characters")
                database.execSQL("ALTER TABLE characters_new RENAME TO characters")
            }
        }
    }
}