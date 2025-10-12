// 文件路径：app/src/main/java/com/example/nativechatdemo/MainActivity.kt
// 文件类型：Kotlin Class (Activity)

package com.example.nativechatdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
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

        // 退出按钮
        findViewById<Button>(R.id.exitButton).setOnClickListener {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出应用")
            .setMessage("确定要退出恋爱话术训练吗？")
            .setPositiveButton("退出") { _, _ ->
                finish()  // 关闭当前Activity
                // 如果需要完全退出应用，可以使用：
                // finishAffinity()  // 关闭所有Activity
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 按返回键时也弹出退出确认
    override fun onBackPressed() {
        showExitDialog()
    }
}