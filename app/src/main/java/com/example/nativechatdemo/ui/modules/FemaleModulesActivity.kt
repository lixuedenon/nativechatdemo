// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/modules/FemaleModulesActivity.kt

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
            startActivity(intent)
        }

        // 社交雷达 ✅ 正确的ID是 radarCard
        findViewById<CardView>(R.id.radarCard).setOnClickListener {
            val intent = Intent(this, RadarMenuActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }

        // 男友养成（未开发）
        findViewById<CardView>(R.id.boyfriendTrainingCard).setOnClickListener {
            Toast.makeText(this, "男友养成模块开发中", Toast.LENGTH_SHORT).show()
        }

        // 定制男友（未开发）
        findViewById<CardView>(R.id.customBoyfriendCard).setOnClickListener {
            Toast.makeText(this, "定制男友模块开发中", Toast.LENGTH_SHORT).show()
        }

        // 反PUA（未开发）
        findViewById<CardView>(R.id.antiPuaCard).setOnClickListener {
            Toast.makeText(this, "反PUA模块开发中", Toast.LENGTH_SHORT).show()
        }
    }
}