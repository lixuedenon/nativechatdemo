// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/modules/FemaleModulesActivity.kt
// 文件类型：Kotlin Class (Activity)

package com.example.nativechatdemo.ui.modules

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity
import com.example.nativechatdemo.ui.radar.RadarMenuActivity

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

        // 男友养成 - 敬请期待
        findViewById<CardView>(R.id.boyfriendTrainingCard).setOnClickListener {
            showComingSoon("男友养成")
        }

        // 定制男友 - 敬请期待
        findViewById<CardView>(R.id.customBoyfriendCard).setOnClickListener {
            showComingSoon("定制男友")
        }

        // 社交雷达 - 已启用 ✅
        findViewById<CardView>(R.id.radarCard).setOnClickListener {
            val intent = Intent(this, RadarMenuActivity::class.java)
            intent.putExtra("targetGender", "male")  // 女生篇：分析男生
            startActivity(intent)
        }

        // 反PUA - 敬请期待
        findViewById<CardView>(R.id.antiPuaCard).setOnClickListener {
            showComingSoon("反PUA")
        }
    }

    private fun showComingSoon(moduleName: String) {
        Toast.makeText(this, "$moduleName 功能正在开发中，敬请期待！", Toast.LENGTH_SHORT).show()
    }
}