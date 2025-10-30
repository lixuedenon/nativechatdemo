// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/chat/MessageAdapter.kt
// 文件名：MessageAdapter.kt
// 文件类型：class（RecyclerView.Adapter）
// 状态：✅ 新建文件
// 创建日期：2025-10-28
// 作者：Claude
//
// 功能说明：
// 1. 聊天消息列表的RecyclerView适配器
// 2. 区分用户消息和AI消息
// 3. 支持消息引用功能
// 4. 支持长按复制消息
// 5. 支持消息时间戳显示
//
// 使用示例：
// val adapter = MessageAdapter(emptyList())
// recyclerView.adapter = adapter
// adapter.updateMessages(newMessages)
//
// 依赖的实体：
// - Message.kt（消息实体）

package com.example.nativechatdemo.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.model.Message
import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天消息列表适配器
 *
 * 支持功能：
 * 1. 用户消息和AI消息显示（左右布局）
 * 2. 消息引用显示
 * 3. 时间戳显示
 * 4. 长按复制消息
 * 5. DiffUtil优化刷新性能
 */
class MessageAdapter(
    private var messages: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        // 消息类型
        private const val VIEW_TYPE_USER = 1      // 用户消息（右侧）
        private const val VIEW_TYPE_AI = 2        // AI消息（左侧）

        // 时间格式化
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    }

    /**
     * 判断消息类型
     */
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_AI
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_AI -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai, parent, false)
                AiMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    /**
     * 绑定数据
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AiMessageViewHolder -> holder.bind(message)
        }
    }

    /**
     * 消息总数
     */
    override fun getItemCount(): Int = messages.size

    /**
     * 更新消息列表（使用DiffUtil优化）
     */
    fun updateMessages(newMessages: List<Message>) {
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages = newMessages
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * 用户消息ViewHolder
     */
    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.messageContent)
        private val timeText: TextView = itemView.findViewById(R.id.messageTime)
        private val quoteText: TextView? = itemView.findViewById(R.id.messageQuote)

        fun bind(message: Message) {
            // 显示内容
            contentText.text = message.content

            // 显示时间
            timeText.text = formatTime(message.timestamp)

            // 显示引用（如果有）
            if (message.quoteContent != null && quoteText != null) {
                quoteText.visibility = View.VISIBLE
                quoteText.text = "引用：${message.quoteContent}"
            } else {
                quoteText?.visibility = View.GONE
            }

            // 长按复制
            itemView.setOnLongClickListener {
                copyToClipboard(itemView.context, message.content)
                true
            }
        }
    }

    /**
     * AI消息ViewHolder
     */
    inner class AiMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.messageContent)
        private val timeText: TextView = itemView.findViewById(R.id.messageTime)
        private val quoteText: TextView? = itemView.findViewById(R.id.messageQuote)

        fun bind(message: Message) {
            // 显示内容
            contentText.text = message.content

            // 显示时间
            timeText.text = formatTime(message.timestamp)

            // 显示引用（如果有）
            if (message.quoteContent != null && quoteText != null) {
                quoteText.visibility = View.VISIBLE
                quoteText.text = "引用：${message.quoteContent}"
            } else {
                quoteText?.visibility = View.GONE
            }

            // 长按复制
            itemView.setOnLongClickListener {
                copyToClipboard(itemView.context, message.content)
                true
            }
        }
    }

    /**
     * 格式化时间戳
     *
     * 规则：
     * - 今天的消息：只显示时间 "14:30"
     * - 其他日期：显示日期+时间 "10-28 14:30"
     */
    private fun formatTime(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val today = Calendar.getInstance()

        return if (messageDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            messageDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        ) {
            // 今天的消息，只显示时间
            timeFormat.format(Date(timestamp))
        } else {
            // 其他日期，显示日期+时间
            dateFormat.format(Date(timestamp))
        }
    }

    /**
     * 复制消息到剪贴板
     */
    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
}

/**
 * DiffUtil回调
 *
 * 用于计算列表差异，优化RecyclerView刷新性能
 */
class MessageDiffCallback(
    private val oldList: List<Message>,
    private val newList: List<Message>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    /**
     * 判断是否是同一个Item
     * 通过消息ID判断
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    /**
     * 判断内容是否相同
     * 通过消息内容、时间戳等判断
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldList[oldItemPosition]
        val newMessage = newList[newItemPosition]

        return oldMessage.content == newMessage.content &&
                oldMessage.timestamp == newMessage.timestamp &&
                oldMessage.isUser == newMessage.isUser &&
                oldMessage.quoteContent == newMessage.quoteContent
    }
}

// ========================================
// 📋 使用示例
// ========================================
//
// 1. 在ChatActivity中初始化：
//
// class ChatActivity : AppCompatActivity() {
//     private lateinit var recyclerView: RecyclerView
//     private lateinit var messageAdapter: MessageAdapter
//
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         setContentView(R.layout.activity_chat)
//
//         // 初始化RecyclerView
//         recyclerView = findViewById(R.id.messageRecyclerView)
//         recyclerView.layoutManager = LinearLayoutManager(this).apply {
//             stackFromEnd = true  // 从底部开始堆叠
//         }
//
//         // 初始化Adapter
//         messageAdapter = MessageAdapter(emptyList())
//         recyclerView.adapter = messageAdapter
//     }
// }
//
// ----------------------------------------
//
// 2. 观察ViewModel更新消息列表：
//
// lifecycleScope.launch {
//     viewModel.messages.collect { messages ->
//         // 更新消息列表
//         messageAdapter.updateMessages(messages)
//
//         // 滚动到最新消息
//         if (messages.isNotEmpty()) {
//             recyclerView.scrollToPosition(messages.size - 1)
//         }
//     }
// }
//
// ========================================
// 📦 依赖的布局文件
// ========================================
//
// 需要创建以下布局文件：
//
// 1. item_message_user.xml（用户消息，靠右）
// 2. item_message_ai.xml（AI消息，靠左）
//
// 如果你还没有这些布局文件，我可以为你生成。
// 布局示例见下方注释。
//
// ========================================
// 🎨 布局文件示例
// ========================================
//
// item_message_user.xml（用户消息）：
// <?xml version="1.0" encoding="utf-8"?>
// <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
//     android:layout_width="match_parent"
//     android:layout_height="wrap_content"
//     android:orientation="vertical"
//     android:gravity="end"
//     android:padding="8dp">
//
//     <!-- 引用消息（可选） -->
//     <TextView
//         android:id="@+id/messageQuote"
//         android:layout_width="wrap_content"
//         android:layout_height="wrap_content"
//         android:maxWidth="250dp"
//         android:padding="8dp"
//         android:background="#E0E0E0"
//         android:textSize="12sp"
//         android:textColor="#666666"
//         android:visibility="gone"
//         android:layout_marginBottom="4dp"/>
//
//     <!-- 消息气泡 -->
//     <LinearLayout
//         android:layout_width="wrap_content"
//         android:layout_height="wrap_content"
//         android:orientation="horizontal"
//         android:gravity="end">
//
//         <!-- 时间 -->
//         <TextView
//             android:id="@+id/messageTime"
//             android:layout_width="wrap_content"
//             android:layout_height="wrap_content"
//             android:layout_gravity="bottom"
//             android:textSize="10sp"
//             android:textColor="#999999"
//             android:layout_marginEnd="4dp"/>
//
//         <!-- 消息内容 -->
//         <TextView
//             android:id="@+id/messageContent"
//             android:layout_width="wrap_content"
//             android:layout_height="wrap_content"
//             android:maxWidth="250dp"
//             android:padding="12dp"
//             android:background="@drawable/bg_message_user"
//             android:textSize="16sp"
//             android:textColor="#FFFFFF"/>
//     </LinearLayout>
// </LinearLayout>
//
// ----------------------------------------
//
// item_message_ai.xml（AI消息）：
// <?xml version="1.0" encoding="utf-8"?>
// <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
//     android:layout_width="match_parent"
//     android:layout_height="wrap_content"
//     android:orientation="vertical"
//     android:gravity="start"
//     android:padding="8dp">
//
//     <!-- 引用消息（可选） -->
//     <TextView
//         android:id="@+id/messageQuote"
//         android:layout_width="wrap_content"
//         android:layout_height="wrap_content"
//         android:maxWidth="250dp"
//         android:padding="8dp"
//         android:background="#E0E0E0"
//         android:textSize="12sp"
//         android:textColor="#666666"
//         android:visibility="gone"
//         android:layout_marginBottom="4dp"/>
//
//     <!-- 消息气泡 -->
//     <LinearLayout
//         android:layout_width="wrap_content"
//         android:layout_height="wrap_content"
//         android:orientation="horizontal"
//         android:gravity="start">
//
//         <!-- 消息内容 -->
//         <TextView
//             android:id="@+id/messageContent"
//             android:layout_width="wrap_content"
//             android:layout_height="wrap_content"
//             android:maxWidth="250dp"
//             android:padding="12dp"
//             android:background="@drawable/bg_message_ai"
//             android:textSize="16sp"
//             android:textColor="#333333"/>
//
//         <!-- 时间 -->
//         <TextView
//             android:id="@+id/messageTime"
//             android:layout_width="wrap_content"
//             android:layout_height="wrap_content"
//             android:layout_gravity="bottom"
//             android:textSize="10sp"
//             android:textColor="#999999"
//             android:layout_marginStart="4dp"/>
//     </LinearLayout>
// </LinearLayout>
//
// ========================================
// 🎨 背景Drawable示例
// ========================================
//
// res/drawable/bg_message_user.xml（用户消息气泡）：
// <?xml version="1.0" encoding="utf-8"?>
// <shape xmlns:android="http://schemas.android.com/apk/res/android">
//     <solid android:color="#4CAF50"/>
//     <corners android:radius="12dp"/>
// </shape>
//
// ----------------------------------------
//
// res/drawable/bg_message_ai.xml（AI消息气泡）：
// <?xml version="1.0" encoding="utf-8"?>
// <shape xmlns:android="http://schemas.android.com/apk/res/android">
//     <solid android:color="#F0F0F0"/>
//     <corners android:radius="12dp"/>
// </shape>
//
// ========================================