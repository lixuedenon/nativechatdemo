// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/chat/ChatActivity.kt
// 文件类型：Kotlin Class (Activity)
// 修改内容：修复编译错误，删除废弃的onBackPressed方法

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
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity
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
    private var gender: String = ""
    private var replayMode: String? = null
    private var originalConversationId: String? = null
    private var moduleType: String = "basic"

    companion object {
        private const val TAG = "ChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val userId = intent.getStringExtra("userId") ?: return finish()
        val characterId = intent.getStringExtra("characterId") ?: return finish()
        val characterName = intent.getStringExtra("characterName") ?: "AI"

        gender = intent.getStringExtra("gender") ?: ""
        replayMode = intent.getStringExtra("replayMode")
        originalConversationId = intent.getStringExtra("originalConversationId")
        moduleType = intent.getStringExtra("moduleType") ?: "basic"

        Log.d(TAG, "moduleType: $moduleType")

        val character = Character(
            id = characterId,
            name = characterName,
            description = "",
            avatar = characterId,
            type = characterId.replace("_girl", "").replace("_boy", ""),
            gender = if (characterId.contains("boy")) "male" else "female"
        )

        currentCharacter = character

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = when (moduleType) {
            "training" -> "与 $characterName 的故事"
            else -> characterName
        }

        initViews()
        viewModel.initChat(userId, character, replayMode, originalConversationId, moduleType)
        observeData()
        setupInput()
        setupKeyboardHandling()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (moduleType != "training") {
            menuInflater.inflate(R.menu.menu_chat, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackPressed()
                true
            }
            R.id.action_stop -> {
                showStopDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleBackPressed() {
        if (moduleType == "training") {
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要退出吗？对话进度将不会保存")
                .setPositiveButton("确定") { _, _ ->
                    exitToCharacterSelection()
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            finish()
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
        intent.putExtra("gender", gender)
        intent.putExtra("finalFavor", conversation?.currentFavorability)
        intent.putExtra("totalRounds", conversation?.actualRounds)
        intent.putExtra("favorPoints", conversation?.favorPoints)

        if (replayMode != null) {
            intent.putExtra("reviewType", "second")
            intent.putExtra("replayMode", replayMode)
            intent.putExtra("originalConversationId", originalConversationId)
        } else {
            intent.putExtra("reviewType", "first")
        }

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
            Log.d(TAG, "点击柱子: round=${point.round}")

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

                    roundsText.text = if (it.isTrainingMode) {
                        "轮数: ${it.actualRounds}"
                    } else {
                        "轮数: ${it.actualRounds}/45"
                    }

                    Log.d(TAG, "当前轮数: ${it.actualRounds}")
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
                Log.d(TAG, "更新好感线: ${points.size}个柱子")
                favorLineView.updatePoints(points)
                scrollToLatestBar(points.size)
            }
        }

        lifecycleScope.launch {
            viewModel.trainingEndingEvent.collect { event ->
                event?.let {
                    handleTrainingEnding(it)
                }
            }
        }
    }

    private fun handleTrainingEnding(event: ChatViewModel.TrainingEndingEvent) {
        Log.d(TAG, "========== handleTrainingEnding: ${event.type} ==========")

        when (event.type) {
            "revive" -> showReviveDialog()
            "final" -> showFinalEndingAndExit()
        }
    }

    private fun showReviveDialog() {
        Log.d(TAG, "========== showReviveDialog 开始 ==========")

        val conversation = viewModel.conversation.value
        if (conversation == null) {
            Log.e(TAG, "conversation 为 null！")
            return
        }

        val reviveCount = conversation.reviveCount
        Log.d(TAG, "当前续命次数: $reviveCount")

        try {
            AlertDialog.Builder(this)
                .setTitle("💔 时光流逝")
                .setMessage("你们已经走过了 ${conversation.actualRounds} 轮对话...\n\n是否继续这段感情？")
                .setPositiveButton("是，继续") { _, _ ->
                    Log.d(TAG, "点击了【是，继续】按钮")
                    showReviveStory(reviveCount + 1)
                }
                .setNegativeButton("否，结束") { _, _ ->
                    Log.d(TAG, "点击了【否，结束】按钮")
                    showFinalEndingAndExit()
                }
                .setCancelable(false)
                .show()

            Log.d(TAG, "续命对话框已显示")
        } catch (e: Exception) {
            Log.e(TAG, "显示续命对话框失败", e)
            e.printStackTrace()
        }
    }

    private fun showReviveStory(newReviveCount: Int) {
        Log.d(TAG, "========== showReviveStory 开始，续命次数: $newReviveCount ==========")

        val conversation = viewModel.conversation.value
        if (conversation == null) {
            Log.e(TAG, "conversation 为 null！")
            return
        }

        Log.d(TAG, "conversation.id: ${conversation.id}")
        Log.d(TAG, "conversation.trainingEndingType: ${conversation.trainingEndingType}")

        val story = when (newReviveCount) {
            1 -> "第1次续命\n\n因为你的爱，奇迹发生了...\n\n你们还能继续在一起。"
            2 -> "第2次续命\n\n爱的力量再次延续了时光...\n\n请珍惜剩下的时间。"
            3 -> "第3次续命（最后一次）\n\n这是最后一次机会了...\n\n好好珍惜彼此。"
            else -> "续命故事（第${newReviveCount}次）"
        }

        Log.d(TAG, "续命故事内容: $story")

        try {
            val dialog = AlertDialog.Builder(this)
                .setTitle("✨ 爱的奇迹")
                .setMessage(story)
                .setPositiveButton("继续对话 💬") { _, _ ->
                    Log.d(TAG, "点击了【继续对话】按钮")
                    try {
                        viewModel.updateReviveCount(newReviveCount)
                        Log.d(TAG, "调用 updateReviveCount 成功")
                    } catch (e: Exception) {
                        Log.e(TAG, "调用 updateReviveCount 失败", e)
                        e.printStackTrace()
                    }
                }
                .setCancelable(false)
                .create()

            dialog.show()
            Log.d(TAG, "续命故事对话框已显示")

        } catch (e: Exception) {
            Log.e(TAG, "显示续命故事对话框失败", e)
            e.printStackTrace()
        }
    }

    private fun showFinalEndingAndExit() {
        Log.d(TAG, "========== showFinalEndingAndExit 开始 ==========")

        viewModel.resetTrainingEvent()

        val conversation = viewModel.conversation.value
        if (conversation == null) {
            Log.e(TAG, "conversation 为 null！")
            exitToCharacterSelection()
            return
        }

        val story = "故事的终章\n\n虽然要离开了，但你们的回忆会永远留在心中...\n\n谢谢你陪我走过这段旅程。"

        Log.d(TAG, "结束故事内容: $story")

        try {
            AlertDialog.Builder(this)
                .setTitle("💫 故事的终章")
                .setMessage(story)
                .setPositiveButton("离开") { _, _ ->
                    Log.d(TAG, "点击了【离开】按钮")
                    exitToCharacterSelection()
                }
                .setCancelable(false)
                .show()

            Log.d(TAG, "结束故事对话框已显示")

        } catch (e: Exception) {
            Log.e(TAG, "显示结束故事对话框失败", e)
            e.printStackTrace()
            exitToCharacterSelection()
        }
    }

    private fun exitToCharacterSelection() {
        Log.d(TAG, "========== exitToCharacterSelection ==========")

        try {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.putExtra("gender", gender)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "退出到角色选择页失败", e)
            e.printStackTrace()
            finish()
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
            Log.d(TAG, "发送前检查：当前轮数=${conversation.actualRounds}")

            if (!conversation.isTrainingMode && conversation.actualRounds >= 45) {
                Log.d(TAG, "已达45轮上限，进入复盘页面")

                AlertDialog.Builder(this)
                    .setTitle("对话结束")
                    .setMessage("本次对话已达到45轮上限\n\n最终好感度: ${conversation.currentFavorability}%")
                    .setPositiveButton("进入复盘") { dialog, _ ->
                        dialog.dismiss()
                        startReviewActivity()
                    }
                    .setCancelable(false)
                    .show()

                inputEditText.isEnabled = false
                sendButton.isEnabled = false

                return
            }
        }

        Log.d(TAG, "发送消息：$content")
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