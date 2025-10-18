// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/CharacterDao.kt
// 文件类型：Kotlin Interface (Room DAO)

package com.example.nativechatdemo.data.dao

import androidx.room.*
import com.example.nativechatdemo.data.model.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY isVip ASC, name ASC")
    fun getAllCharacters(): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getCharacterById(characterId: String): Character?

    @Query("SELECT * FROM characters WHERE isVip = 0")
    fun getFreeCharacters(): Flow<List<Character>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<Character>)

    @Query("DELETE FROM characters")
    suspend fun deleteAllCharacters()

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCount(): Int
}