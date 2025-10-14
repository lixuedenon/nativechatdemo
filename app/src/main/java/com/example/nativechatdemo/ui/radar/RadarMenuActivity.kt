// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/radar/RadarMenuActivity.kt
// 文件类型：Kotlin Class (Activity)

package com.example.nativechatdemo.ui.radar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.ui.conversation.ConversationLearnActivity
import com.example.nativechatdemo.ui.conversation.ConversationPracticeActivity

class RadarMenuActivity : AppCompatActivity() {

    private var targetGender: String = "female"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radar_menu)

        targetGender = intent.getStringExtra("targetGender") ?: "female"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = "社交雷达"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 学习模式
        findViewById<CardView>(R.id.learnModeCard).setOnClickListener {
            val intent = Intent(this, ConversationLearnActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }

        // 练习模式
        findViewById<CardView>(R.id.practiceModeCard).setOnClickListener {
            val intent = Intent(this, ConversationPracticeActivity::class.java)
            intent.putExtra("targetGender", targetGender)
            startActivity(intent)
        }
    }
}