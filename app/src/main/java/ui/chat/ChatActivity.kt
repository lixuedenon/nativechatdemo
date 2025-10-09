package com.example.nativechatdemo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.custom.FavorLineView
import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var favorScrollView: HorizontalScrollView
    private lateinit var favorLineView: FavorLineView
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var favorabilityText: TextView
    private lateinit var roundsText: TextView

    private var currentCharacter: Character? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val userId = intent.getStringExtra("userId") ?: return finish()
        val characterId = intent.getStringExtra("characterId") ?: return finish()
        val characterName = intent.getStringExtra("characterName") ?: "AI"

        val character = Character(
            id = characterId,
            name = characterName,
            description = "",
            avatar = characterId,
            type = characterId.replace("_girl", "").replace("_boy", ""),
            gender = if (characterId.contains("boy")) "male" else "female"
        )

        currentCharacter = character

        // 初始化Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = characterName

        initViews()
        viewModel.initChat(userId, character)
        observeData()
        setupInput()
        setupKeyboardHandling()
    }

    // 🔥 重写这个方法来加载菜单
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    // 🔥 处理菜单项点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_stop -> {
                showStopDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showStopDialog() {
        AlertDialog.Builder(this)
            .setTitle("停止对话")
            .setMessage("确定要停止本次对话吗？")
            .setPositiveButton("确定") { _, _ ->
                handleStopConversation()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun handleStopConversation() {
        val conversation = viewModel.conversation.value
        if (conversation == null) {
            finish()
            return
        }

        val rounds = conversation.actualRounds

        if (rounds > 5) {
            AlertDialog.Builder(this)
                .setTitle("进入复盘")
                .setMessage("对话轮数: $rounds 轮\n准备查看复盘分析")
                .setPositiveButton("进入") { _, _ ->
                    startReviewActivity()
                }
                .setCancelable(false)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("对话太短")
                .setMessage("对话轮数不足6轮，无法生成复盘")
                .setPositiveButton("确定") { _, _ ->
                    finish()
                }
                .show()
        }
    }

    private fun startReviewActivity() {
        val intent = Intent(this, com.example.nativechatdemo.ui.review.ReviewActivity::class.java)

        val conversation = viewModel.conversation.value
        val character = currentCharacter

        intent.putExtra("conversationId", conversation?.id)
        intent.putExtra("userId", conversation?.userId)
        intent.putExtra("characterId", character?.id)
        intent.putExtra("characterName", character?.name)
        intent.putExtra("finalFavor", conversation?.currentFavorability)
        intent.putExtra("totalRounds", conversation?.actualRounds)
        intent.putExtra("favorPoints", conversation?.favorPoints)

        startActivity(intent)
        finish()
    }

    private fun initViews() {
        favorScrollView = findViewById(R.id.favorScrollView)
        favorLineView = findViewById(R.id.favorLineView)
        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        favorabilityText = findViewById(R.id.favorabilityText)
        roundsText = findViewById(R.id.roundsText)

        messageAdapter = MessageAdapter()
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }

        favorLineView.onPointClickListener = { point ->
            Log.d("ChatActivity", "点击柱子: round=${point.round}")

            val messagePosition = calculateMessagePosition(point.round)
            if (messagePosition >= 0) {
                recyclerView.smoothScrollToPosition(messagePosition)
            }

            val message = if (point.reason.isNotEmpty()) {
                "第${point.round}轮\n好感度: ${point.favor}%\n\n💡 变化原因:\n${point.reason}"
            } else {
                "第${point.round}轮\n好感度: ${point.favor}%"
            }

            val title = if (point.reason.isNotEmpty()) "好感度突破 🎉" else "好感度详情"

            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun calculateMessagePosition(round: Int): Int {
        if (round <= 0) return 0
        val currentMessages = viewModel.messages.value
        if (currentMessages.isEmpty()) return -1
        val targetPosition = (round - 1) * 2
        return if (targetPosition < currentMessages.size) targetPosition else currentMessages.size - 1
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.conversation.collect { conversation ->
                conversation?.let {
                    favorabilityText.text = "好感度: ${it.currentFavorability}"
                    roundsText.text = "轮数: ${it.actualRounds}/45"

                    Log.d("ChatActivity", "当前轮数: ${it.actualRounds}/45")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                messageAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.favorPoints.collect { points ->
                Log.d("ChatActivity", "更新好感线: ${points.size}个柱子")
                favorLineView.updatePoints(points)
                scrollToLatestBar(points.size)
            }
        }
    }

    private fun scrollToLatestBar(barCount: Int) {
        if (barCount == 0) return

        favorScrollView.post {
            val density = resources.displayMetrics.density
            val barWidth = 20f * density
            val barGap = 4f * density
            val lastBarIndex = barCount - 1
            val lastBarX = ((barWidth + barGap) * lastBarIndex).toInt()
            val scrollViewWidth = favorScrollView.width
            val singleBarWidth = barWidth.toInt()

            val scrollToX = if (lastBarX + singleBarWidth > scrollViewWidth) {
                (lastBarX + singleBarWidth - scrollViewWidth).coerceAtLeast(0)
            } else {
                0
            }

            favorScrollView.smoothScrollTo(scrollToX, 0)
        }
    }

    private fun setupInput() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun setupKeyboardHandling() {
        recyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                recyclerView.postDelayed({
                    val itemCount = messageAdapter.itemCount
                    if (itemCount > 0) {
                        recyclerView.scrollToPosition(itemCount - 1)
                    }
                }, 100)
            }
        }
    }

    private fun sendMessage() {
        val content = inputEditText.text.toString().trim()
        if (content.isEmpty() || content.length > 50) {
            return
        }

        val conversation = viewModel.conversation.value
        if (conversation != null) {
            Log.d("ChatActivity", "发送前检查：当前轮数=${conversation.actualRounds}")

            if (conversation.actualRounds >= 45) {
                Log.d("ChatActivity", "已达45轮上限，显示结束对话框")

                AlertDialog.Builder(this)
                    .setTitle("对话结束")
                    .setMessage("本次对话已达到45轮上限\n\n最终好感度: ${conversation.currentFavorability}%")
                    .setPositiveButton("确定") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setCancelable(false)
                    .show()

                inputEditText.isEnabled = false
                sendButton.isEnabled = false

                return
            }
        }

        Log.d("ChatActivity", "发送消息：$content")
        viewModel.sendMessage(content)
        inputEditText.text.clear()
        inputEditText.requestFocus()
    }

    private fun hideKeyboard() {
        inputEditText.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }
}