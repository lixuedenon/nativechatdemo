// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/ConversationScenarioDao.kt
// 文件类型：Kotlin Interface (Room DAO)

package com.example.nativechatdemo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nativechatdemo.data.model.ConversationScenario

@Dao
interface ConversationScenarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: ConversationScenario)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenarios(scenarios: List<ConversationScenario>)

    @Query("SELECT * FROM conversation_scenarios WHERE targetGender = :gender")
    suspend fun getScenariosByGender(gender: String): List<ConversationScenario>

    @Query("SELECT * FROM conversation_scenarios WHERE id = :scenarioId")
    suspend fun getScenarioById(scenarioId: String): ConversationScenario?

    @Query("DELETE FROM conversation_scenarios")
    suspend fun deleteAllScenarios()

    @Query("SELECT COUNT(*) FROM conversation_scenarios")
    suspend fun getCount(): Int
}