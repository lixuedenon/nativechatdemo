// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/chat/ChatActivity.kt
package com.example.nativechatdemo.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.custom.FavorLineView
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.Character
import com.example.nativechatdemo.data.model.Conversation
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity
import com.example.nativechatdemo.custom.ConfessionTestActivity
import com.example.nativechatdemo.ui.review.ReviewActivity
import com.example.nativechatdemo.utils.CustomTraitConfig
import com.example.nativechatdemo.utils.FavorAnalyzer
import com.example.nativechatdemo.utils.TrainingStoryConfig
import com.example.nativechatdemo.viewmodel.ChatViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.json.JSONArray

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: ImageButton
    private var favorLineView: FavorLineView? = null
    private var favorText: TextView? = null
    private var roundText: TextView? = null
    private lateinit var confessionButton: FloatingActionButton

    // 引用预览相关
    private lateinit var quotePreviewLayout: LinearLayout
    private lateinit var quotePreviewSender: TextView
    private lateinit var quotePreviewContent: TextView
    private lateinit var quoteCancelButton: ImageButton
    private var currentQuotedMessage: com.example.nativechatdemo.data.model.Message? = null

    private var userId: String = ""
    private var characterId: String = ""
    private var characterName: String = ""
    private var gender: String = ""
    private var moduleType: String = "basic"
    private var replayMode: String? = null
    private var originalConversationId: String? = null
    private var customTraitId: String? = null
    private var customTraits: String? = null
    private var scenarioType: Int = 0

    // PopupWindow
    private var messageActionPopup: PopupWindow? = null

    // ✅ 输入区域的View，用于判断是否点击在输入区域
    private lateinit var inputAreaLayout: LinearLayout

    companion object {
        private const val TAG = "ChatActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userId = intent.getStringExtra("userId") ?: "test_user_001"
        characterId = intent.getStringExtra("characterId") ?: ""
        characterName = intent.getStringExtra("characterName") ?: ""
        gender = intent.getStringExtra("gender") ?: "female"
        moduleType = intent.getStringExtra("moduleType") ?: "basic"
        replayMode = intent.getStringExtra("replayMode")
        originalConversationId = intent.getStringExtra("originalConversationId")
        customTraitId = intent.getStringExtra("customTraitId")
        customTraits = intent.getStringExtra("customTraits")
        scenarioType = intent.getIntExtra("scenarioType", 0)

        Log.d(TAG, "=== ChatActivity onCreate ===")
        Log.d(TAG, "gender: $gender")

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        initViews()
        setupRecyclerView()
        setupToolbar()
        setupInputArea()
        setupQuotePreview()
        observeViewModel()
        initChat()

        inputEditText.requestFocus()
        inputEditText.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
            }
        })
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        inputAreaLayout = findViewById(R.id.inputAreaLayout)

        favorLineView = findViewById(R.id.favorLineView)
        favorText = findViewById(R.id.favorText)
        roundText = findViewById(R.id.roundText)
        confessionButton = findViewById(R.id.confessionButton)

        quotePreviewLayout = findViewById(R.id.quotePreviewLayout)
        quotePreviewSender = findViewById(R.id.quotePreviewSender)
        quotePreviewContent = findViewById(R.id.quotePreviewContent)
        quoteCancelButton = findViewById(R.id.quoteCancelButton)

        if (moduleType == "custom") {
            confessionButton.visibility = View.VISIBLE
            confessionButton.isEnabled = false
            confessionButton.setOnClickListener {
                showConfessionTest()
            }
        } else {
            confessionButton.visibility = View.GONE
        }

        Log.d(TAG, "Views初始化完成")
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()

        messageAdapter.onMessageLongClick = { message, view ->
            showMessageActionPopup(message, view)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
        Log.d(TAG, "RecyclerView设置完成")
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = when (moduleType) {
            "training" -> "与 $characterName 的故事"
            "custom" -> when (scenarioType) {
                1 -> "相识并初步了解"
                2 -> "刚刚相识并有初步了解"
                3 -> "尚未相识但有心仪目标"
                4 -> "刚刚相识但不了解"
                else -> characterName
            }
            else -> characterName
        }

        toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }

        Log.d(TAG, "Toolbar设置完成")
    }

    private fun setupInputArea() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        val buttonColor = if (gender == "male") {
            Color.parseColor("#00BCD4")
        } else {
            Color.parseColor("#FF69B4")
        }
        sendButton.setColorFilter(buttonColor)

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        Log.d(TAG, "输入区域设置完成")
    }

    private fun setupQuotePreview() {
        quoteCancelButton.setOnClickListener {
            cancelQuote()
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "开始观察ViewModel")

        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                Log.d(TAG, "收到消息更新，数量: ${messages.size}")

                val cleanMessages = messages.map { msg ->
                    msg.copy(content = FavorAnalyzer.extractCleanMessage(msg.content))
                }

                messageAdapter.submitList(cleanMessages) {
                    if (cleanMessages.isNotEmpty()) {
                        recyclerView.smoothScrollToPosition(cleanMessages.size - 1)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.conversation.collect { conversation ->
                conversation?.let {
                    Log.d(TAG, "对话状态更新: favor=${it.currentFavorability}, rounds=${it.actualRounds}")

                    favorText?.text = "${it.currentFavorability}%"
                    roundText?.text = "第${it.actualRounds}轮"

                    if (moduleType == "custom") {
                        confessionButton.isEnabled = it.confessionButtonEnabled
                        confessionButton.alpha = if (it.confessionButtonEnabled) 1.0f else 0.5f
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.favorPoints.collect { points ->
                Log.d(TAG, "好感度点更新，数量: ${points.size}")
                favorLineView?.updatePoints(points)
            }
        }

        lifecycleScope.launch {
            viewModel.trainingEndingEvent.collect { event ->
                event?.let {
                    Log.d(TAG, "收到养成模式事件: ${it.type}")
                    handleTrainingEnding(it)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.customSpecialEvent.collect { event ->
                event?.let {
                    Log.d(TAG, "收到定制模式事件: $it")
                    handleCustomSpecialEvent(it)
                }
            }
        }

        Log.d(TAG, "ViewModel观察设置完成")
    }

    private fun initChat() {
        Log.d(TAG, "初始化聊天")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@ChatActivity)

                val character = if (moduleType == "custom") {
                    Log.d(TAG, "定制模式，创建虚拟角色")
                    Character(
                        id = characterId,
                        name = characterName,
                        description = "定制的聊天对象",
                        avatar = "custom_avatar",
                        type = "custom",
                        gender = gender,
                        isVip = false
                    )
                } else {
                    Log.d(TAG, "其他模式，查询数据库")
                    val dbCharacter = db.characterDao().getCharacterById(characterId)

                    if (dbCharacter == null) {
                        Log.e(TAG, "找不到角色: $characterId")
                        Toast.makeText(this@ChatActivity, "角色不存在", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    Log.d(TAG, "找到角色: ${dbCharacter.name}")
                    dbCharacter
                }

                Log.d(TAG, "准备初始化ViewModel，角色: ${character.name}")

                viewModel.initChat(
                    userId = userId,
                    character = character,
                    replayMode = replayMode,
                    originalConversationId = originalConversationId,
                    moduleType = moduleType,
                    customTraitId = customTraitId,
                    customTraits = customTraits,
                    scenarioType = scenarioType
                )

                Log.d(TAG, "聊天初始化完成")
            } catch (e: Exception) {
                Log.e(TAG, "初始化聊天失败", e)
                Toast.makeText(this@ChatActivity, "初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun sendMessage() {
        val content = inputEditText.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "发送消息: $content")

        inputEditText.text.clear()

        // 如果有引用，带上引用信息发送
        if (currentQuotedMessage != null) {
            viewModel.sendMessageWithQuote(
                content,
                currentQuotedMessage!!.id,
                currentQuotedMessage!!.content,
                currentQuotedMessage!!.sender
            )
            cancelQuote()
        } else {
            viewModel.sendMessage(content)
        }

        inputEditText.requestFocus()
    }

    private fun showMessageActionPopup(message: com.example.nativechatdemo.data.model.Message, anchorView: View) {
        dismissPopup()

        val popupView = layoutInflater.inflate(R.layout.popup_message_actions, null)

        messageActionPopup = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            // ✅ 关键修改：设置不影响输入法
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            // ✅ 设置软键盘模式为不调整
            softInputMode = android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            // ✅ 设置点击外部可关闭
            isOutsideTouchable = true
        }

        // 复制
        popupView.findViewById<TextView>(R.id.actionCopy).setOnClickListener {
            copyMessageToClipboard(message.content)
            dismissPopup()
        }

        // 引用
        popupView.findViewById<TextView>(R.id.actionQuote).setOnClickListener {
            setQuotedMessage(message)
            dismissPopup()
        }

        // 多选
        popupView.findViewById<TextView>(R.id.actionMultiSelect).setOnClickListener {
            Toast.makeText(this, "多选功能开发中...", Toast.LENGTH_SHORT).show()
            dismissPopup()
        }

        // 翻译
        popupView.findViewById<TextView>(R.id.actionTranslate).setOnClickListener {
            translateMessage(message.content)
            dismissPopup()
        }

        // 显示在消息上方
        messageActionPopup?.showAsDropDown(anchorView, 0, -anchorView.height - 120, Gravity.CENTER)
    }

    private fun dismissPopup() {
        messageActionPopup?.dismiss()
        messageActionPopup = null
    }

    private fun copyMessageToClipboard(content: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", content)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "消息已复制: $content")
    }

    private fun setQuotedMessage(message: com.example.nativechatdemo.data.model.Message) {
        currentQuotedMessage = message

        quotePreviewLayout.visibility = View.VISIBLE
        quotePreviewSender.text = "引用：${if (message.sender == "user") "你" else "对方"}"
        quotePreviewContent.text = message.content

        inputEditText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)

        Toast.makeText(this, "已设置引用，发送时将带上引用", Toast.LENGTH_SHORT).show()
    }

    private fun cancelQuote() {
        currentQuotedMessage = null
        quotePreviewLayout.visibility = View.GONE
    }

    private fun translateMessage(content: String) {
        val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_PROCESS_TEXT, content)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "设备未安装翻译应用（如Google翻译）", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleTrainingEnding(event: ChatViewModel.TrainingEndingEvent) {
        Log.d(TAG, "处理养成模式结束事件: ${event.type}")

        when (event.type) {
            "revive" -> showReviveDialog()
            "final" -> showFinalEndingAndExit()
        }
    }

    private fun showReviveDialog() {
        val conversation = viewModel.conversation.value ?: return

        AlertDialog.Builder(this)
            .setTitle("💔 关键时刻")
            .setMessage("你们的关系遇到了考验...\n\n是否继续努力挽回？\n（剩余续命次数: ${3 - conversation.reviveCount}）")
            .setPositiveButton("是，继续") { _, _ ->
                showReviveStory(conversation.reviveCount + 1)
            }
            .setNegativeButton("否，结束") { _, _ ->
                showFinalEndingAndExit()
            }
            .setCancelable(false)
            .show()
    }

    private fun showReviveStory(newReviveCount: Int) {
        val conversation = viewModel.conversation.value ?: return
        val endingType = TrainingStoryConfig.EndingType.valueOf(
            conversation.trainingEndingType?.uppercase() ?: "SICK"
        )

        val story = TrainingStoryConfig.getReviveStory(newReviveCount, endingType, gender)

        AlertDialog.Builder(this)
            .setTitle("💫 续命")
            .setMessage(story)
            .setPositiveButton("继续对话") { _, _ ->
                viewModel.updateReviveCount(newReviveCount)
            }
            .setCancelable(false)
            .show()
    }

    private fun showFinalEndingAndExit() {
        viewModel.resetTrainingEvent()

        val conversation = viewModel.conversation.value ?: return
        val endingType = TrainingStoryConfig.EndingType.valueOf(
            conversation.trainingEndingType?.uppercase() ?: "SICK"
        )

        val story = TrainingStoryConfig.getFinalEndingStory(endingType, gender)

        AlertDialog.Builder(this)
            .setTitle("👋 再见")
            .setMessage(story)
            .setPositiveButton("离开") { _, _ ->
                exitToCharacterSelection()
            }
            .setCancelable(false)
            .show()
    }

    private fun handleCustomSpecialEvent(event: String) {
        Log.d(TAG, "处理定制模式事件: $event")

        when (event) {
            "breakup" -> {
                AlertDialog.Builder(this)
                    .setTitle("💔 对话结束")
                    .setMessage(CustomTraitConfig.generateFailureEnding())
                    .setPositiveButton("离开") { _, _ ->
                        exitToCharacterSelection()
                    }
                    .setCancelable(false)
                    .show()
            }
            "angry" -> {
                Toast.makeText(this, "对方似乎有些不开心...", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.resetCustomEvent()
    }

    private fun showConfessionTest() {
        val conversation = viewModel.conversation.value ?: return

        val intent = Intent(this, ConfessionTestActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("customTraitId", customTraitId)
        intent.putExtra("conversationId", conversation.id)
        intent.putExtra("currentFavor", conversation.currentFavorability)
        intent.putExtra("customTraits", customTraits)
        startActivity(intent)
    }

    private fun exitToCharacterSelection() {
        val intent = Intent(this, CharacterSelectionActivity::class.java)
        intent.putExtra("gender", gender)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun handleBackPressed() {
        if (moduleType == "training") {
            AlertDialog.Builder(this)
                .setTitle("确认退出")
                .setMessage("养成模式退出后对话进度不保存，确定要退出吗？")
                .setPositiveButton("退出") { _, _ ->
                    finish()
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (moduleType == "basic" && replayMode == null) {
            menuInflater.inflate(R.menu.menu_chat, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_review -> {
                showReviewDialog()
                true
            }
            android.R.id.home -> {
                handleBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showReviewDialog() {
        val conversation = viewModel.conversation.value ?: return

        if (conversation.actualRounds < 5) {
            Toast.makeText(this, "至少需要5轮对话才能复盘", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("结束对话")
            .setMessage("确定要结束当前对话并查看复盘吗？")
            .setPositiveButton("确定") { _, _ ->
                navigateToReview()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun navigateToReview() {
        val conversation = viewModel.conversation.value ?: return

        val intent = Intent(this, ReviewActivity::class.java)
        intent.putExtra("conversationId", conversation.id)
        intent.putExtra("userId", userId)
        intent.putExtra("characterId", characterId)
        intent.putExtra("characterName", characterName)
        intent.putExtra("gender", gender)
        intent.putExtra("finalFavor", conversation.currentFavorability)
        intent.putExtra("totalRounds", conversation.actualRounds)
        intent.putExtra("favorPoints", conversation.favorPoints)
        intent.putExtra("reviewType", "first")
        startActivity(intent)
        finish()
    }

    // ✅ 核心修改：简化逻辑，只要不点击输入区域就隐藏键盘
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            val view = currentFocus

            if (view is EditText) {
                // ✅ 如果 PopupWindow 正在显示，不隐藏键盘（避免长按松手时键盘消失）
                if (messageActionPopup?.isShowing == true) {
                    return super.dispatchTouchEvent(ev)
                }

                // ✅ 检查是否点击在输入区域（包括引用预览）
                val inputAreaRect = Rect()
                inputAreaLayout.getGlobalVisibleRect(inputAreaRect)

                // 同时检查引用预览区域
                val quoteRect = Rect()
                quotePreviewLayout.getGlobalVisibleRect(quoteRect)

                val clickX = ev.rawX.toInt()
                val clickY = ev.rawY.toInt()

                // 如果点击在输入区域或引用预览区域，不隐藏键盘
                if (inputAreaRect.contains(clickX, clickY) ||
                    (quotePreviewLayout.visibility == View.VISIBLE && quoteRect.contains(clickX, clickY))) {
                    return super.dispatchTouchEvent(ev)
                }

                // ✅ 其他所有情况，隐藏键盘
                view.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}