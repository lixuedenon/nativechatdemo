// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/ConfessionTestDao.kt
// 文件类型：Kotlin Interface (Room DAO)
// 文件状态：【新建】

package com.example.nativechatdemo.data.dao

import androidx.room.*
import com.example.nativechatdemo.data.model.ConfessionTest
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfessionTestDao {

    @Insert
    suspend fun insertTest(test: ConfessionTest)

    @Query("SELECT * FROM confession_tests WHERE id = :testId")
    suspend fun getTestById(testId: String): ConfessionTest?

    @Query("SELECT * FROM confession_tests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTestsByUser(userId: String): Flow<List<ConfessionTest>>

    @Query("SELECT * FROM confession_tests WHERE userId = :userId AND traitId = :traitId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestTestByTrait(userId: String, traitId: String): ConfessionTest?

    @Query("DELETE FROM confession_tests WHERE id = :testId")
    suspend fun deleteTest(testId: String)

    @Query("SELECT COUNT(*) FROM confession_tests")
    suspend fun getCount(): Int
}