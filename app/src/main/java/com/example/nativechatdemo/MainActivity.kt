package com.example.nativechatdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.User
import com.example.nativechatdemo.ui.chat.ChatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 异步初始化，带超时保护
        initTestDataAndLaunch()
    }

    private fun initTestDataAndLaunch() {
        lifecycleScope.launch {
            try {
                // 设置3秒超时
                withTimeout(3000L) {
                    val database = AppDatabase.getDatabase(applicationContext)

                    // 检查或创建测试用户
                    var user = withContext(Dispatchers.IO) {
                        database.userDao().getUserById("test_user_001")
                    }

                    if (user == null) {
                        user = User(
                            id = "test_user_001",
                            username = "测试用户",
                            credits = 100,
                            userLevel = 1,
                            createdAt = System.currentTimeMillis()
                        )
                        withContext(Dispatchers.IO) {
                            database.userDao().insertUser(user)
                        }
                    }

                    // 等待角色数据（最多重试3次，每次500ms）
                    withContext(Dispatchers.IO) {
                        var character = database.characterDao().getCharacterById("gentle_girl")
                        var retryCount = 0
                        while (character == null && retryCount < 3) {
                            Thread.sleep(500)
                            character = database.characterDao().getCharacterById("gentle_girl")
                            retryCount++
                        }

                        // 如果还是null，手动插入
                        if (character == null) {
                            populateCharacters(database)
                        }
                    }
                }

                // 初始化成功，跳转
                startChatActivity()

            } catch (e: Exception) {
                e.printStackTrace()

                // 超时或出错，仍然跳转但提示用户
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "数据库初始化失败，使用临时数据",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                startChatActivity()
            }
        }
    }

    private fun startChatActivity() {
        val intent = Intent(this@MainActivity, ChatActivity::class.java).apply {
            putExtra("userId", "test_user_001")
            putExtra("characterId", "gentle_girl")
            putExtra("characterName", "温柔女生")
        }
        startActivity(intent)
        finish()
    }

    // 手动插入角色数据
    private suspend fun populateCharacters(database: AppDatabase) {
        withContext(Dispatchers.IO) {
            try {
                val characters = listOf(
                    com.example.nativechatdemo.data.model.Character(
                        id = "gentle_girl",
                        name = "温柔女生",
                        description = "她温柔体贴，善解人意",
                        avatar = "gentle_girl",
                        type = "gentle",
                        gender = "female",
                        isVip = false
                    ),
                    com.example.nativechatdemo.data.model.Character(
                        id = "lively_girl",
                        name = "活泼女生",
                        description = "她活泼开朗，充满活力",
                        avatar = "lively_girl",
                        type = "lively",
                        gender = "female",
                        isVip = false
                    ),
                    com.example.nativechatdemo.data.model.Character(
                        id = "elegant_girl",
                        name = "优雅女生",
                        description = "她优雅知性，气质出众",
                        avatar = "elegant_girl",
                        type = "elegant",
                        gender = "female",
                        isVip = false
                    ),
                    com.example.nativechatdemo.data.model.Character(
                        id = "sunny_boy",
                        name = "阳光男生",
                        description = "他阳光开朗，积极向上",
                        avatar = "sunny_boy",
                        type = "sunny",
                        gender = "male",
                        isVip = false
                    )
                )
                database.characterDao().insertCharacters(characters)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}