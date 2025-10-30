// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/chat/ChatActivity.kt
// 文件名：ChatActivity.kt
// 文件类型：class（Activity）
// 状态：⚠️ 修改现有文件
// 功能：添加"AI思考中..."提示、错误提示优化、从Intent接收角色ID
// 修改内容：
//   1. 添加"AI思考中..."的ProgressBar或TextView
//   2. 观察ViewModel的aiTyping和errorMessage状态
//   3. 从Intent接收characterId和mode参数
// 创建日期：2025-10-15
// 最后修改：2025-10-28
// 作者：Claude

package com.example.nativechatdemo.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.example.nativechatdemo.R
import com.example.nativechatdemo.viewmodel.ChatViewModel
import com.example.nativechatdemo.data.database.AppDatabase

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var messageAdapter: MessageAdapter

    // 原有UI组件
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var favorTextView: TextView

    // ========== 新增：AI思考中提示 ==========
    private lateinit var aiTypingIndicator: LinearLayout  // 或者用TextView/ProgressBar
    private lateinit var aiTypingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // 初始化UI
        initViews()
        setupRecyclerView()

        // ========== 修改：从Intent接收参数 ==========
        val userId = intent.getLongExtra("userId", 1L)
        val characterId = intent.getLongExtra("characterId", -1L)
        val mode = intent.getStringExtra("mode") ?: "basic"

        if (characterId == -1L) {
            Toast.makeText(this, "角色ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 加载角色并初始化聊天
        loadCharacterAndInitChat(userId, characterId, mode)

        // 观察ViewModel状态
        observeViewModel()

        // 设置发送按钮
        setupSendButton()
    }

    /**
     * 修改方法：添加AI思考中提示的初始化
     */
    private fun initViews() {
        recyclerView = findViewById(R.id.messageRecyclerView)
        inputEditText = findViewById(R.id.inputEditText)
        sendButton = findViewById(R.id.sendButton)
        favorTextView = findViewById(R.id.favorTextView)

        // ========== 新增：AI思考中提示 ==========
        aiTypingIndicator = findViewById(R.id.aiTypingIndicator)
        aiTypingText = findViewById(R.id.aiTypingText)
        aiTypingIndicator.visibility = View.GONE  // 初始隐藏
    }

    /**
     * 新增方法：加载角色并初始化聊天
     */
    private fun loadCharacterAndInitChat(userId: Long, characterId: Long, mode: String) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getInstance(this@ChatActivity)
                val character = database.characterDao().getById(characterId)

                if (character == null) {
                    Toast.makeText(this@ChatActivity, "角色不存在", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // 设置标题为角色名称
                supportActionBar?.title = "与${character.name}聊天"

                // 初始化聊天
                viewModel.initChat(
                    userId = userId,
                    character = character,
                    mode = mode
                )

            } catch (e: Exception) {
                Toast.makeText(
                    this@ChatActivity,
                    "加载失败：${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true  // 从底部开始堆叠
            }
            adapter = messageAdapter
        }
    }

    /**
     * 修改方法：观察ViewModel状态
     */
    private fun observeViewModel() {
        // 观察消息列表
        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                messageAdapter.updateMessages(messages)
                // 滚动到最新消息
                if (messages.isNotEmpty()) {
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }

        // 观察好感度
        lifecycleScope.launch {
            viewModel.currentFavor.collect { favor ->
                favorTextView.text = "好感度：$favor/100"
            }
        }

        // ========== 新增：观察AI思考状态 ==========
        lifecycleScope.launch {
            viewModel.aiTyping.collect { isTyping ->
                if (isTyping) {
                    aiTypingIndicator.visibility = View.VISIBLE
                    aiTypingText.text = "AI正在思考..."
                    sendButton.isEnabled = false  // 禁用发送按钮
                } else {
                    aiTypingIndicator.visibility = View.GONE
                    sendButton.isEnabled = true   // 恢复发送按钮
                }
            }
        }

        // ========== 新增：观察错误信息 ==========
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                if (error != null) {
                    // 显示错误提示
                    Toast.makeText(this@ChatActivity, error, Toast.LENGTH_LONG).show()
                    // 或者显示在UI上
                    // errorTextView.text = error
                    // errorTextView.visibility = View.VISIBLE

                    // 清除错误（避免重复显示）
                    viewModel.clearError()
                }
            }
        }

        // ========== 新增：观察加载状态（可选）==========
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // 可以显示全局loading
                // progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * 设置发送按钮
     */
    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val content = inputEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                // 发送消息
                viewModel.sendMessage(content)
                // 清空输入框
                inputEditText.text.clear()
            }
        }
    }
}

// ========================================
// 📋 修改说明
// ========================================
//
// 需要在原有ChatActivity.kt中：
//
// 1. 添加新的UI组件声明（类顶部）：
//    private lateinit var aiTypingIndicator: LinearLayout
//    private lateinit var aiTypingText: TextView
//
// 2. 修改initViews()方法，添加：
//    aiTypingIndicator = findViewById(R.id.aiTypingIndicator)
//    aiTypingText = findViewById(R.id.aiTypingText)
//    aiTypingIndicator.visibility = View.GONE
//
// 3. 在observeViewModel()方法中添加3个新的观察：
//    - 观察aiTyping状态
//    - 观察errorMessage状态
//    - 观察isLoading状态（可选）
//
// 4. 修改onCreate()方法，改为从Intent接收参数：
//    val userId = intent.getLongExtra("userId", 1L)
//    val characterId = intent.getLongExtra("characterId", -1L)
//    val mode = intent.getStringExtra("mode") ?: "basic"
//    loadCharacterAndInitChat(userId, characterId, mode)
//
// 5. 添加新方法：
//    - loadCharacterAndInitChat()
//
// 6. 在activity_chat.xml中添加AI思考中提示的布局：
//    <!-- 在RecyclerView上方添加 -->
//    <LinearLayout
//        android:id="@+id/aiTypingIndicator"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:orientation="horizontal"
//        android:gravity="center"
//        android:padding="8dp"
//        android:background="#F0F0F0"
//        android:visibility="gone">
//
//        <ProgressBar
//            android:layout_width="20dp"
//            android:layout_height="20dp"
//            style="?android:attr/progressBarStyleSmall"
//            android:layout_marginEnd="8dp"/>
//
//        <TextView
//            android:id="@+id/aiTypingText"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:text="AI正在思考..."
//            android:textSize="14sp"
//            android:textColor="#666666"/>
//    </LinearLayout>