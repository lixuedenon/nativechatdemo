// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/RadarScenarioDao.kt
// 文件类型：Kotlin Interface (DAO)

package com.example.nativechatdemo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nativechatdemo.data.model.RadarScenario

@Dao
interface RadarScenarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenario(scenario: RadarScenario)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenarios(scenarios: List<RadarScenario>)

    @Query("SELECT * FROM radar_scenarios WHERE type = :type AND targetGender = :gender")
    suspend fun getScenariosByTypeAndGender(type: String, gender: String): List<RadarScenario>

    @Query("SELECT * FROM radar_scenarios WHERE id = :scenarioId")
    suspend fun getScenarioById(scenarioId: String): RadarScenario?

    @Query("SELECT * FROM radar_scenarios WHERE type = :type AND targetGender = :gender AND category = :category")
    suspend fun getScenariosByCategory(type: String, gender: String, category: String): List<RadarScenario>

    @Query("DELETE FROM radar_scenarios")
    suspend fun deleteAllScenarios()
}