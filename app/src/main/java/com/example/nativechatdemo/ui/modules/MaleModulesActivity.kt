// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/modules/MaleModulesActivity.kt
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

class MaleModulesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_male_modules)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 基础对话 - 已启用
        findViewById<CardView>(R.id.basicChatCard).setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("gender", "female")  // 男生篇：和女生聊天
            startActivity(intent)
        }

        // 女友养成 - 敬请期待
        findViewById<CardView>(R.id.girlfriendTrainingCard).setOnClickListener {
            showComingSoon("女友养成")
        }

        // 定制女友 - 敬请期待
        findViewById<CardView>(R.id.customGirlfriendCard).setOnClickListener {
            showComingSoon("定制女友")
        }

        // 社交雷达 - 已启用 ✅
        findViewById<CardView>(R.id.radarCard).setOnClickListener {
            val intent = Intent(this, RadarMenuActivity::class.java)
            intent.putExtra("targetGender", "female")  // 男生篇：分析女生
            startActivity(intent)
        }
    }

    private fun showComingSoon(moduleName: String) {
        Toast.makeText(this, "$moduleName 功能正在开发中，敬请期待！", Toast.LENGTH_SHORT).show()
    }
}