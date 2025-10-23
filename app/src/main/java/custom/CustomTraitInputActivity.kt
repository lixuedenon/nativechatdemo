// 文件路径：app/src/main/java/custom/CustomTraitInputActivity.kt

package com.example.nativechatdemo.custom  // ⭐ 修改这里

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.dao.CustomPartnerTraitDao
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.CustomPartnerTrait
import com.example.nativechatdemo.ui.chat.ChatActivity
import com.example.nativechatdemo.utils.CustomTraitConfig
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*

class CustomTraitInputActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var traitDao: CustomPartnerTraitDao

    private lateinit var traitChipGroups: Map<String, ChipGroup>
    private lateinit var customDescriptionEdit: EditText
    private lateinit var startChatButton: Button
    private lateinit var randomTraitButton: Button
    private lateinit var suggestionsText: TextView

    private var gender: String = "female"
    private var scenarioType: Int = 1
    private val selectedTraits = mutableSetOf<String>()
    private val userId = "test_user_001"

    companion object {
        private const val TAG = "CustomTraitInput"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_trait_input)

        gender = intent.getStringExtra("gender") ?: "female"
        scenarioType = intent.getIntExtra("scenarioType", 1)

        Log.d(TAG, "性别: $gender, 场景: $scenarioType")

        database = AppDatabase.getDatabase(this)
        traitDao = database.customPartnerTraitDao()

        setupToolbar()
        initViews()
        createTraitChips()
        setupButtons()

        if (scenarioType == 4) {
            randomGenerateTraits()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val scenarioName = CustomTraitConfig.ScenarioType.values()
            .find { it.value == scenarioType }?.title ?: ""
        supportActionBar?.title = scenarioName

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        customDescriptionEdit = findViewById(R.id.customDescriptionEdit)
        startChatButton = findViewById(R.id.startChatButton)
        randomTraitButton = findViewById(R.id.randomTraitButton)
        suggestionsText = findViewById(R.id.suggestionsText)

        traitChipGroups = mapOf(
            "性格" to findViewById(R.id.personalityChipGroup),
            "兴趣爱好" to findViewById(R.id.interestChipGroup),
            "情感特征" to findViewById(R.id.emotionChipGroup),
            "生活态度" to findViewById(R.id.lifestyleChipGroup),
            "社交特点" to findViewById(R.id.socialChipGroup)
        )

        if (scenarioType == 4) {
            findViewById<LinearLayout>(R.id.traitSelectionLayout).visibility = LinearLayout.GONE
            customDescriptionEdit.visibility = EditText.GONE
        }
    }

    private fun createTraitChips() {
        CustomTraitConfig.TRAIT_CATEGORIES.forEach { (category, traits) ->
            val chipGroup = traitChipGroups[category] ?: return@forEach

            traits.forEach { trait ->
                val chip = Chip(this).apply {
                    text = trait
                    isCheckable = true
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedTraits.add(trait)
                        } else {
                            selectedTraits.remove(trait)
                        }
                        updateSuggestions()
                    }
                }
                chipGroup.addView(chip)
            }
        }
    }

    private fun setupButtons() {
        startChatButton.setOnClickListener {
            if (selectedTraits.isEmpty() && scenarioType != 4) {
                Toast.makeText(this, "请至少选择一个特质", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveTraitAndStartChat()
        }

        randomTraitButton.setOnClickListener {
            randomGenerateTraits()
        }
    }

    private fun randomGenerateTraits() {
        selectedTraits.clear()
        traitChipGroups.values.forEach { chipGroup ->
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                chip?.isChecked = false
            }
        }

        CustomTraitConfig.TRAIT_CATEGORIES.forEach { (category, traits) ->
            val randomCount = (1..2).random()
            val randomTraits = traits.shuffled().take(randomCount)

            randomTraits.forEach { trait ->
                selectedTraits.add(trait)

                val chipGroup = traitChipGroups[category]
                for (i in 0 until (chipGroup?.childCount ?: 0)) {
                    val chip = chipGroup?.getChildAt(i) as? Chip
                    if (chip?.text == trait) {
                        chip.isChecked = true
                        break
                    }
                }
            }
        }

        updateSuggestions()

        if (scenarioType == 4) {
            Toast.makeText(this, "已随机生成特质", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSuggestions() {
        if (selectedTraits.isEmpty()) {
            suggestionsText.text = "请选择特质以获取聊天建议"
            return
        }

        val suggestions = CustomTraitConfig.generateChatSuggestions(selectedTraits.toList())
        val suggestionsStr = suggestions.joinToString("\n• ", "聊天建议：\n• ")
        suggestionsText.text = suggestionsStr
    }

    private fun saveTraitAndStartChat() {
        lifecycleScope.launch {
            try {
                val trait = CustomPartnerTrait(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    scenarioType = scenarioType,
                    gender = gender,
                    traitTags = JSONArray(selectedTraits.toList()).toString(),
                    customDescription = customDescriptionEdit.text.toString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    chatCount = 0,
                    lastChatDate = 0
                )

                withContext(Dispatchers.IO) {
                    traitDao.insertTrait(trait)
                }

                val personality = CustomTraitConfig.generatePersonalityDescription(selectedTraits.toList())

                AlertDialog.Builder(this@CustomTraitInputActivity)
                    .setTitle("角色生成成功")
                    .setMessage(personality)
                    .setPositiveButton("开始聊天") { _, _ ->
                        navigateToChat(trait.id)
                    }
                    .setCancelable(false)
                    .show()

            } catch (e: Exception) {
                Log.e(TAG, "保存特质失败", e)
                Toast.makeText(this@CustomTraitInputActivity, "保存失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToChat(traitId: String) {
        val intent = Intent(this, ChatActivity::class.java)

        val characterId = "custom_${gender}_$scenarioType"
        val characterName = if (gender == "female") "定制女友" else "定制男友"

        intent.putExtra("userId", userId)
        intent.putExtra("characterId", characterId)
        intent.putExtra("characterName", characterName)
        intent.putExtra("gender", gender)
        intent.putExtra("moduleType", "custom")
        intent.putExtra("customTraitId", traitId)
        intent.putExtra("scenarioType", scenarioType)
        intent.putExtra("customTraits", JSONArray(selectedTraits.toList()).toString())

        startActivity(intent)
        finish()
    }
}