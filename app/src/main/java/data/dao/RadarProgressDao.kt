// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/RadarProgressDao.kt
// 文件类型：Kotlin Interface (DAO)

package com.example.nativechatdemo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nativechatdemo.data.model.RadarProgress

@Dao
interface RadarProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: RadarProgress)

    @Query("SELECT * FROM radar_progress WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getProgressByUserId(userId: String): List<RadarProgress>

    @Query("SELECT SUM(score) FROM radar_progress WHERE userId = :userId AND mode = 'practice'")
    suspend fun getTotalScore(userId: String): Int?

    @Query("SELECT COUNT(*) FROM radar_progress WHERE userId = :userId AND mode = 'practice' AND isCorrect = 1")
    suspend fun getCorrectCount(userId: String): Int

    @Query("DELETE FROM radar_progress WHERE userId = :userId")
    suspend fun deleteProgressByUserId(userId: String)
}