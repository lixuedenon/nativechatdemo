// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/review/ReviewActivity.kt
// 状态：暂时禁用（复盘功能待接入OpenAI）

package com.example.nativechatdemo.ui.review

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "复盘功能开发中", Toast.LENGTH_SHORT).show()
        finish()
    }
}