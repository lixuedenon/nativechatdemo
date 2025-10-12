// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/radar/RadarPracticeActivity.kt
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
import com.example.nativechatdemo.data.model.RadarProgress
import com.example.nativechatdemo.data.model.RadarScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class RadarPracticeActivity : AppCompatActivity() {

    // UI 组件 - 状态页面
    private lateinit var introLayout: LinearLayout
    private lateinit var dialogueLayout: LinearLayout
    private lateinit var practiceLayout: LinearLayout

    // UI 组件 - 说明页
    private lateinit var introTitleText: TextView
    private lateinit var introDescText: TextView
    private lateinit var startButton: Button

    // UI 组件 - 对话展示页
    private lateinit var dialogueContainer: LinearLayout
    private lateinit var startPracticeButton: Button

    // UI 组件 - 练习页
    private lateinit var scoreText: TextView
    private lateinit var currentQuestionText: TextView
    private lateinit var contextDescText: TextView
    private lateinit var partnerMessageText: TextView
    private lateinit var optionsLayout: LinearLayout
    private lateinit var analysisLayout: LinearLayout
    private lateinit var intentText: TextView
    private lateinit var selectedAnalysisText: TextView
    private lateinit var recommendText: TextView
    private lateinit var nextButton: Button

    private val optionButtons = mutableListOf<Button>()
    private var targetGender: String = "female"
    private var scenarios = mutableListOf<RadarScenario>()
    private var currentIndex = 0
    private var totalScore = 0
    private var selectedOption = -1

    companion object {
        private const val TAG = "RadarPracticeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "========== onCreate ==========")

        setContentView(R.layout.activity_radar_practice)

        targetGender = intent.getStringExtra("targetGender") ?: "female"
        Log.d(TAG, "targetGender: $targetGender")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = "练习模式"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        initViews()
        loadScenarios()
    }

    private fun initViews() {
        Log.d(TAG, "initViews 开始")

        try {
            // 初始化三个状态页面
            introLayout = findViewById(R.id.introLayout)
            dialogueLayout = findViewById(R.id.dialogueLayout)
            practiceLayout = findViewById(R.id.practiceLayout)

            // 说明页组件
            introTitleText = findViewById(R.id.introTitleText)
            introDescText = findViewById(R.id.introDescText)
            startButton = findViewById(R.id.startButton)

            // 对话展示页组件
            dialogueContainer = findViewById(R.id.dialogueContainer)
            startPracticeButton = findViewById(R.id.startPracticeButton)

            // 练习页组件
            scoreText = findViewById(R.id.scoreText)
            currentQuestionText = findViewById(R.id.currentQuestionText)
            contextDescText = findViewById(R.id.contextDescText)
            partnerMessageText = findViewById(R.id.partnerMessageText)
            optionsLayout = findViewById(R.id.optionsLayout)
            analysisLayout = findViewById(R.id.analysisLayout)
            intentText = findViewById(R.id.intentText)
            selectedAnalysisText = findViewById(R.id.selectedAnalysisText)
            recommendText = findViewById(R.id.recommendText)
            nextButton = findViewById(R.id.nextButton)

            val button1: Button = findViewById(R.id.optionButton1)
            val button2: Button = findViewById(R.id.optionButton2)
            val button3: Button = findViewById(R.id.optionButton3)
            val button4: Button = findViewById(R.id.optionButton4)

            optionButtons.add(button1)
            optionButtons.add(button2)
            optionButtons.add(button3)
            optionButtons.add(button4)

            optionButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    onOptionSelected(index + 1)
                }
            }

            // 设置按钮点击事件
            startButton.setOnClickListener {
                showDialoguePage()
            }

            startPracticeButton.setOnClickListener {
                showPracticePage()
            }

            nextButton.setOnClickListener {
                nextScenario()
            }

            // 默认显示说明页
            showIntroPage()

            Log.d(TAG, "initViews 完成")
        } catch (e: Exception) {
            Log.e(TAG, "❌ initViews 出错", e)
            e.printStackTrace()
        }
    }

    private fun loadScenarios() {
        Log.d(TAG, "========== loadScenarios 开始 ==========")
        Log.d(TAG, "查询条件 - type: practice, gender: $targetGender")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@RadarPracticeActivity)
                scenarios = withContext(Dispatchers.IO) {
                    val result = db.radarScenarioDao().getScenariosByTypeAndGender("practice", targetGender)
                    Log.d(TAG, "数据库返回场景数量: ${result.size}")
                    result.toMutableList()
                }

                Log.d(TAG, "加载场景数量: ${scenarios.size}")

                if (scenarios.isEmpty()) {
                    Log.w(TAG, "⚠️ 暂无练习场景")
                    Toast.makeText(this@RadarPracticeActivity, "暂无练习场景", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 加载场景失败", e)
                e.printStackTrace()
                Toast.makeText(this@RadarPracticeActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ========== 状态切换 ==========

    private fun showIntroPage() {
        Log.d(TAG, "显示说明页")
        introLayout.visibility = View.VISIBLE
        dialogueLayout.visibility = View.GONE
        practiceLayout.visibility = View.GONE

        // 设置说明内容
        introTitleText.text = "欢迎来到练习模式"
        introDescText.text = """
            🎯 练习模式说明：
            
            1. 首先你将看到约10轮完整对话
            2. 仔细观察每轮对话的内容
            3. 然后进入答题环节
            4. 对每轮对话选择最佳回答
            5. 系统会给你打分并分析
            
            💡 提示：
            - 先认真看完所有对话
            - 思考如何回答最合适
            - 注意对方的言外之意
            
            准备好了就开始练习吧！
        """.trimIndent()
    }

    private fun showDialoguePage() {
        Log.d(TAG, "显示对话展示页")
        introLayout.visibility = View.GONE
        dialogueLayout.visibility = View.VISIBLE
        practiceLayout.visibility = View.GONE

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

    private fun showPracticePage() {
        Log.d(TAG, "显示练习页")
        introLayout.visibility = View.GONE
        dialogueLayout.visibility = View.GONE
        practiceLayout.visibility = View.VISIBLE

        // 重置练习状态
        currentIndex = 0
        totalScore = 0
        updateScore()

        // 显示第一题
        if (scenarios.isNotEmpty()) {
            displayScenario(scenarios[currentIndex])
        }
    }

    // ========== 动态生成对话卡片 ==========

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

    // ========== 练习答题逻辑 ==========

    private fun displayScenario(scenario: RadarScenario) {
        Log.d(TAG, "========== displayScenario ==========")
        Log.d(TAG, "场景ID: ${scenario.id}")

        selectedOption = -1
        analysisLayout.visibility = View.GONE
        optionsLayout.visibility = View.VISIBLE
        nextButton.visibility = View.GONE

        optionButtons.forEach { it.isEnabled = true }

        currentQuestionText.text = "第 ${currentIndex + 1} 题 / 共 ${scenarios.size} 题"
        contextDescText.text = "场景：${scenario.contextDescription}"
        partnerMessageText.text = "她/他：\"${scenario.partnerMessage}\""

        val wrongOptions = parseOptions(scenario.wrongResponses)
        val allOptions = (wrongOptions + scenario.correctResponse).shuffled()

        optionButtons.forEachIndexed { index, button ->
            if (index < allOptions.size) {
                val optionText = ('A' + index).toString() + ". " + allOptions[index]
                button.text = optionText
                button.visibility = View.VISIBLE
            } else {
                button.visibility = View.GONE
            }
        }

        Log.d(TAG, "场景显示完成")
    }

    private fun parseOptions(json: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 解析选项失败", e)
            e.printStackTrace()
        }
        return list
    }

    private fun onOptionSelected(optionIndex: Int) {
        Log.d(TAG, "用户选择了选项: $optionIndex")

        selectedOption = optionIndex
        optionButtons.forEach { it.isEnabled = false }

        val scenario = scenarios[currentIndex]
        val selectedText = optionButtons[optionIndex - 1].text.toString().substring(3)

        val analysisJson = JSONObject(scenario.analysis)
        val intent = analysisJson.getString("intent")
        val optionsArray = analysisJson.getJSONArray("options")

        var selectedAnalysis = ""
        var score = 0
        var recommendOption = ""
        var maxScore = 0

        for (i in 0 until optionsArray.length()) {
            val opt = optionsArray.getJSONObject(i)
            val text = opt.getString("text")
            val analysis = opt.getString("analysis")
            val optScore = opt.getInt("score")

            if (text == selectedText) {
                selectedAnalysis = analysis
                score = optScore
            }

            if (optScore > maxScore) {
                maxScore = optScore
                recommendOption = text
            }
        }

        intentText.text = "📊 对方的真实意图：\n$intent"
        selectedAnalysisText.text = "📝 你的回答分析：\n你选择了「$selectedText」\n$selectedAnalysis\n\n得分：${if (score > 0) "+" else ""}${score}分"

        if (selectedText != recommendOption) {
            recommendText.text = "✨ 推荐答案：\n「$recommendOption」更合适，可以获得 +$maxScore 分"
            recommendText.visibility = View.VISIBLE
        } else {
            recommendText.visibility = View.GONE
        }

        totalScore += score
        updateScore()

        saveProgress(scenario.id, optionIndex, selectedText == scenario.correctResponse, score)

        analysisLayout.visibility = View.VISIBLE
        nextButton.visibility = View.VISIBLE

        Log.d(TAG, "选项处理完成，得分: $score")
    }

    private fun saveProgress(scenarioId: String, option: Int, correct: Boolean, score: Int) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@RadarPracticeActivity)
                val progress = RadarProgress(
                    id = "progress_${System.currentTimeMillis()}",
                    userId = "test_user_001",
                    scenarioId = scenarioId,
                    mode = "practice",
                    selectedOption = option,
                    isCorrect = correct,
                    score = score
                )

                withContext(Dispatchers.IO) {
                    db.radarProgressDao().insertProgress(progress)
                }
                Log.d(TAG, "进度保存成功")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 保存进度失败", e)
                e.printStackTrace()
            }
        }
    }

    private fun updateScore() {
        scoreText.text = "总分：$totalScore"
    }

    private fun nextScenario() {
        Log.d(TAG, "nextScenario - 当前索引: $currentIndex, 总数: ${scenarios.size}")

        if (currentIndex < scenarios.size - 1) {
            currentIndex++
            displayScenario(scenarios[currentIndex])
        } else {
            Toast.makeText(this, "练习完成！总分：$totalScore", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}