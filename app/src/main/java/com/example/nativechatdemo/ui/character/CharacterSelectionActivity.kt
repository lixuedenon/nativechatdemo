// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/character/CharacterSelectionActivity.kt
// 文件类型：Kotlin Class (Activity)
// 修改内容：新增养成模式支持，显示开场故事弹窗

package com.example.nativechatdemo.ui.character

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.ui.chat.ChatActivity
import com.example.nativechatdemo.utils.TrainingStoryConfig
import android.widget.TextView

class CharacterSelectionActivity : AppCompatActivity() {

    private var gender: String = "female"
    private var moduleType: String = "basic"  // 新增：模块类型

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_selection)

        // 获取传入的参数
        gender = intent.getStringExtra("gender") ?: "female"
        moduleType = intent.getStringExtra("moduleType") ?: "basic"  // 新增

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 根据性别和模块类型设置标题
        toolbar.title = when {
            moduleType == "training" && gender == "female" -> "选择你的理想男友"
            moduleType == "training" && gender == "male" -> "选择你的理想女友"
            gender == "female" -> "选择女生角色"
            else -> "选择男生角色"
        }

        // 根据性别调整第三个角色的文案
        updateThirdCharacter()

        // 设置点击事件
        setupCharacterClicks()
    }

    private fun updateThirdCharacter() {
        if (gender == "male") {
            findViewById<TextView>(R.id.elegantName).text = "高冷型"
            findViewById<TextView>(R.id.elegantDesc).text = "神秘高冷，不易接近"
        }
    }

    private fun setupCharacterClicks() {
        findViewById<CardView>(R.id.gentleCard).setOnClickListener {
            onCharacterSelected("gentle")
        }

        findViewById<CardView>(R.id.livelyCard).setOnClickListener {
            onCharacterSelected("lively")
        }

        findViewById<CardView>(R.id.elegantCard).setOnClickListener {
            onCharacterSelected("elegant")
        }

        findViewById<CardView>(R.id.sunnyCard).setOnClickListener {
            onCharacterSelected("sunny")
        }
    }

    private fun onCharacterSelected(characterType: String) {
        // 如果是养成模式，先显示开场故事
        if (moduleType == "training") {
            showOpeningStory(characterType)
        } else {
            // 基础对话模式，直接进入聊天
            startChat(characterType)
        }
    }

    /**
     * 显示开场故事弹窗
     */
    private fun showOpeningStory(characterType: String) {
        val story = TrainingStoryConfig.getOpeningStory(gender)

        AlertDialog.Builder(this)
            .setTitle(story.title)
            .setMessage(story.content)
            .setPositiveButton("开始聊天 💬") { _, _ ->
                startChat(characterType)
            }
            .setCancelable(false)
            .show()
    }

    private fun startChat(characterType: String) {
        val intent = Intent(this, ChatActivity::class.java)

        // 传递用户ID
        intent.putExtra("userId", "test_user_001")

        // 构建角色ID
        val characterId = if (gender == "female") {
            "${characterType}_girl"
        } else {
            "${characterType}_boy"
        }
        intent.putExtra("characterId", characterId)

        // 构建角色名称
        val characterName = when (characterType) {
            "gentle" -> if (gender == "female") "温柔女生" else "温柔男生"
            "lively" -> if (gender == "female") "活泼女生" else "活泼男生"
            "elegant" -> if (gender == "female") "优雅女生" else "高冷男生"
            "sunny" -> if (gender == "female") "阳光女生" else "阳光男生"
            else -> "角色"
        }
        intent.putExtra("characterName", characterName)

        // 传递性别和模块类型
        intent.putExtra("gender", gender)
        intent.putExtra("moduleType", moduleType)

        startActivity(intent)
        finish()  // 关闭角色选择页
    }
}