// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/review/AnalysisAdapter.kt
// 文件类型：Kotlin Class (RecyclerView Adapter)

package com.example.nativechatdemo.ui.review

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.model.ConversationAnalysis

class AnalysisAdapter(
    private val analyses: List<ConversationAnalysis>
) : RecyclerView.Adapter<AnalysisAdapter.AnalysisViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalysisViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation_analysis, parent, false)
        return AnalysisViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnalysisViewHolder, position: Int) {
        holder.bind(analyses[position])
    }

    override fun getItemCount(): Int = analyses.size

    class AnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roundTitle: TextView = itemView.findViewById(R.id.roundTitle)
        private val aiMessageText: TextView = itemView.findViewById(R.id.aiMessageText)
        private val userMessageText: TextView = itemView.findViewById(R.id.userMessageText)
        private val analysisText: TextView = itemView.findViewById(R.id.analysisText)
        private val suggestionText: TextView = itemView.findViewById(R.id.suggestionText)

        fun bind(analysis: ConversationAnalysis) {
            roundTitle.text = "第 ${analysis.round} 轮"
            aiMessageText.text = analysis.aiMessage
            userMessageText.text = analysis.userMessage
            analysisText.text = analysis.analysis
            suggestionText.text = analysis.suggestion
        }
    }
}