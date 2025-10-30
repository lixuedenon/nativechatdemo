// 文件路径：app/src/main/java/com/example/nativechatdemo/MainActivity.kt
// 文件名：MainActivity.kt
// 文件类型：class（Activity，继承AppCompatActivity）
// 状态：✅ 完整版（直接替换使用）
// 创建日期：2025-10-15
// 最后修改：2025-10-28
// 作者：Claude

package com.example.nativechatdemo

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nativechatdemo.ui.character.CharacterConfigActivity
import com.example.nativechatdemo.utils.ApiConfig
import com.example.nativechatdemo.utils.OpenAIService

class MainActivity : AppCompatActivity() {

    private lateinit var btnConfigCharacter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConfigCharacter = findViewById(R.id.btnConfigCharacter)

        initializeOpenAI()

        btnConfigCharacter.setOnClickListener {
            startCharacterConfig()
        }
    }

    private fun initializeOpenAI() {
        val savedApiKey = ApiConfig.loadApiKey(this)

        if (savedApiKey != null) {
            OpenAIService.initialize(savedApiKey)
        } else {
            showApiKeyInputDialog()
        }
    }

    private fun showApiKeyInputDialog() {
        val input = EditText(this)
        input.hint = "输入OpenAI API Key (sk-...)"
        input.inputType = InputType.TYPE_CLASS_TEXT

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(50, 20, 50, 20)
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("配置OpenAI API")
            .setMessage("请输入你的OpenAI API Key\n\n获取地址：\nhttps://platform.openai.com/api-keys")
            .setView(container)
            .setPositiveButton("保存") { dialog, which ->
                val apiKey = input.text.toString().trim()
                val success = ApiConfig.saveApiKey(this, apiKey)
                if (success) {
                    OpenAIService.initialize(apiKey)
                    Toast.makeText(this, "API Key已保存", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "API Key格式错误", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun startCharacterConfig() {
        if (!OpenAIService.isInitialized()) {
            Toast.makeText(this, "请先配置OpenAI API Key", Toast.LENGTH_SHORT).show()
            showApiKeyInputDialog()
            return
        }

        val intent = Intent(this, CharacterConfigActivity::class.java)
        startActivity(intent)
    }
}