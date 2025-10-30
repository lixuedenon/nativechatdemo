// 文件路径：app/src/main/java/com/example/nativechatdemo/data/database/AppDatabase.kt
// 文件名：AppDatabase.kt
// 类型：Database（Room数据库主类）
// 功能：应用数据库主类，管理所有表和数据访问对象（DAOs）
// 依赖：
//   - androidx.room（Room数据库框架）
//   - 所有Model类（Character, Conversation, Message等）
//   - 所有DAO接口
// 引用：被所有需要访问数据库的类使用
//   - ViewModels
//   - Activities
//   - Services
// 数据库版本：10（从版本9升级，Character表新增traitsJson字段）
// 创建日期：2025-10-15
// 最后修改：2025-10-28（版本升级，Character表schema变化）
// 作者：Claude

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
 * 应用数据库
 *
 * 当前版本：10
 * 包含的表：
 * - characters：角色配置表
 * - conversations：对话记录表
 * - messages：消息记录表
 * - conversation_analyses：对话分析表
 * - confession_tests：告白测试表
 * - custom_partner_traits：定制伙伴特质表
 * - conversation_scenarios：对话场景表
 * - users：用户表
 */
@Database(
    entities = [
        Character::class,
        Conversation::class,
        Message::class,
        ConversationAnalysis::class,
        ConfessionTest::class,
        CustomPartnerTrait::class,
        ConversationScenario::class,
        User::class
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // ========== DAOs ==========
    abstract fun characterDao(): CharacterDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationAnalysisDao(): ConversationAnalysisDao
    abstract fun confessionTestDao(): ConfessionTestDao
    abstract fun customPartnerTraitDao(): CustomPartnerTraitDao
    abstract fun conversationScenarioDao(): ConversationScenarioDao
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
                    .fallbackToDestructiveMigration()  // 开发阶段可用，生产环境需删除
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * 数据库迁移：版本9 → 版本10
         *
         * 变更内容：
         * 1. Character表新增字段：
         *    - traitsJson: TEXT（4维度特征配置）
         *    - isTemplate: INTEGER（是否为预设模板）
         *    - templateType: TEXT（模板类型）
         *
         * 2. 移除Character表的旧字段：
         *    - personality: TEXT（旧的性格字段，已废弃）
         *    - description: TEXT（旧的描述字段，已废弃）
         */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 创建新的临时表
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

                // 2. 迁移旧数据到新表（为旧角色创建默认配置）
                database.execSQL("""
                    INSERT INTO characters_new (
                        id, name, gender, avatarRes, traitsJson, 
                        isTemplate, templateType, createdAt, updatedAt
                    )
                    SELECT 
                        id, 
                        name, 
                        gender, 
                        avatarRes,
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

                // 3. 删除旧表
                database.execSQL("DROP TABLE IF EXISTS characters")

                // 4. 重命名新表
                database.execSQL("ALTER TABLE characters_new RENAME TO characters")

                // 5. 创建索引（可选，提升查询性能）
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_characters_templateType 
                    ON characters(templateType)
                """.trimIndent())
            }
        }
    }
}