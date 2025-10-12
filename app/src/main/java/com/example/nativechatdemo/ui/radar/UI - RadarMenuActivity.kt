// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/radar/RadarMenuActivity.kt
// 文件类型：Kotlin Class (Activity)

package com.example.nativechatdemo.ui.radar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R

class RadarMenuActivity : AppCompatActivity() {

    private var targetGender: String = "female" // 默认女生（男生篇）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radar_menu)

        // 获取目标性别（从模块列表页传入）
        targetGender = intent.getStringExtra("targetGender") ?: "female"

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 根据目标性别设置标题
        toolbar.title = if (targetGender == "female") "社交雷达 - 与女生" else "社交雷达 - 与男生"

        // 学习模式按钮
        findViewById<CardView>(R.id.learnModeCard).setOnClickListener {
            val intent = Intent(this, RadarLearnActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }

        // 练习模式按钮
        findViewById<CardView>(R.id.practiceModeCard).setOnClickListener {
            val intent = Intent(this, RadarPracticeActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }
    }
}