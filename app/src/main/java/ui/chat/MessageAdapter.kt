// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/chat/MessageAdapter.kt

package com.example.nativechatdemo.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.model.Message

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    var onMessageLongClick: ((Message, View) -> Unit)? = null

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.sender == "user") {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)

        when (holder) {
            is SentMessageViewHolder -> {
                holder.bind(message)
                holder.itemView.setOnLongClickListener {
                    onMessageLongClick?.invoke(message, it)
                    true
                }
            }
            is ReceivedMessageViewHolder -> {
                holder.bind(message)
                holder.itemView.setOnLongClickListener {
                    onMessageLongClick?.invoke(message, it)
                    true
                }
            }
        }
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val quotedLayout: LinearLayout = itemView.findViewById(R.id.quotedLayout)
        private val quotedSenderText: TextView = itemView.findViewById(R.id.quotedSenderText)
        private val quotedContentText: TextView = itemView.findViewById(R.id.quotedContentText)

        fun bind(message: Message) {
            messageText.text = message.content

            // 显示引用内容
            if (message.quotedContent != null) {
                quotedLayout.visibility = View.VISIBLE
                quotedSenderText.text = if (message.quotedSender == "user") "你" else "对方"
                quotedContentText.text = message.quotedContent
            } else {
                quotedLayout.visibility = View.GONE
            }
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val quotedLayout: LinearLayout = itemView.findViewById(R.id.quotedLayout)
        private val quotedSenderText: TextView = itemView.findViewById(R.id.quotedSenderText)
        private val quotedContentText: TextView = itemView.findViewById(R.id.quotedContentText)

        fun bind(message: Message) {
            messageText.text = message.content

            // 显示引用内容
            if (message.quotedContent != null) {
                quotedLayout.visibility = View.VISIBLE
                quotedSenderText.text = if (message.quotedSender == "user") "你" else "对方"
                quotedContentText.text = message.quotedContent
            } else {
                quotedLayout.visibility = View.GONE
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}