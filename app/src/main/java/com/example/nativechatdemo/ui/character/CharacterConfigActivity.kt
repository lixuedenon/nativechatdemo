// 文件路径：app/src/main/java/com/example/nativechatdemo/ui/character/CharacterConfigActivity.kt
// 文件名：CharacterConfigActivity.kt
// 文件类型：class（Activity，继承AppCompatActivity）
// 状态：✅ 新建文件
// 功能：角色配置界面，支持选择预设模板或自定义4维度配置
// 依赖：
//   - androidx.appcompat（Activity基类）
//   - Material Design（UI组件）
//   - ViewBinding（视图绑定）
//   - CharacterTraits.kt（特征配置）
//   - Character.kt（角色模型）
// 引用：被以下文件调用
//   - MainActivity.kt（点击"配置角色"按钮进入）
// 启动的Activity：
//   - ChatActivity.kt（配置完成后进入聊天）
// 对应布局：activity_character_config.xml
// 创建日期：2025-10-28
// 作者：Claude

package com.example.nativechatdemo.ui.character

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import com.example.nativechatdemo.R
import com.example.nativechatdemo.data.database.AppDatabase
import com.example.nativechatdemo.data.model.*
import com.example.nativechatdemo.ui.chat.ChatActivity

/**
 * 角色配置Activity
 *
 * 功能：
 * 1. 显示6个预设模板供快速选择
 * 2. 支持自定义4维度配置
 * 3. 保存角色到数据库
 * 4. 配置完成后进入聊天界面
 *
 * UI流程：
 * Step 1: 选择模式（快速模板 / 自定义配置）
 * Step 2: 如果选模板，展示6个预设卡片
 * Step 3: 如果自定义，展示4维度配置界面
 * Step 4: 输入角色名称
 * Step 5: 保存并开始聊天
 */
class CharacterConfigActivity : AppCompatActivity() {

    // ========== UI组件 ==========
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var quickModeRadio: RadioButton
    private lateinit var customModeRadio: RadioButton

    // 快速模板区域
    private lateinit var templateRecyclerView: RecyclerView
    private lateinit var templateAdapter: TemplateAdapter

    // 自定义配置区域
    private lateinit var customConfigLayout: LinearLayout

    // 维度1：基础身份
    private lateinit var ageSlider: Slider
    private lateinit var ageValueText: TextView
    private lateinit var occupationSpinner: Spinner
    private lateinit var educationSpinner: Spinner

    // 维度2：性格类型
    private lateinit var personalitySpinner: Spinner
    private lateinit var profanitySpinner: Spinner
    private lateinit var emojiSpinner: Spinner

    // 维度3：兴趣爱好
    private lateinit var hobby1Spinner: Spinner
    private lateinit var hobby2Spinner: Spinner
    private lateinit var hobby3Spinner: Spinner

    // 维度4：社交风格
    private lateinit var proactivitySlider: Slider
    private lateinit var proactivityValueText: TextView
    private lateinit var opennessSlider: Slider
    private lateinit var opennessValueText: TextView
    private lateinit var chatHabitSpinner: Spinner

    // 角色名称和按钮
    private lateinit var characterNameInput: EditText
    private lateinit var saveButton: Button

    // ========== 数据 ==========
    private val database by lazy { AppDatabase.getInstance(this) }
    private val characterDao by lazy { database.characterDao() }
    private var selectedTemplate: CharacterTemplate? = null
    private val userId = 1L  // 简化处理，实际应从登录信息获取

    // ========== 生命周期 ==========

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_config)

        setupToolbar()
        initViews()
        setupModeToggle()
        setupTemplateList()
        setupCustomConfig()
        setupSaveButton()
    }

    /**
     * 设置工具栏
     */
    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "配置AI角色"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * 初始化所有View
     */
    private fun initViews() {
        // 模式切换
        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        quickModeRadio = findViewById(R.id.quickModeRadio)
        customModeRadio = findViewById(R.id.customModeRadio)

        // 快速模板
        templateRecyclerView = findViewById(R.id.templateRecyclerView)

        // 自定义配置区域
        customConfigLayout = findViewById(R.id.customConfigLayout)

        // 维度1
        ageSlider = findViewById(R.id.ageSlider)
        ageValueText = findViewById(R.id.ageValueText)
        occupationSpinner = findViewById(R.id.occupationSpinner)
        educationSpinner = findViewById(R.id.educationSpinner)

        // 维度2
        personalitySpinner = findViewById(R.id.personalitySpinner)
        profanitySpinner = findViewById(R.id.profanitySpinner)
        emojiSpinner = findViewById(R.id.emojiSpinner)

        // 维度3
        hobby1Spinner = findViewById(R.id.hobby1Spinner)
        hobby2Spinner = findViewById(R.id.hobby2Spinner)
        hobby3Spinner = findViewById(R.id.hobby3Spinner)

        // 维度4
        proactivitySlider = findViewById(R.id.proactivitySlider)
        proactivityValueText = findViewById(R.id.proactivityValueText)
        opennessSlider = findViewById(R.id.opennessSlider)
        opennessValueText = findViewById(R.id.opennessValueText)
        chatHabitSpinner = findViewById(R.id.chatHabitSpinner)

        // 名称和按钮
        characterNameInput = findViewById(R.id.characterNameInput)
        saveButton = findViewById(R.id.saveButton)
    }

    /**
     * 设置模式切换（快速/自定义）
     */
    private fun setupModeToggle() {
        modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.quickModeRadio -> {
                    // 显示模板列表，隐藏自定义配置
                    templateRecyclerView.visibility = View.VISIBLE
                    customConfigLayout.visibility = View.GONE
                }
                R.id.customModeRadio -> {
                    // 隐藏模板列表，显示自定义配置
                    templateRecyclerView.visibility = View.GONE
                    customConfigLayout.visibility = View.VISIBLE
                    selectedTemplate = null
                }
            }
        }

        // 默认选中快速模式
        quickModeRadio.isChecked = true
    }

    /**
     * 设置预设模板列表
     */
    private fun setupTemplateList() {
        val templates = CharacterTemplate.getAllTemplates()

        templateAdapter = TemplateAdapter(templates) { template ->
            selectedTemplate = template
            // 填充角色名称
            characterNameInput.setText(template.name)
        }

        templateRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CharacterConfigActivity)
            adapter = templateAdapter
        }
    }

    /**
     * 设置自定义配置区域
     */
    private fun setupCustomConfig() {
        // 维度1：年龄滑动条
        ageSlider.apply {
            valueFrom = 18f
            valueTo = 35f
            value = 22f
            addOnChangeListener { _, value, _ ->
                ageValueText.text = "${value.toInt()}岁"
            }
        }
        ageValueText.text = "22岁"

        // 维度1：职业下拉
        occupationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Occupation.getDisplayNames()
        )

        // 维度1：教育下拉
        educationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Education.getDisplayNames()
        )

        // 维度2：性格类型
        personalitySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            PersonalityType.getDisplayNames()
        )

        // 维度2：脏话程度
        profanitySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            ProfanityLevel.getDisplayNames()
        )

        // 维度2：表情使用
        emojiSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            EmojiLevel.getDisplayNames()
        )

        // 维度3：兴趣爱好（3个下拉框）
        val hobbyNames = listOf("不选") + Hobby.getDisplayNames()
        hobby1Spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hobbyNames)
        hobby2Spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hobbyNames)
        hobby3Spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hobbyNames)

        // 维度4：主动性
        proactivitySlider.apply {
            valueFrom = 0f
            valueTo = 10f
            value = 5f
            stepSize = 1f
            addOnChangeListener { _, value, _ ->
                proactivityValueText.text = "${value.toInt()}/10"
            }
        }
        proactivityValueText.text = "5/10"

        // 维度4：开放度
        opennessSlider.apply {
            valueFrom = 0f
            valueTo = 10f
            value = 5f
            stepSize = 1f
            addOnChangeListener { _, value, _ ->
                opennessValueText.text = "${value.toInt()}/10"
            }
        }
        opennessValueText.text = "5/10"

        // 维度4：聊天习惯
        chatHabitSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            ChatHabit.getDisplayNames()
        )
    }

    /**
     * 设置保存按钮
     */
    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val name = characterNameInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "请输入角色名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 检查是快速模式还是自定义模式
            if (quickModeRadio.isChecked) {
                // 快速模式：使用选中的模板
                if (selectedTemplate == null) {
                    Toast.makeText(this, "请选择一个模板", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                saveCharacterFromTemplate(name, selectedTemplate!!)
            } else {
                // 自定义模式：从UI读取配置
                saveCharacterFromCustom(name)
            }
        }
    }

    /**
     * 从模板保存角色
     */
    private fun saveCharacterFromTemplate(name: String, template: CharacterTemplate) {
        lifecycleScope.launch {
            try {
                val character = Character.fromTemplate(template, name)
                val characterId = characterDao.insert(character)

                Toast.makeText(this@CharacterConfigActivity, "角色创建成功", Toast.LENGTH_SHORT).show()
                startChat(characterId)
            } catch (e: Exception) {
                Toast.makeText(this@CharacterConfigActivity, "创建失败：${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 从自定义配置保存角色
     */
    private fun saveCharacterFromCustom(name: String) {
        try {
            // 读取维度1
            val age = ageSlider.value.toInt()
            val occupation = Occupation.values()[occupationSpinner.selectedItemPosition]
            val education = Education.values()[educationSpinner.selectedItemPosition]

            // 读取维度2
            val personalityType = PersonalityType.values()[personalitySpinner.selectedItemPosition]
            val profanityLevel = ProfanityLevel.values()[profanitySpinner.selectedItemPosition]
            val emojiLevel = EmojiLevel.values()[emojiSpinner.selectedItemPosition]

            // 读取维度3（兴趣爱好，过滤掉"不选"）
            val hobbies = mutableListOf<Hobby>()
            if (hobby1Spinner.selectedItemPosition > 0) {
                hobbies.add(Hobby.values()[hobby1Spinner.selectedItemPosition - 1])
            }
            if (hobby2Spinner.selectedItemPosition > 0) {
                hobbies.add(Hobby.values()[hobby2Spinner.selectedItemPosition - 1])
            }
            if (hobby3Spinner.selectedItemPosition > 0) {
                hobbies.add(Hobby.values()[hobby3Spinner.selectedItemPosition - 1])
            }

            // 读取维度4
            val proactivity = proactivitySlider.value.toInt()
            val openness = opennessSlider.value.toInt()
            val chatHabit = ChatHabit.values()[chatHabitSpinner.selectedItemPosition]

            // 构建CharacterTraits
            val traits = CharacterTraits(
                age = age,
                occupation = occupation,
                education = education,
                personalityType = personalityType,
                profanityLevel = profanityLevel,
                emojiLevel = emojiLevel,
                hobbies = hobbies,
                proactivity = proactivity,
                openness = openness,
                chatHabit = chatHabit
            )

            // 保存角色
            lifecycleScope.launch {
                try {
                    val character = Character.createCustom(name, traits)
                    val characterId = characterDao.insert(character)

                    Toast.makeText(this@CharacterConfigActivity, "角色创建成功", Toast.LENGTH_SHORT).show()
                    startChat(characterId)
                } catch (e: Exception) {
                    Toast.makeText(this@CharacterConfigActivity, "创建失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "配置有误：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 开始聊天
     */
    private fun startChat(characterId: Long) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("characterId", characterId)
            putExtra("mode", "basic")  // 基础对话模式
        }
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/**
 * 模板列表适配器
 */
class TemplateAdapter(
    private val templates: List<CharacterTemplate>,
    private val onTemplateSelected: (CharacterTemplate) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character_template, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val template = templates[position]
        holder.bind(template, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onTemplateSelected(template)
        }
    }

    override fun getItemCount() = templates.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emojiText: TextView = itemView.findViewById(R.id.templateEmoji)
        private val nameText: TextView = itemView.findViewById(R.id.templateName)
        private val descText: TextView = itemView.findViewById(R.id.templateDescription)
        private val cardView: com.google.android.material.card.MaterialCardView =
            itemView.findViewById(R.id.templateCard)

        fun bind(template: CharacterTemplate, isSelected: Boolean) {
            emojiText.text = template.emoji
            nameText.text = template.name
            descText.text = template.description

            // 选中状态
            cardView.strokeWidth = if (isSelected) 4 else 0
        }
    }
}