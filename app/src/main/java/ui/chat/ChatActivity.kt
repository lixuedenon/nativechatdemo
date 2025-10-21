// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/chat/ChatActivity.kt
// 文件类型：Kotlin Class (Activity)
// 文件状态：【修改】
// 修改内容：修复import和引用错误

package com.example.nativechatdemo.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
import com.example.nativechatdemo.data.model.Conversation  // 添加这个import
import com.example.nativechatdemo.ui.character.CharacterSelectionActivity
import com.example.nativechatdemo.ui.custom.ConfessionTestActivity
import com.example.nativechatdemo.utils.CustomTraitConfig  // 添加这个import
import com.example.nativechatdemo.viewmodel.ChatViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.json.JSONArray

class ChatActivity : AppCompatActivity() {
    // ... 其余代码保持不变，主要是添加了缺失的import
}