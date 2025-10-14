// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/conversation/ConversationPracticeActivity.kt
// 文件类型：Kotlin Class (Activity)

package com.example.nativechatdemo.ui.conversation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.ConversationScenario
import com.example.nativechatdemo.data.model.DialogueTurn
import com.example.nativechatdemo.data.model.KeyPoint
import com.example.nativechatdemo.data.model.OptionAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class ConversationPracticeActivity : AppCompatActivity() {

    private lateinit var introLayout: LinearLayout
    private lateinit var conversationLayout: LinearLayout
    private lateinit var introTitle: TextView
    private lateinit var introDesc: TextView
    private lateinit var startButton: Button
    private lateinit var scenarioTitle: TextView
    private lateinit var conversationScrollView: ScrollView
    private lateinit var conversationContainer: LinearLayout
    private lateinit var nextButton: Button
    private lateinit var finishButton: Button

    private var targetGender: String = "female"
    private var scenario: ConversationScenario? = null
    private var dialogueTurns = mutableListOf<DialogueTurn>()
    private var keyPoints = mutableListOf<KeyPoint>()
    private var currentTurnIndex = 0
    private var currentMessageIndex = 0
    private var totalScore = 0

    companion object {
        private const val TAG = "ConversationPractice"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_learn)

        targetGender = intent.getStringExtra("targetGender") ?: "female"
        Log.d(TAG, "targetGender: $targetGender")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = "练习模式"
        toolbar.setNavigationOnClickListener { finish() }

        initViews()
        loadScenario()
    }

    private fun initViews() {
        introLayout = findViewById(R.id.introLayout)
        conversationLayout = findViewById(R.id.conversationLayout)
        introTitle = findViewById(R.id.introTitle)
        introDesc = findViewById(R.id.introDesc)
        startButton = findViewById(R.id.startButton)
        scenarioTitle = findViewById(R.id.scenarioTitle)
        conversationScrollView = findViewById(R.id.conversationScrollView)
        conversationContainer = findViewById(R.id.conversationContainer)
        nextButton = findViewById(R.id.nextButton)
        finishButton = findViewById(R.id.finishButton)

        introTitle.text = "欢迎来到练习模式"
        introDesc.text = """
            🎯 练习模式说明：
            
            1. 你将参与一段真实的对话（10-15轮）
            2. 对话会逐句展示，点击"下一句"继续
            3. 遇到关键时刻需要你自己选择回答
            4. 系统会根据你的选择进行打分和分析
            5. 练习结束后查看总分和详细评价
            
            💡 提示：
            - 每个选择都会影响最终得分
            - 仔细思考每个选项的含义
            - 不要着急，选择最合适的回答
            
            准备好了就开始练习吧！
        """.trimIndent()

        startButton.setOnClickListener {
            if (scenario != null) {
                showConversationPage()
            } else {
                Toast.makeText(this, "暂无练习场景", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            showNextMessage()
        }

        finishButton.setOnClickListener {
            showFinalScore()
        }
    }

    private fun loadScenario() {
        Log.d(TAG, "========== loadScenario 开始 ==========")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@ConversationPracticeActivity)
                val scenarios = withContext(Dispatchers.IO) {
                    db.conversationScenarioDao().getScenariosByGender(targetGender)
                }

                Log.d(TAG, "返回场景数量: ${scenarios.size}")

                if (scenarios.isNotEmpty()) {
                    scenario = scenarios.first()
                    parseScenarioData()
                    Log.d(TAG, "✅ 加载场景成功: ${scenario?.title}")
                } else {
                    Log.w(TAG, "⚠️ 暂无练习场景")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 加载场景失败", e)
            }
        }
    }

    private fun parseScenarioData() {
        scenario?.let { s ->
            try {
                val dialogueArray = JSONArray(s.dialogueJson)
                dialogueTurns.clear()
                for (i in 0 until dialogueArray.length()) {
                    val obj = dialogueArray.getJSONObject(i)
                    dialogueTurns.add(
                        DialogueTurn(
                            index = obj.getInt("index"),
                            partnerSays = obj.getString("partnerSays"),
                            mySays = obj.getString("mySays"),
                            isKeyPoint = obj.getBoolean("isKeyPoint")
                        )
                    )
                }
                Log.d(TAG, "解析对话数量: ${dialogueTurns.size}")
            } catch (e: Exception) {
                Log.e(TAG, "解析对话失败", e)
            }

            try {
                val keyPointsArray = JSONArray(s.keyPointsJson)
                keyPoints.clear()
                for (i in 0 until keyPointsArray.length()) {
                    val obj = keyPointsArray.getJSONObject(i)

                    val wrongOptions = mutableListOf<String>()
                    val wrongOptionsArray = obj.getJSONArray("wrongOptions")
                    for (j in 0 until wrongOptionsArray.length()) {
                        wrongOptions.add(wrongOptionsArray.getString(j))
                    }

                    val optionAnalysisList = mutableListOf<OptionAnalysis>()
                    val analysisArray = obj.getJSONArray("optionAnalysis")
                    for (j in 0 until analysisArray.length()) {
                        val aObj = analysisArray.getJSONObject(j)
                        optionAnalysisList.add(
                            OptionAnalysis(
                                optionText = aObj.getString("optionText"),
                                analysis = aObj.getString("analysis"),
                                score = aObj.getInt("score")
                            )
                        )
                    }

                    keyPoints.add(
                        KeyPoint(
                            atTurnIndex = obj.getInt("atTurnIndex"),
                            warning = obj.getString("warning"),
                            correctResponse = obj.getString("correctResponse"),
                            wrongOptions = wrongOptions,
                            analysis = obj.getString("analysis"),
                            optionAnalysis = optionAnalysisList
                        )
                    )
                }
                Log.d(TAG, "解析关键点数量: ${keyPoints.size}")
            } catch (e: Exception) {
                Log.e(TAG, "解析关键点失败", e)
            }
        }
    }

    private fun showConversationPage() {
        introLayout.visibility = View.GONE
        conversationLayout.visibility = View.VISIBLE
        scenarioTitle.text = "场景：${scenario?.title ?: ""} | 总分：$totalScore"

        currentTurnIndex = 0
        currentMessageIndex = 0
        totalScore = 0
        conversationContainer.removeAllViews()

        showNextMessage()
    }

    private fun showNextMessage() {
        Log.d(TAG, "showNextMessage - turnIndex: $currentTurnIndex, messageIndex: $currentMessageIndex")

        if (currentTurnIndex >= dialogueTurns.size) {
            Log.d(TAG, "对话结束")
            nextButton.visibility = View.GONE
            finishButton.visibility = View.VISIBLE
            return
        }

        val turn = dialogueTurns[currentTurnIndex]
        Log.d(TAG, "当前轮次: ${turn.index}, isKeyPoint: ${turn.isKeyPoint}")

        if (currentMessageIndex == 0) {
            // 显示对方的话
            addPartnerMessage(turn.partnerSays)
            currentMessageIndex = 1

            // 如果是关键点，弹出选择对话框
            if (turn.isKeyPoint) {
                Log.d(TAG, "⭐ 这是关键点，禁用下一句按钮，弹出选择对话框")
                nextButton.isEnabled = false

                // 延迟200ms弹出对话框，确保消息已经显示
                conversationScrollView.postDelayed({
                    showChoiceDialog(turn.index)
                }, 200)
            }
        } else {
            // 练习模式中，我方的话由用户选择后自动显示
            // 如果不是关键点，显示默认回答
            if (!turn.isKeyPoint) {
                addMyMessage(turn.mySays)
            }
            currentTurnIndex++
            currentMessageIndex = 0
        }

        conversationScrollView.post {
            conversationScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun addPartnerMessage(message: String) {
        val view = layoutInflater.inflate(R.layout.item_message_partner, conversationContainer, false)
        view.findViewById<TextView>(R.id.messageText).text = message
        conversationContainer.addView(view)
    }

    private fun addMyMessage(message: String) {
        val view = layoutInflater.inflate(R.layout.item_message_me, conversationContainer, false)
        view.findViewById<TextView>(R.id.messageText).text = message
        conversationContainer.addView(view)
    }

    private fun showChoiceDialog(turnIndex: Int) {
        Log.d(TAG, "showChoiceDialog - turnIndex: $turnIndex")

        val keyPoint = keyPoints.find { it.atTurnIndex == turnIndex }

        if (keyPoint == null) {
            Log.e(TAG, "❌ 找不到 turnIndex=$turnIndex 的关键点")
            nextButton.isEnabled = true
            return
        }

        Log.d(TAG, "找到关键点，构建选项...")

        // 构建4个选项（3个错误+1个正确，打乱顺序）
        val allOptions = mutableListOf<String>()
        allOptions.add(keyPoint.correctResponse)
        allOptions.addAll(keyPoint.wrongOptions)
        allOptions.shuffle()

        Log.d(TAG, "选项数量: ${allOptions.size}")
        allOptions.forEachIndexed { index, option ->
            Log.d(TAG, "选项[$index]: $option")
        }

        val optionsArray = allOptions.toTypedArray()

        // 弹出选择对话框（只用setItems，不用setMessage）
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("💡 ${keyPoint.warning}\n\n👇 请选择你的回答：")
                .setItems(optionsArray) { _, which ->
                    val selectedOption = optionsArray[which]
                    Log.d(TAG, "用户选择了: $selectedOption")
                    handleChoice(selectedOption, keyPoint)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun handleChoice(selectedOption: String, keyPoint: KeyPoint) {
        Log.d(TAG, "handleChoice - selectedOption: $selectedOption")

        // 显示用户选择的消息
        addMyMessage(selectedOption)

        // 查找对应的分析
        val optionAnalysis = keyPoint.optionAnalysis.find { it.optionText == selectedOption }
        val score = optionAnalysis?.score ?: 0
        totalScore += score

        Log.d(TAG, "得分: $score, 总分: $totalScore")

        // 更新分数显示
        scenarioTitle.text = "场景：${scenario?.title ?: ""} | 总分：$totalScore"

        // 显示分析
        val message = """
            你选择了：
            "$selectedOption"
            
            📊 分析：
            ${optionAnalysis?.analysis ?: ""}
            
            得分：${if (score > 0) "+" else ""}$score
            
            ${if (selectedOption != keyPoint.correctResponse) {
                "✅ 更好的回答：\n${keyPoint.correctResponse}\n\n💡 ${keyPoint.analysis}"
            } else {
                "🎉 完美回答！"
            }}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("💡 回答分析")
            .setMessage(message)
            .setPositiveButton("继续对话") { _, _ ->
                Log.d(TAG, "用户点击继续对话")
                nextButton.isEnabled = true
                // 滚动到底部
                conversationScrollView.post {
                    conversationScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun showFinalScore() {
        val maxScore = keyPoints.sumOf { kp ->
            kp.optionAnalysis.maxOfOrNull { it.score } ?: 0
        }

        val percentage = if (maxScore > 0) (totalScore.toFloat() / maxScore * 100).toInt() else 0

        val evaluation = when {
            percentage >= 90 -> "🏆 完美！你的情商非常高！"
            percentage >= 70 -> "👍 很好！继续保持！"
            percentage >= 50 -> "😊 不错，还有提升空间"
            else -> "💪 需要多加练习哦"
        }

        AlertDialog.Builder(this)
            .setTitle("🎯 练习完成")
            .setMessage("""
                总分：$totalScore / $maxScore
                正确率：$percentage%
                
                $evaluation
            """.trimIndent())
            .setPositiveButton("返回菜单") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}