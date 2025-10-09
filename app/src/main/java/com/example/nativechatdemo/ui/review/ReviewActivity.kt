package com.example.nativechatdemo.ui.review

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.nativechatdemo.R
import com.example.nativechatdemo.custom.FavorLineView
import com.example.nativechatdemo.data.model.FavorPoint
import org.json.JSONArray

class ReviewActivity : AppCompatActivity() {

    private lateinit var favorLineView: FavorLineView
    private lateinit var roundsValue: TextView
    private lateinit var favorValue: TextView
    private lateinit var peaksValue: TextView
    private lateinit var summaryText: TextView

    private var finalFavor = 0
    private var totalRounds = 0
    private val favorPoints = mutableListOf<FavorPoint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // 获取传入数据
        finalFavor = intent.getIntExtra("finalFavor", 0)
        totalRounds = intent.getIntExtra("totalRounds", 0)
        val favorPointsJson = intent.getStringExtra("favorPoints")

        // 解析好感线数据
        parseFavorPoints(favorPointsJson)

        // 设置Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // 🔥 新增：禁用返回键（使用新的API）
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 禁用返回键，必须选择一个选项
                Toast.makeText(this@ReviewActivity, "请选择一种训练方式继续", Toast.LENGTH_SHORT).show()
            }
        })

        initViews()
        displayData()
        setupButtons()
    }

    private fun parseFavorPoints(json: String?) {
        if (json.isNullOrEmpty()) return

        try {
            val jsonArray = JSONArray(json)
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViews() {
        favorLineView = findViewById(R.id.favorLineView)
        roundsValue = findViewById(R.id.roundsValue)
        favorValue = findViewById(R.id.favorValue)
        peaksValue = findViewById(R.id.peaksValue)
        summaryText = findViewById(R.id.summaryText)
    }

    private fun displayData() {
        // 显示概览数据
        roundsValue.text = totalRounds.toString()
        val favorText = "$finalFavor%"
        favorValue.text = favorText

        // 统计关键转折点
        val peaks = favorPoints.count { it.reason.isNotEmpty() }
        peaksValue.text = peaks.toString()

        // 显示好感线
        favorLineView.updatePoints(favorPoints)

        // 生成总评
        val summary = generateSummary()
        summaryText.text = summary
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

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // TODO: 进入下一页（用户稍后会告诉你）
        Toast.makeText(this, "即将进入下一页...", Toast.LENGTH_SHORT).show()
    }
}