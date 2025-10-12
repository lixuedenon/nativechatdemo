// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/radar/RadarLearnActivity.kt
// 文件类型：Kotlin Class (Activity)

package com.example.nativechatdemo.ui.radar

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.RadarScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RadarLearnActivity : AppCompatActivity() {

    // UI 组件 - 状态页面
    private lateinit var introLayout: LinearLayout
    private lateinit var dialogueLayout: LinearLayout
    private lateinit var analysisLayout: LinearLayout

    // UI 组件 - 说明页
    private lateinit var introTitleText: TextView
    private lateinit var introDescText: TextView
    private lateinit var startButton: Button

    // UI 组件 - 对话展示页
    private lateinit var dialogueContainer: LinearLayout
    private lateinit var finishViewButton: Button

    // UI 组件 - 复盘页
    private lateinit var analysisContainer: LinearLayout
    private lateinit var backToMenuButton: Button

    private var targetGender: String = "female"
    private var scenarios = mutableListOf<RadarScenario>()

    companion object {
        private const val TAG = "RadarLearnActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "========== onCreate ==========")

        setContentView(R.layout.activity_radar_learn)

        targetGender = intent.getStringExtra("targetGender") ?: "female"
        Log.d(TAG, "targetGender: $targetGender")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = "学习模式"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        loadScenarios()
    }

    private fun initViews() {
        Log.d(TAG, "initViews 开始")

        // 初始化三个状态页面
        introLayout = findViewById(R.id.introLayout)
        dialogueLayout = findViewById(R.id.dialogueLayout)
        analysisLayout = findViewById(R.id.analysisLayout)

        // 说明页组件
        introTitleText = findViewById(R.id.introTitleText)
        introDescText = findViewById(R.id.introDescText)
        startButton = findViewById(R.id.startButton)

        // 对话展示页组件
        dialogueContainer = findViewById(R.id.dialogueContainer)
        finishViewButton = findViewById(R.id.finishViewButton)

        // 复盘页组件
        analysisContainer = findViewById(R.id.analysisContainer)
        backToMenuButton = findViewById(R.id.backToMenuButton)

        // 设置按钮点击事件
        startButton.setOnClickListener {
            showDialoguePage()
        }

        finishViewButton.setOnClickListener {
            showAnalysisPage()
        }

        backToMenuButton.setOnClickListener {
            finish()
        }

        // 默认显示说明页
        showIntroPage()

        Log.d(TAG, "initViews 完成")
    }

    private fun loadScenarios() {
        Log.d(TAG, "========== loadScenarios 开始 ==========")
        Log.d(TAG, "查询条件 - type: learning, gender: $targetGender")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@RadarLearnActivity)
                scenarios = withContext(Dispatchers.IO) {
                    val result = db.radarScenarioDao().getScenariosByTypeAndGender("learning", targetGender)
                    Log.d(TAG, "数据库返回场景数量: ${result.size}")
                    result.toMutableList()
                }

                Log.d(TAG, "加载场景数量: ${scenarios.size}")

                if (scenarios.isEmpty()) {
                    Log.w(TAG, "⚠️ 暂无学习场景")
                    Toast.makeText(this@RadarLearnActivity, "暂无学习场景", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 加载场景失败", e)
                e.printStackTrace()
                Toast.makeText(this@RadarLearnActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ========== 状态切换 ==========

    private fun showIntroPage() {
        Log.d(TAG, "显示说明页")
        introLayout.visibility = View.VISIBLE
        dialogueLayout.visibility = View.GONE
        analysisLayout.visibility = View.GONE

        // 设置说明内容
        introTitleText.text = "欢迎来到学习模式"
        introDescText.text = """
            📚 学习模式说明：
            
            1. 你将看到约10轮真实对话场景
            2. 这些对话中包含了常见的套路和陷阱
            3. 看完对话后，我们会为你详细分析每个关键点
            4. 帮助你识别对方的真实意图
            
            💡 提示：
            - 仔细观察对话中的细节
            - 注意对方的措辞和语气
            - 思考背后的真实意图
            
            准备好了就开始学习吧！
        """.trimIndent()
    }

    private fun showDialoguePage() {
        Log.d(TAG, "显示对话展示页")
        introLayout.visibility = View.GONE
        dialogueLayout.visibility = View.VISIBLE
        analysisLayout.visibility = View.GONE

        // 清空之前的对话
        dialogueContainer.removeAllViews()

        // 生成对话内容
        if (scenarios.isNotEmpty()) {
            scenarios.forEachIndexed { index, scenario ->
                addDialogueItem(index + 1, scenario)
            }
        } else {
            val emptyText = TextView(this).apply {
                text = "暂无对话内容"
                textSize = 16f
                setPadding(32, 32, 32, 32)
            }
            dialogueContainer.addView(emptyText)
        }
    }

    private fun showAnalysisPage() {
        Log.d(TAG, "显示复盘分析页")
        introLayout.visibility = View.GONE
        dialogueLayout.visibility = View.GONE
        analysisLayout.visibility = View.VISIBLE

        // 清空之前的分析
        analysisContainer.removeAllViews()

        // 生成分析内容
        if (scenarios.isNotEmpty()) {
            scenarios.forEachIndexed { index, scenario ->
                addAnalysisItem(index + 1, scenario)
            }
        } else {
            val emptyText = TextView(this).apply {
                text = "暂无分析内容"
                textSize = 16f
                setPadding(32, 32, 32, 32)
            }
            analysisContainer.addView(emptyText)
        }
    }

    // ========== 动态生成内容 ==========

    private fun addDialogueItem(round: Int, scenario: RadarScenario) {
        val card = layoutInflater.inflate(R.layout.item_dialogue_card, dialogueContainer, false)

        val roundText = card.findViewById<TextView>(R.id.roundText)
        val contextText = card.findViewById<TextView>(R.id.contextText)
        val messageText = card.findViewById<TextView>(R.id.messageText)

        roundText.text = "第 $round 轮"
        contextText.text = "场景：${scenario.contextDescription}"
        messageText.text = "对方：\"${scenario.partnerMessage}\""

        dialogueContainer.addView(card)
    }

    private fun addAnalysisItem(round: Int, scenario: RadarScenario) {
        val card = layoutInflater.inflate(R.layout.item_analysis_card, analysisContainer, false)

        val roundText = card.findViewById<TextView>(R.id.roundText)
        val messageText = card.findViewById<TextView>(R.id.messageText)
        val intentText = card.findViewById<TextView>(R.id.intentText)
        val recommendText = card.findViewById<TextView>(R.id.recommendText)

        try {
            val analysisJson = JSONObject(scenario.analysis)
            val intent = analysisJson.getString("intent")

            roundText.text = "第 $round 轮分析"
            messageText.text = "对方说：\"${scenario.partnerMessage}\""
            intentText.text = "📊 真实意图：\n$intent"
            recommendText.text = "✅ 建议回答：\n${scenario.correctResponse}"
        } catch (e: Exception) {
            Log.e(TAG, "解析分析数据失败", e)
        }

        analysisContainer.addView(card)
    }
}