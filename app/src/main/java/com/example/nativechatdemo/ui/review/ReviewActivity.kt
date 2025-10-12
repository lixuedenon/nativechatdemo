// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/review/ReviewActivity.kt
// 文件类型：Kotlin Class (Activity)
// 修改内容：支持首次复盘和二次复盘，动态切换按钮

package com.example.nativechatdemo.ui.review

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity
import com.example.nativechatdemo.ui.chat.ChatActivity
import com.example.nativechatdemo.ui.modules.FemaleModulesActivity
import com.example.nativechatdemo.ui.modules.MaleModulesActivity
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

    // 🔥 首次复盘按钮组
    private lateinit var firstReviewButtonsLayout: LinearLayout
    private lateinit var sameReplyButton: Button
    private lateinit var similarReplyButton: Button
    private lateinit var naturalReplyButton: Button

    // 🔥 二次复盘按钮组
    private lateinit var secondReviewButtonsLayout: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var changeCharacterButton: Button
    private lateinit var moreChallengesButton: Button

    private var conversationId: String = ""
    private var userId: String = ""
    private var characterId: String = ""
    private var characterName: String = ""
    private var gender: String = ""  // 🔥 新增：用于返回模块列表
    private var finalFavor = 0
    private var totalRounds = 0
    private var reviewType: String = "first"  // 🔥 新增："first" 或 "second"
    private var replayMode: String? = null    // 🔥 新增：训练模式
    private var originalConversationId: String? = null  // 🔥 新增：原对话ID

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

        // 🔥 接收参数
        conversationId = intent.getStringExtra("conversationId") ?: ""
        userId = intent.getStringExtra("userId") ?: ""
        characterId = intent.getStringExtra("characterId") ?: ""
        characterName = intent.getStringExtra("characterName") ?: ""
        gender = intent.getStringExtra("gender") ?: ""
        finalFavor = intent.getIntExtra("finalFavor", 0)
        totalRounds = intent.getIntExtra("totalRounds", 0)
        reviewType = intent.getStringExtra("reviewType") ?: "first"
        replayMode = intent.getStringExtra("replayMode")
        originalConversationId = intent.getStringExtra("originalConversationId")
        val favorPointsJson = intent.getStringExtra("favorPoints")

        Log.d(TAG, "reviewType: $reviewType")
        Log.d(TAG, "replayMode: $replayMode")
        Log.d(TAG, "originalConversationId: $originalConversationId")

        parseFavorPoints(favorPointsJson)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initViews()
        displayData()
        setupButtons()
        loadAnalysisData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
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

        // 🔥 初始化首次复盘按钮
        firstReviewButtonsLayout = findViewById(R.id.firstReviewButtonsLayout)
        sameReplyButton = findViewById(R.id.sameReplyButton)
        similarReplyButton = findViewById(R.id.similarReplyButton)
        naturalReplyButton = findViewById(R.id.naturalReplyButton)

        // 🔥 初始化二次复盘按钮
        secondReviewButtonsLayout = findViewById(R.id.secondReviewButtonsLayout)
        retryButton = findViewById(R.id.retryButton)
        changeCharacterButton = findViewById(R.id.changeCharacterButton)
        moreChallengesButton = findViewById(R.id.moreChallengesButton)

        analysisAdapter = AnalysisAdapter(analyses)
        analysisRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReviewActivity)
            adapter = analysisAdapter
        }

        Log.d(TAG, "Views初始化完成")
    }

    private fun displayData() {
        Log.d(TAG, "显示数据")

        // 🔥 根据reviewType显示不同的按钮组
        when (reviewType) {
            "first" -> {
                firstReviewButtonsLayout.visibility = View.VISIBLE
                secondReviewButtonsLayout.visibility = View.GONE
            }
            "second" -> {
                firstReviewButtonsLayout.visibility = View.GONE
                secondReviewButtonsLayout.visibility = View.VISIBLE
            }
        }

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

                    // 🔥 根据reviewType调用不同的分析方法
                    val response = if (reviewType == "second") {
                        // 二次复盘：获取原对话数据进行对比
                        val originalMessages = if (originalConversationId != null) {
                            db.messageDao().getMessagesByConversationId(originalConversationId!!)
                        } else {
                            emptyList()
                        }

                        MockAIService.generateSecondReviewAnalysis(
                            currentMessages = messages,
                            originalMessages = originalMessages,
                            characterName = characterName,
                            finalFavor = finalFavor
                        )
                    } else {
                        // 首次复盘：正常分析
                        MockAIService.generateAnalysis(
                            messages = messages,
                            characterName = characterName,
                            finalFavor = finalFavor
                        )
                    }

                    Log.d(TAG, "📝 生成的JSON长度: ${response.length}")

                    val jsonArray = JSONArray(response)
                    Log.d(TAG, "✅ 解析出的分析数量: ${jsonArray.length()}")

                    val newAnalyses = mutableListOf<ConversationAnalysis>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val analysis = ConversationAnalysis(
                            id = "analysis_${conversationId}_$i",
                            conversationId = conversationId,
                            round = obj.getInt("round"),
                            userMessageId = "",
                            aiMessageId = "",
                            userMessage = obj.getString("userMessage"),
                            aiMessage = obj.getString("aiMessage"),
                            analysis = obj.getString("analysis"),
                            suggestion = obj.getString("suggestion"),
                            createdAt = System.currentTimeMillis()
                        )
                        newAnalyses.add(analysis)
                    }

                    db.conversationAnalysisDao().insertAll(newAnalyses)

                    withContext(Dispatchers.Main) {
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
            loadingDialog = AlertDialog.Builder(this)
                .setTitle("请稍候")
                .setMessage(message)
                .setCancelable(false)
                .create()

            loadingDialog?.show()
        }

        private fun dismissLoadingDialog() {
            loadingDialog?.dismiss()
            loadingDialog = null
        }

        private fun setupButtons() {
            // 🔥 首次复盘按钮事件
            sameReplyButton.setOnClickListener {
                startReplayChat("same")
            }

            similarReplyButton.setOnClickListener {
                startReplayChat("similar")
            }

            naturalReplyButton.setOnClickListener {
                startReplayChat("natural")
            }

            // 🔥 二次复盘按钮事件
            retryButton.setOnClickListener {
                // 再来一次：使用相同的replayMode和originalConversationId
                startReplayChat(replayMode ?: "similar")
            }

            changeCharacterButton.setOnClickListener {
                // 选择其他对象：返回角色选择页
                val intent = Intent(this, CharacterSelectionActivity::class.java)
                intent.putExtra("gender", gender)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }

            moreChallengesButton.setOnClickListener {
                // 更多挑战：返回模块列表页
                val intent = if (gender == "female") {
                    Intent(this, MaleModulesActivity::class.java)
                } else {
                    Intent(this, FemaleModulesActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        // 🔥 启动复盘对话
        private fun startReplayChat(mode: String) {
            Log.d(TAG, "启动复盘对话，模式: $mode")

            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("characterId", characterId)
            intent.putExtra("characterName", characterName)
            intent.putExtra("gender", gender)
            intent.putExtra("replayMode", mode)

            // 🔥 如果是首次复盘，originalConversationId就是当前conversationId
            // 如果是二次复盘，继续使用之前的originalConversationId
            val originalId = originalConversationId ?: conversationId
            intent.putExtra("originalConversationId", originalId)

            startActivity(intent)
            finish()
        }
    }