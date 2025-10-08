// 文件路径：app/src/main/java/com/example/nativechatdemo/MainActivity.kt
package com.example.nativechatdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.ui.modules.FemaleModulesActivity
import com.example.nativechatdemo.ui.modules.MaleModulesActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 隐藏ActionBar
        supportActionBar?.hide()

        // 女生篇按钮
        findViewById<CardView>(R.id.femaleCard).setOnClickListener {
            val intent = Intent(this, FemaleModulesActivity::class.java)
            startActivity(intent)
        }

        // 男生篇按钮
        findViewById<CardView>(R.id.maleCard).setOnClickListener {
            val intent = Intent(this, MaleModulesActivity::class.java)
            startActivity(intent)
        }
    }
}