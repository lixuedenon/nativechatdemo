package com.example.nativechatdemo.ui.character

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.ui.chat.ChatActivity
import android.widget.TextView

class CharacterSelectionActivity : AppCompatActivity() {

    private var gender: String = "female"  // 默认女生

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_selection)

        // 获取传入的性别参数
        gender = intent.getStringExtra("gender") ?: "female"

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 根据性别设置标题
        toolbar.title = if (gender == "female") "选择女生角色" else "选择男生角色"

        // 根据性别调整第三个角色的文案
        updateThirdCharacter()

        // 设置点击事件
        setupCharacterClicks()
    }

    private fun updateThirdCharacter() {
        // 女生是"优雅型"，男生是"高冷型"
        if (gender == "male") {
            findViewById<TextView>(R.id.elegantName).text = "高冷型"
            findViewById<TextView>(R.id.elegantDesc).text = "神秘高冷，不易接近"
        }
    }

    private fun setupCharacterClicks() {
        // 温柔型
        findViewById<CardView>(R.id.gentleCard).setOnClickListener {
            startChat("gentle")
        }

        // 活泼型
        findViewById<CardView>(R.id.livelyCard).setOnClickListener {
            startChat("lively")
        }

        // 优雅/高冷型
        findViewById<CardView>(R.id.elegantCard).setOnClickListener {
            startChat("elegant")
        }

        // 阳光型
        findViewById<CardView>(R.id.sunnyCard).setOnClickListener {
            startChat("sunny")
        }
    }

    private fun startChat(characterType: String) {
        val intent = Intent(this, ChatActivity::class.java)

        // 传递用户ID
        intent.putExtra("userId", "test_user_001")

        // 构建角色ID：类型_性别（例如：gentle_girl, lively_boy）
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

        startActivity(intent)
    }
}