// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/conversation/ConversationPracticeActivity.kt
// 状态：暂时禁用（社交雷达模块）

package com.example.nativechatdemo.ui.conversation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ConversationPracticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "社交雷达模块开发中", Toast.LENGTH_SHORT).show()
        finish()
    }
}