// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/custom/ConfessionTestActivity.kt
// 文件类型：Kotlin Class (Activity)
// 文件状态：【新建】

package com.example.nativechatdemo.ui.custom

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.dao.ConfessionTestDao
import com.example.nativechatdemo.data.dao.ConversationDao
import com.example.nativechatdemo.data.dao.CustomPartnerTraitDao
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.ConfessionTest
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.utils.CustomPartnerService
import com.example.nativechatdemo.utils.CustomTraitConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

class ConfessionTestActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var confessionTestDao: ConfessionTestDao
    private lateinit var customTraitDao: CustomPartnerTraitDao

    private lateinit var progressBar: ProgressBar
    private lateinit var successRateText: TextView
    private lateinit var analysisText: TextView
    private lateinit var suggestionsText: TextView
    private lateinit var testTypeText: TextView
    private lateinit var resultContainer: View
    private lateinit var backButton: Button

    private var userId: String = ""
    private var customTraitId: String? = null
    private var conversationId: String = ""
    private var currentFavor: Int = 0
    private var customTraits: List<String> = emptyList()

    companion object {
        private const val TAG = "ConfessionTest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confession_test)

        // 获取参数
        userId = intent.getStringExtra("userId") ?: "test_user_001"
        customTraitId = intent.getStringExtra("customTraitId")
        conversationId = intent.getStringExtra("conversationId") ?: ""
        currentFavor = intent.getIntExtra("currentFavor", 50)

        val traitsJson = intent.getStringExtra("customTraits")
        if (traitsJson != null) {
            try {
                val jsonArray = JSONArray(traitsJson)
                customTraits = (0 until jsonArray.length()).map { jsonArray.getString(it) }
            } catch (e: Exception) {
                Log.e(TAG, "解析特质失败", e)
            }
        }

        // 初始化数据库
        database = AppDatabase.getDatabase(this)
        conversationDao = database.conversationDao()
        confessionTestDao = database.confessionTestDao()
        customTraitDao = database.customPartnerTraitDao()

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.confession_test_title)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // 初始化视图
        initViews()

        // 开始分析
        startAnalysis()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        successRateText = findViewById(R.id.successRateText)
        analysisText = findViewById(R.id.analysisText)
        suggestionsText = findViewById(R.id.suggestionsText)
        testTypeText = findViewById(R.id.testTypeText)
        resultContainer = findViewById(R.id.resultContainer)
        backButton = findViewById(R.id.backButton)

        resultContainer.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun startAnalysis() {
        lifecycleScope.launch {
            try {
                // 显示分析中
                showAnalyzing()

                // 获取相关对话记录
                val conversations = withContext(Dispatchers.IO) {
                    conversationDao.getConversationsByUser(userId)
                        .filter { it.moduleType == "custom" && it.customTraitId == customTraitId }
                }

                // 判断测试类型
                val testType = determineTestType(conversations)

                // 预测成功率
                val prediction = CustomPartnerService.predictConfessionSuccess(
                    userId = userId,
                    traits = customTraits,
                    conversations = conversations,
                    testType = testType
                )

                // 保存测试结果
                val test = ConfessionTest(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    traitId = customTraitId,
                    conversationIds = JSONArray(conversations.map { it.id }).toString(),
                    testType = testType,
                    successRate = prediction.successRate,
                    analysis = prediction.analysis,
                    suggestions = JSONArray(prediction.suggestions).toString(),
                    createdAt = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    confessionTestDao.insertTest(test)
                }

                // 模拟分析延迟
                delay(2000)

                // 显示结果
                showResult(prediction, testType)

            } catch (e: Exception) {
                Log.e(TAG, "分析失败", e)
                finish()
            }
        }
    }

    private fun determineTestType(conversations: List<Conversation>): Int {
        if (conversations.isEmpty()) return 1

        val currentConv = conversations.find { it.id == conversationId }
        if (currentConv == null) return 1

        return when {
            // 检查是否连续聊天（同一个对话超过3轮）
            currentConv.actualRounds >= 3 && currentConv.currentFavorability >= 40 -> 1

            // 检查是否重复尝试（同特质多次对话）
            conversations.size >= 3 -> 2

            // 检查是否多样尝试（需要查询其他特质）
            else -> 3
        }
    }

    private fun showAnalyzing() {
        val messages = listOf(
            "正在分析对话记录...",
            "评估性格匹配度...",
            "计算成功率...",
            "生成建议..."
        )

        lifecycleScope.launch {
            messages.forEach { msg ->
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.VISIBLE
                    successRateText.text = msg
                }
                delay(500)
            }
        }
    }

    private fun showResult(prediction: CustomPartnerService.ConfessionPrediction, testType: Int) {
        progressBar.visibility = View.GONE
        resultContainer.visibility = View.VISIBLE

        // 显示成功率（带动画）
        animateSuccessRate(prediction.successRate)

        // 显示测试类型
        val typeDesc = when (testType) {
            1 -> "连续型对话"
            2 -> "重复尝试型"
            3 -> "多样探索型"
            else -> ""
        }
        testTypeText.text = "测试类型：$typeDesc"

        // 显示分析
        analysisText.text = prediction.analysis

        // 显示建议
        val suggestionsStr = prediction.suggestions.joinToString("\n\n• ", "建议：\n\n• ")
        suggestionsText.text = suggestionsStr

        // 根据成功率改变文字颜色
        val color = when {
            prediction.successRate >= 70 -> getColor(R.color.success_green)
            prediction.successRate >= 40 -> getColor(R.color.warning_yellow)
            else -> getColor(R.color.error_red)
        }
        successRateText.setTextColor(color)
    }

    private fun animateSuccessRate(targetRate: Float) {
        lifecycleScope.launch {
            var currentRate = 0
            while (currentRate < targetRate) {
                currentRate = (currentRate + 2).coerceAtMost(targetRate.toInt())
                successRateText.text = "${currentRate}%"
                delay(30)
            }

            // 显示最终评价
            val evaluation = when {
                targetRate >= 80 -> "💕 非常高！大胆去表白吧！"
                targetRate >= 60 -> "💗 成功率不错，可以试试"
                targetRate >= 40 -> "💛 还需要继续努力"
                targetRate >= 20 -> "💙 建议先培养感情"
                else -> "💔 时机还不成熟"
            }

            successRateText.text = "${targetRate.toInt()}%\n$evaluation"
        }
    }
}