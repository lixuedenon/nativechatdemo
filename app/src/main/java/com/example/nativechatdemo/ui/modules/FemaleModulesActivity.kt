// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/modules/FemaleModulesActivity.kt
// 文件类型：Kotlin Class (Activity)
// 文件状态：【修改】
// 修改内容：添加定制男友按钮跳转逻辑

package com.example.nativechatdemo.ui.modules

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity
import com.example.nativechatdemo.ui.custom.CustomPartnerMenuActivity  // 新增导入
import com.example.nativechatdemo.ui.radar.RadarMenuActivity

class FemaleModulesActivity : AppCompatActivity() {

    private val targetGender = "male"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_female_modules)

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
            intent.putExtra("gender", "female")  // 女生篇
            intent.putExtra("moduleType", "basic")
            startActivity(intent)
        }

        // 社交雷达
        findViewById<CardView>(R.id.radarCard).setOnClickListener {
            val intent = Intent(this, RadarMenuActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }

        // 男友养成
        findViewById<CardView>(R.id.boyfriendTrainingCard).setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("gender", "female")  // 女生篇
            intent.putExtra("moduleType", "training")  // 养成模式
            startActivity(intent)
        }

        // 定制男友（修改：跳转到定制模块）
        findViewById<CardView>(R.id.customBoyfriendCard).setOnClickListener {
            val intent = Intent(this, CustomPartnerMenuActivity::class.java)
            intent.putExtra("gender", "female")  // 女生篇，定制男友
            startActivity(intent)
        }

        // 反PUA（未开发）
        findViewById<CardView>(R.id.antiPuaCard).setOnClickListener {
            Toast.makeText(this, "反PUA模块开发中", Toast.LENGTH_SHORT).show()
        }
    }
}