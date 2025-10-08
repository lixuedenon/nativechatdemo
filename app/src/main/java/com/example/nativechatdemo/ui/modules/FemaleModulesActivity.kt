package com.example.nativechatdemo.ui.modules

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity

class FemaleModulesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_female_modules)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 基础对话 - 已启用
        findViewById<CardView>(R.id.basicChatCard).setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("gender", "male")  // 女生篇：和男生聊天
            startActivity(intent)
        }

        // 其他模块 - 敬请期待
        findViewById<CardView>(R.id.scenarioCard).setOnClickListener {
            showComingSoon("场景模拟")
        }

        findViewById<CardView>(R.id.practiceCard).setOnClickListener {
            showComingSoon("练习模式")
        }

        findViewById<CardView>(R.id.radarCard).setOnClickListener {
            showComingSoon("社交雷达")
        }

        findViewById<CardView>(R.id.antiPuaCard).setOnClickListener {
            showComingSoon("反PUA")
        }
    }

    private fun showComingSoon(moduleName: String) {
        Toast.makeText(this, "$moduleName 功能正在开发中，敬请期待！", Toast.LENGTH_SHORT).show()
    }
}