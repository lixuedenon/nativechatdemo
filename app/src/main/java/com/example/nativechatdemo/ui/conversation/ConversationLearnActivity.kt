// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/conversation/ConversationLearnActivity.kt

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class ConversationLearnActivity : AppCompatActivity() {

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

    companion object {
        private const val TAG = "ConversationLearn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_learn)

        targetGender = intent.getStringExtra("targetGender") ?: "female"
        Log.d(TAG, "========== onCreate ==========")
        Log.d(TAG, "targetGender: $targetGender")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = "学习模式"
        toolbar.setNavigationOnClickListener { finish() }

        initViews()
        loadScenario()
    }

    private fun initViews() {
        Log.d(TAG, "initViews 开始")

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

        introDesc.text = """
            📚 学习模式说明：
            
            1. 你将观看一段完整的对话（10-15轮）
            2. 对话会逐句展示，点击"下一句"继续
            3. 遇到关键时刻会暂停并提示
            4. 系统会展示正确的回答方式和分析
            5. 学习完成后可以进入练习模式实战
            
            💡 提示：
            - 仔细观察对话中的每个细节
            - 注意对方的话术和意图
            - 思考为什么这样回答是最好的
            
            准备好了就开始学习吧！
        """.trimIndent()

        startButton.setOnClickListener {
            Log.d(TAG, "点击开始学习按钮")
            if (scenario != null) {
                Log.d(TAG, "场景存在，显示对话页")
                showConversationPage()
            } else {
                Log.w(TAG, "场景为空，显示提示")
                Toast.makeText(this, "暂无学习场景", Toast.LENGTH_SHORT).show()
            }
        }

        nextButton.setOnClickListener {
            showNextMessage()
        }

        finishButton.setOnClickListener {
            Toast.makeText(this, "学习完成！", Toast.LENGTH_SHORT).show()
            finish()
        }

        Log.d(TAG, "initViews 完成")
    }

    private fun loadScenario() {
        Log.d(TAG, "========== loadScenario 开始 ==========")
        Log.d(TAG, "targetGender: $targetGender")

        lifecycleScope.launch {
            try {
                Log.d(TAG, "开始异步加载场景")
                val db = AppDatabase.getDatabase(this@ConversationLearnActivity)
                Log.d(TAG, "数据库实例获取成功")

                val scenarios = withContext(Dispatchers.IO) {
                    val result = db.conversationScenarioDao().getScenariosByGender(targetGender)
                    Log.d(TAG, "数据库返回场景数量: ${result.size}")

                    // 打印每个场景的详细信息
                    result.forEachIndexed { index, s ->
                        Log.d(TAG, "场景[$index]: id=${s.id}, title=${s.title}, gender=${s.targetGender}")
                    }

                    result
                }

                Log.d(TAG, "协程返回，最终场景数量: ${scenarios.size}")

                if (scenarios.isNotEmpty()) {
                    scenario = scenarios.first()
                    parseScenarioData()
                    Log.d(TAG, "✅ 加载场景成功: ${scenario?.title}")
                    Log.d(TAG, "对话轮数: ${dialogueTurns.size}, 关键点数: ${keyPoints.size}")
                } else {
                    Log.w(TAG, "⚠️ 暂无学习场景")
                    runOnUiThread {
                        Toast.makeText(this@ConversationLearnActivity, "暂无学习场景，请稍后重试", Toast.LENGTH_LONG).show()
                    }
                }

                Log.d(TAG, "========== loadScenario 结束 ==========")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 加载场景失败", e)
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ConversationLearnActivity, "加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseScenarioData() {
        Log.d(TAG, "========== parseScenarioData 开始 ==========")

        scenario?.let { s ->
            // 解析对话
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
                Log.d(TAG, "✅ 解析对话数量: ${dialogueTurns.size}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 解析对话失败", e)
                e.printStackTrace()
            }

            // 解析关键点
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

                    val optionAnalysisList = mutableListOf<com.example.nativechatdemo.data.model.OptionAnalysis>()
                    val analysisArray = obj.getJSONArray("optionAnalysis")
                    for (j in 0 until analysisArray.length()) {
                        val aObj = analysisArray.getJSONObject(j)
                        optionAnalysisList.add(
                            com.example.nativechatdemo.data.model.OptionAnalysis(
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
                Log.d(TAG, "✅ 解析关键点数量: ${keyPoints.size}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ 解析关键点失败", e)
                e.printStackTrace()
            }
        }

        Log.d(TAG, "========== parseScenarioData 结束 ==========")
    }

    private fun showConversationPage() {
        introLayout.visibility = View.GONE
        conversationLayout.visibility = View.VISIBLE
        scenarioTitle.text = "场景：${scenario?.title ?: ""}"

        currentTurnIndex = 0
        currentMessageIndex = 0
        conversationContainer.removeAllViews()

        showNextMessage()
    }

    private fun showNextMessage() {
        if (currentTurnIndex >= dialogueTurns.size) {
            nextButton.visibility = View.GONE
            finishButton.visibility = View.VISIBLE
            return
        }

        val turn = dialogueTurns[currentTurnIndex]

        if (currentMessageIndex == 0) {
            addPartnerMessage(turn.partnerSays)
            currentMessageIndex = 1

            if (turn.isKeyPoint) {
                nextButton.isEnabled = false
                showKeyPointDialog(turn.index)
            }
        } else {
            addMyMessage(turn.mySays)
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

    private fun showKeyPointDialog(turnIndex: Int) {
        val keyPoint = keyPoints.find { it.atTurnIndex == turnIndex } ?: return

        AlertDialog.Builder(this)
            .setTitle("💡 关键时刻")
            .setMessage(keyPoint.warning)
            .setPositiveButton("查看正确回答") { _, _ ->
                showAnalysisDialog(keyPoint)
            }
            .setCancelable(false)
            .show()
    }

    private fun showAnalysisDialog(keyPoint: KeyPoint) {
        val message = """
            ✅ 正确回答：
            ${keyPoint.correctResponse}
            
            📊 分析：
            ${keyPoint.analysis}
            
            ❌ 错误示例：
            ${keyPoint.wrongOptions.joinToString("\n") { "• $it" }}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("💡 回答分析")
            .setMessage(message)
            .setPositiveButton("继续对话") { _, _ ->
                nextButton.isEnabled = true
            }
            .setCancelable(false)
            .show()
    }
}