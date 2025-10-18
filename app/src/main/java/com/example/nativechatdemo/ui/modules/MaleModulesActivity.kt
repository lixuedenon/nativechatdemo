// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/modules/MaleModulesActivity.kt
// 文件类型：Kotlin Class (Activity)
// 修改内容：修改"女友养成"按钮，跳转到角色选择页并传递moduleType参数

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

    private val targetGender = "female"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_male_modules)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 基础对话
        findViewById<CardView>(R.id.basicChatCard).setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            intent.putExtra("gender", "male")  // 男生篇
            intent.putExtra("moduleType", "basic")
            startActivity(intent)
        }

        // 社交雷达
        findViewById<CardView>(R.id.radarCard).setOnClickListener {
            val intent = Intent(this, RadarMenuActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }

        // 女友养成（修改为跳转到角色选择）
        findViewById<CardView>(R.id.girlfriendTrainingCard).setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("gender", "male")  // 男生篇
            intent.putExtra("moduleType", "training")  // 养成模式
            startActivity(intent)
        }

        // 定制女友（未开发）
        findViewById<CardView>(R.id.customGirlfriendCard).setOnClickListener {
            Toast.makeText(this, "定制女友模块开发中", Toast.LENGTH_SHORT).show()
        }
    }
}