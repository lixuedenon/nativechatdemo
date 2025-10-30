// 文件路径：app/src/main/java/com/example/nativechatdemo/data/dao/CharacterDao.kt
// 文件名：CharacterDao.kt
// 类型：DAO（Room数据访问对象）
// 功能：提供Character表的CRUD操作接口
// 依赖：
//   - androidx.room（Room注解）
//   - Character.kt（实体类）
// 引用：被以下类使用
//   - ChatViewModel.kt（查询角色信息）
//   - CharacterConfigActivity.kt（保存/加载角色）
//   - MainActivity.kt（加载角色列表）
// 数据库表：characters
// 创建日期：2025-10-15
// 最后修改：2025-10-28（添加模板相关查询方法）
// 作者：Claude

package com.example.nativechatdemo.data.dao

import androidx.room.*
import com.example.nativechatdemo.data.model.Character

/**
 * Character表的数据访问对象
 * 提供所有与角色相关的数据库操作
 */
@Dao
interface CharacterDao {

    // ========== 增 (Create) ==========

    /**
     * 插入单个角色
     * @return 新插入角色的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: Character): Long

    /**
     * 批量插入角色
     * @return 插入的角色ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<Character>): List<Long>

    // ========== 删 (Delete) ==========

    /**
     * 删除角色
     */
    @Delete
    suspend fun delete(character: Character)

    /**
     * 根据ID删除角色
     */
    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun deleteById(characterId: Long)

    /**
     * 删除所有非模板角色（用户自定义的）
     */
    @Query("DELETE FROM characters WHERE isTemplate = 0")
    suspend fun deleteAllCustom()

    // ========== 改 (Update) ==========

    /**
     * 更新角色
     */
    @Update
    suspend fun update(character: Character)

    /**
     * 更新角色名称
     */
    @Query("UPDATE characters SET name = :name, updatedAt = :timestamp WHERE id = :characterId")
    suspend fun updateName(characterId: Long, name: String, timestamp: Long = System.currentTimeMillis())

    /**
     * 更新角色特征配置
     */
    @Query("UPDATE characters SET traitsJson = :traitsJson, updatedAt = :timestamp WHERE id = :characterId")
    suspend fun updateTraits(characterId: Long, traitsJson: String, timestamp: Long = System.currentTimeMillis())

    // ========== 查 (Read) ==========

    /**
     * 根据ID查询角色
     */
    @Query("SELECT * FROM characters WHERE id = :characterId")
    suspend fun getById(characterId: Long): Character?

    /**
     * 查询所有角色
     */
    @Query("SELECT * FROM characters ORDER BY createdAt DESC")
    suspend fun getAll(): List<Character>

    /**
     * 查询所有预设模板
     */
    @Query("SELECT * FROM characters WHERE isTemplate = 1 ORDER BY createdAt ASC")
    suspend fun getAllTemplates(): List<Character>

    /**
     * 查询所有用户自定义角色
     */
    @Query("SELECT * FROM characters WHERE isTemplate = 0 ORDER BY createdAt DESC")
    suspend fun getAllCustom(): List<Character>

    /**
     * 根据模板类型查询
     */
    @Query("SELECT * FROM characters WHERE templateType = :templateType LIMIT 1")
    suspend fun getByTemplateType(templateType: String): Character?

    /**
     * 根据名称模糊查询
     */
    @Query("SELECT * FROM characters WHERE name LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    suspend fun searchByName(keyword: String): List<Character>

    /**
     * 根据性别查询
     */
    @Query("SELECT * FROM characters WHERE gender = :gender ORDER BY createdAt DESC")
    suspend fun getByGender(gender: String): List<Character>

    /**
     * 统计角色数量
     */
    @Query("SELECT COUNT(*) FROM characters")
    suspend fun getCount(): Int

    /**
     * 统计用户自定义角色数量
     */
    @Query("SELECT COUNT(*) FROM characters WHERE isTemplate = 0")
    suspend fun getCustomCount(): Int

    /**
     * 检查角色名称是否已存在
     */
    @Query("SELECT EXISTS(SELECT 1 FROM characters WHERE name = :name LIMIT 1)")
    suspend fun isNameExists(name: String): Boolean
}