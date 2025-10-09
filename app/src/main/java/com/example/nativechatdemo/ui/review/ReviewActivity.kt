// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/review/ReviewActivity.kt
// 文件类型：Kotlin Class (Activity)
// 修改内容：返回箭头和返回键都正常返回上一页

package com.example.nativechatdemo.ui.review

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.custom.FavorLineView
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.ConversationAnalysis
import com.example.nativechatdemo.data.model.FavorPoint
import com.example.nativechatdemo.utils.MockAIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class ReviewActivity : AppCompatActivity() {

    private lateinit var favorLineView: FavorLineView
    private lateinit var roundsValue: TextView
    private lateinit var favorValue: TextView
    private lateinit var peaksValue: TextView
    private lateinit var summaryText: TextView
    private lateinit var analysisRecyclerView: RecyclerView
    private lateinit var analysisAdapter: AnalysisAdapter

    private var conversationId: String = ""
    private var userId: String = ""
    private var characterId: String = ""
    private var characterName: String = ""
    private var finalFavor = 0
    private var totalRounds = 0
    private val favorPoints = mutableListOf<FavorPoint>()
    private val analyses = mutableListOf<ConversationAnalysis>()

    private var loadingDialog: AlertDialog? = null

    companion object {
        private const val TAG = "ReviewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        Log.d(TAG, "=== ReviewActivity onCreate ===")

        conversationId = intent.getStringExtra("conversationId") ?: ""
        userId = intent.getStringExtra("userId") ?: ""
        characterId = intent.getStringExtra("characterId") ?: ""
        characterName = intent.getStringExtra("characterName") ?: ""
        finalFavor = intent.getIntExtra("finalFavor", 0)
        totalRounds = intent.getIntExtra("totalRounds", 0)
        val favorPointsJson = intent.getStringExtra("favorPoints")

        Log.d(TAG, "conversationId: $conversationId")
        Log.d(TAG, "characterName: $characterName")
        Log.d(TAG, "totalRounds: $totalRounds")
        Log.d(TAG, "finalFavor: $finalFavor")

        parseFavorPoints(favorPointsJson)

        // 🔥 启用返回箭头
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 🔥 移除禁用返回键的逻辑，允许正常返回
        // 不再添加OnBackPressedCallback，使用系统默认行为

        initViews()
        displayData()
        setupButtons()
        loadAnalysisData()
    }

    // 🔥 处理返回箭头点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // 返回箭头被点击，直接返回上一页
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun parseFavorPoints(json: String?) {
        if (json.isNullOrEmpty()) {
            Log.w(TAG, "favorPoints JSON为空")
            return
        }

        try {
            val jsonArray = JSONArray(json)
            Log.d(TAG, "解析favorPoints，数量: ${jsonArray.length()}")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val point = FavorPoint(
                    round = obj.getInt("round"),
                    favor = obj.getInt("favor"),
                    messageId = obj.getString("messageId"),
                    reason = obj.optString("reason", ""),
                    timestamp = obj.getLong("timestamp"),
                    favorChange = obj.optInt("favorChange", 0)
                )
                favorPoints.add(point)
            }
            Log.d(TAG, "成功解析 ${favorPoints.size} 个好感点")
        } catch (e: Exception) {
            Log.e(TAG, "解析favorPoints失败", e)
            e.printStackTrace()
        }
    }

    private fun initViews() {
        Log.d(TAG, "初始化Views")

        favorLineView = findViewById(R.id.favorLineView)
        roundsValue = findViewById(R.id.roundsValue)
        favorValue = findViewById(R.id.favorValue)
        peaksValue = findViewById(R.id.peaksValue)
        summaryText = findViewById(R.id.summaryText)
        analysisRecyclerView = findViewById(R.id.analysisRecyclerView)

        analysisAdapter = AnalysisAdapter(analyses)
        analysisRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReviewActivity)
            adapter = analysisAdapter
        }

        Log.d(TAG, "Views初始化完成")
    }

    private fun displayData() {
        Log.d(TAG, "显示数据")

        roundsValue.text = totalRounds.toString()
        val favorText = "$finalFavor%"
        favorValue.text = favorText

        val peaks = favorPoints.count { it.reason.isNotEmpty() }
        peaksValue.text = peaks.toString()

        favorLineView.updatePoints(favorPoints)

        val summary = generateSummary()
        summaryText.text = summary

        Log.d(TAG, "数据显示完成")
    }

    private fun generateSummary(): String {
        return when {
            finalFavor >= 80 -> "🎉 太棒了！你的表现非常出色，成功建立了深厚的好感。对话中你展现了良好的情商和沟通技巧，继续保持！"
            finalFavor >= 60 -> "👍 做得不错！整体表现良好，对话中有一些亮点。继续学习和实践，你会做得更好。"
            finalFavor >= 40 -> "💪 还不错！有进步空间。建议多注意对方的情绪反应，适时调整话题和语气。"
            finalFavor >= 20 -> "📚 需要加油！对话中有些地方可以改进。建议多练习，注意倾听和回应的技巧。"
            else -> "🔄 建议重新开始！这次对话效果不太理想。不要气馁，多练习几次会有明显进步。"
        }
    }

    private fun loadAnalysisData() {
        Log.d(TAG, "开始加载分析数据")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@ReviewActivity)
                val loadedAnalyses = withContext(Dispatchers.IO) {
                    db.conversationAnalysisDao().getAnalysisByConversationId(conversationId)
                }

                Log.d(TAG, "从数据库加载的分析数量: ${loadedAnalyses.size}")

                if (loadedAnalyses.isNotEmpty()) {
                    Log.d(TAG, "使用已有分析数据")
                    analyses.clear()
                    analyses.addAll(loadedAnalyses)
                    analysisAdapter.notifyDataSetChanged()
                } else {
                    Log.d(TAG, "没有已有分析，开始生成新分析")
                    generateAnalysis()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载分析数据失败", e)
                e.printStackTrace()
                Toast.makeText(this@ReviewActivity, "加载分析数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun generateAnalysis() {
        Log.d(TAG, "=== 开始生成分析 ===")
        showLoadingDialog("AI正在分析对话，请稍候...")

        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@ReviewActivity)

                val messages = db.messageDao().getMessagesByConversationId(conversationId)

                Log.d(TAG, "📊 总消息数: ${messages.size}")
                messages.forEachIndexed { index, msg ->
                    Log.d(TAG, "消息[$index]: isUser=${msg.isUser}, content='${msg.content}', favorChange=${msg.favorChange}")
                }

                val response = MockAIService.generateAnalysis(
                    messages = messages,
                    characterName = characterName,
                    finalFavor = finalFavor
                )

                Log.d(TAG, "📝 生成的JSON长度: ${response.length}")
                Log.d(TAG, "📝 生成的JSON内容: $response")

                val jsonArray = JSONArray(response)

                Log.d(TAG, "✅ 解析出的分析数量: ${jsonArray.length()}")

                val newAnalyses = mutableListOf<ConversationAnalysis>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val round = obj.getInt("round")
                    val userMsg = obj.getString("userMessage")
                    val aiMsg = obj.getString("aiMessage")

                    Log.d(TAG, "解析分析[$i]: round=$round, user='$userMsg', ai='$aiMsg'")

                    val analysis = ConversationAnalysis(
                        id = "analysis_${conversationId}_$i",
                        conversationId = conversationId,
                        round = round,
                        userMessageId = "",
                        aiMessageId = "",
                        userMessage = userMsg,
                        aiMessage = aiMsg,
                        analysis = obj.getString("analysis"),
                        suggestion = obj.getString("suggestion"),
                        createdAt = System.currentTimeMillis()
                    )
                    newAnalyses.add(analysis)
                }

                Log.d(TAG, "💾 准备保存 ${newAnalyses.size} 条分析到数据库")
                db.conversationAnalysisDao().insertAll(newAnalyses)
                Log.d(TAG, "✅ 保存成功")

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "🎨 更新UI，显示 ${newAnalyses.size} 条分析")
                    analyses.clear()
                    analyses.addAll(newAnalyses)
                    analysisAdapter.notifyDataSetChanged()
                    dismissLoadingDialog()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ 分析失败", e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    dismissLoadingDialog()
                    Toast.makeText(
                        this@ReviewActivity,
                        "分析失败: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showLoadingDialog(message: String) {
        Log.d(TAG, "显示加载对话框: $message")

        loadingDialog = AlertDialog.Builder(this)
            .setTitle("请稍候")
            .setMessage(message)
            .setCancelable(false)
            .create()

        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        Log.d(TAG, "关闭加载对话框")
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.sameReplyButton).setOnClickListener {
            handleReplyChoice("same")
        }

        findViewById<Button>(R.id.similarReplyButton).setOnClickListener {
            handleReplyChoice("similar")
        }

        findViewById<Button>(R.id.naturalReplyButton).setOnClickListener {
            handleReplyChoice("natural")
        }
    }

    private fun handleReplyChoice(type: String) {
        val message = when (type) {
            "same" -> "你选择了【相同回复】模式"
            "similar" -> "你选择了【相近回复】模式"
            "natural" -> "你选择了【自然回复】模式"
            else -> "未知模式"
        }

        Log.d(TAG, "用户选择训练方式: $type")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // TODO: 进入下一页（用户稍后会告诉你）
        Toast.makeText(this, "即将进入下一页...", Toast.LENGTH_SHORT).show()
    }
}