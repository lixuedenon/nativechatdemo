// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/CustomPartnerTraitDao.kt
// 文件类型：Kotlin Interface (Room DAO)
// 文件状态：【新建】

package com.example.nativechatdemo.data.dao

import androidx.room.*
import com.example.nativechatdemo.data.model.CustomPartnerTrait
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPartnerTraitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrait(trait: CustomPartnerTrait)

    @Update
    suspend fun updateTrait(trait: CustomPartnerTrait)

    @Query("SELECT * FROM custom_partner_traits WHERE id = :traitId")
    suspend fun getTraitById(traitId: String): CustomPartnerTrait?

    @Query("SELECT * FROM custom_partner_traits WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getTraitsByUser(userId: String): Flow<List<CustomPartnerTrait>>

    @Query("SELECT * FROM custom_partner_traits WHERE userId = :userId AND gender = :gender ORDER BY updatedAt DESC")
    suspend fun getTraitsByUserAndGender(userId: String, gender: String): List<CustomPartnerTrait>

    @Query("UPDATE custom_partner_traits SET chatCount = chatCount + 1, lastChatDate = :timestamp WHERE id = :traitId")
    suspend fun incrementChatCount(traitId: String, timestamp: Long)

    @Query("DELETE FROM custom_partner_traits WHERE id = :traitId")
    suspend fun deleteTrait(traitId: String)

    @Query("SELECT COUNT(*) FROM custom_partner_traits")
    suspend fun getCount(): Int
}