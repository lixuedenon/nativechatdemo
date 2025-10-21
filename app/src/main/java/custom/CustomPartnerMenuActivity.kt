// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/custom/CustomPartnerMenuActivity.kt
// 文件类型：Kotlin Class (Activity)
// 文件状态：【修改】
// 修改内容：添加所有缺失的import语句

package com.example.nativechatdemo.ui.custom

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.utils.CustomTraitConfig

class CustomPartnerMenuActivity : AppCompatActivity() {

    private var gender: String = "female"

    companion object {
        private const val TAG = "CustomPartnerMenu"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_partner_menu)

        // 获取性别参数
        gender = intent.getStringExtra("gender") ?: "female"
        Log.d(TAG, "性别: $gender")

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (gender == "female") "定制男友" else "定制女友"

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 设置四个场景卡片点击事件
        setupScenarioCards()
    }

    private fun setupScenarioCards() {
        // 场景1：相识并初步了解
        findViewById<CardView>(R.id.scenario1Card).setOnClickListener {
            showScenarioDescription(CustomTraitConfig.ScenarioType.KNOWN_ESTABLISHED)
        }

        // 场景2：刚刚相识并有初步了解
        findViewById<CardView>(R.id.scenario2Card).setOnClickListener {
            showScenarioDescription(CustomTraitConfig.ScenarioType.JUST_MET_PARTIAL)
        }

        // 场景3：尚未相识但有心仪目标
        findViewById<CardView>(R.id.scenario3Card).setOnClickListener {
            showScenarioDescription(CustomTraitConfig.ScenarioType.IDEAL_TYPE)
        }

        // 场景4：刚刚相识但不了解
        findViewById<CardView>(R.id.scenario4Card).setOnClickListener {
            showScenarioDescription(CustomTraitConfig.ScenarioType.JUST_MET_UNKNOWN)
        }
    }

    private fun showScenarioDescription(scenario: CustomTraitConfig.ScenarioType) {
        AlertDialog.Builder(this)
            .setTitle(scenario.title)
            .setMessage(scenario.description)
            .setPositiveButton("选择") { _, _ ->
                navigateToTraitInput(scenario.value)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun navigateToTraitInput(scenarioType: Int) {
        // 使用完整的类名来创建Intent
        val intent = Intent(this, CustomTraitInputActivity::class.java)
        intent.putExtra("gender", gender)
        intent.putExtra("scenarioType", scenarioType)
        startActivity(intent)
    }
}