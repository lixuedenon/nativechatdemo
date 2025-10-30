// 文件路径：app/src/main/java/com/example/nativechatdemo/data/model/CharacterTraits.kt
// 文件名：CharacterTraits.kt
// 类型：Model（数据类）
// 功能：基础对话模块的4维度角色特征配置，支持JSON序列化和反序列化
// 依赖：
//   - org.json.JSONObject（JSON解析）
// 引用：被以下文件使用
//   - Character.kt（存储在数据库中）
//   - PromptBuilder.kt（构建AI的System Prompt）
//   - CharacterConfigActivity.kt（配置界面的数据绑定）
// 创建日期：2025-10-28
// 最后修改：2025-10-28
// 作者：Claude

package com.example.nativechatdemo.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * 角色特征配置类（基础对话模块 - 4维度简化版）
 * 
 * 4个核心维度：
 * 1. 基础身份：年龄、职业、教育程度
 * 2. 性格类型：6种预设性格 + 脏话/表情微调
 * 3. 兴趣爱好：最多3个爱好领域
 * 4. 社交风格：主动性、开放度、回复速度
 */
data class CharacterTraits(
    // ========== 维度1：基础身份 ==========
    val age: Int,                           // 年龄：18-35
    val occupation: Occupation,             // 职业类型
    val education: Education,               // 教育程度
    
    // ========== 维度2：性格类型 ==========
    val personalityType: PersonalityType,   // 性格类型（6选1）
    val profanityLevel: ProfanityLevel,     // 脏话程度
    val emojiLevel: EmojiLevel,             // 表情使用频率
    
    // ========== 维度3：兴趣爱好 ==========
    val hobbies: List<Hobby>,               // 兴趣爱好列表（最多3个）
    
    // ========== 维度4：社交风格 ==========
    val proactivity: Int,                   // 主动性：0-10（0=极被动，10=超主动）
    val openness: Int,                      // 开放度：0-10（0=保守，10=开放）
    val chatHabit: ChatHabit                // 聊天习惯（回复速度）
) {
    
    /**
     * 转换为JSON字符串（用于存储到数据库）
     */
    fun toJson(): String {
        return JSONObject().apply {
            // 维度1：基础身份
            put("age", age)
            put("occupation", occupation.name)
            put("education", education.name)
            
            // 维度2：性格类型
            put("personalityType", personalityType.name)
            put("profanityLevel", profanityLevel.name)
            put("emojiLevel", emojiLevel.name)
            
            // 维度3：兴趣爱好
            put("hobbies", JSONArray().apply {
                hobbies.forEach { put(it.name) }
            })
            
            // 维度4：社交风格
            put("proactivity", proactivity)
            put("openness", openness)
            put("chatHabit", chatHabit.name)
        }.toString()
    }
    
    companion object {
        /**
         * 从JSON字符串解析（从数据库读取）
         */
        fun fromJson(json: String): CharacterTraits {
            val obj = JSONObject(json)
            
            // 解析兴趣爱好数组
            val hobbiesArray = obj.getJSONArray("hobbies")
            val hobbiesList = mutableListOf<Hobby>()
            for (i in 0 until hobbiesArray.length()) {
                hobbiesList.add(Hobby.valueOf(hobbiesArray.getString(i)))
            }
            
            return CharacterTraits(
                // 维度1
                age = obj.getInt("age"),
                occupation = Occupation.valueOf(obj.getString("occupation")),
                education = Education.valueOf(obj.getString("education")),
                
                // 维度2
                personalityType = PersonalityType.valueOf(obj.getString("personalityType")),
                profanityLevel = ProfanityLevel.valueOf(obj.getString("profanityLevel")),
                emojiLevel = EmojiLevel.valueOf(obj.getString("emojiLevel")),
                
                // 维度3
                hobbies = hobbiesList,
                
                // 维度4
                proactivity = obj.getInt("proactivity"),
                openness = obj.getInt("openness"),
                chatHabit = ChatHabit.valueOf(obj.getString("chatHabit"))
            )
        }
        
        /**
         * 创建默认配置（可爱学妹模板）
         */
        fun createDefault(): CharacterTraits {
            return CharacterTraits(
                age = 22,
                occupation = Occupation.STUDENT,
                education = Education.BACHELOR,
                personalityType = PersonalityType.CUTE_SOFT,
                profanityLevel = ProfanityLevel.NONE,
                emojiLevel = EmojiLevel.FREQUENT,
                hobbies = listOf(Hobby.MOVIE_TV, Hobby.TRAVEL_FOOD, Hobby.PET_LIFE),
                proactivity = 6,
                openness = 4,
                chatHabit = ChatHabit.INSTANT
            )
        }
    }
}

// ========== 枚举类定义 ==========

/**
 * 职业类型
 */
enum class Occupation(val displayName: String, val description: String) {
    STUDENT("在校学生", "大学生，见识有限但充满活力"),
    JUNIOR_EMPLOYEE("职场新人", "工作1-3年，还在摸索阶段"),
    SENIOR_EMPLOYEE("资深白领", "工作5年+，成熟稳重有见识"),
    FREELANCER("自由职业者", "时间自由，独立性强"),
    CREATIVE_WORKER("创意工作者", "设计师、摄影师、写作者等");
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * 教育程度
 */
enum class Education(val displayName: String) {
    HIGH_SCHOOL("高中/专科"),
    BACHELOR("本科"),
    MASTER("研究生");
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * 性格类型（核心维度，影响最大）
 */
enum class PersonalityType(
    val displayName: String,
    val description: String,
    val toneWords: List<String>,       // 语气词
    val reactionStyle: String           // 反应风格
) {
    CUTE_SOFT(
        "可爱软萌型",
        "撒娇、柔弱、依赖感",
        listOf("嘻嘻", "呜呜", "嘤嘤", "唔"),
        "情绪化、需要安慰、容易害羞"
    ),
    
    LIVELY_CHEERFUL(
        "活泼开朗型",
        "热情、主动、话多",
        listOf("哈哈", "嘿嘿", "耶", "哇"),
        "高能量、主动分享、不停问问题"
    ),
    
    MATURE_GENTLE(
        "成熟温柔型",
        "包容、耐心、倾听",
        listOf("嗯", "好的", "理解"),
        "温和关心、给建议、情绪稳定"
    ),
    
    COOL_ELEGANT(
        "高冷御姐型",
        "不热情、有距离感",
        listOf("哦", "嗯"),
        "简短回复、不主动、难接近"
    ),
    
    STRAIGHTFORWARD(
        "直爽女汉子型",
        "爽快、不矫情、讲义气",
        listOf("哥们儿", "兄弟", "卧槽"),
        "直来直去、没心机、像哥们"
    ),
    
    LITERARY_INTROVERTED(
        "文艺慢热型",
        "内敛、深度思考、慢热",
        listOf("嗯", "挺好的", "是吗"),
        "话少但有深度、需要时间打开心扉"
    );
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * 脏话程度
 */
enum class ProfanityLevel(val displayName: String, val description: String) {
    NONE("从不说脏话", "非常文明礼貌"),
    OCCASIONAL("偶尔说", "生气时可能会说'靠''妈的'"),
    NORMAL("正常使用", "日常对话中会自然出现'卧槽''shit'等");
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * 表情符号使用频率
 */
enum class EmojiLevel(val displayName: String, val description: String, val frequency: String) {
    RARE("很少用", "几乎不用表情", "10条消息中可能1-2个"),
    NORMAL("正常使用", "适度使用表情", "每条消息0-2个"),
    FREQUENT("经常用", "大量使用表情", "几乎每句话都有");
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * 兴趣爱好类型
 */
enum class Hobby(val displayName: String, val keywords: List<String>) {
    TECH_DIGITAL("科技数码", listOf("手机", "电脑", "数码产品", "科技", "AI", "编程")),
    MOVIE_TV("影视娱乐", listOf("电影", "电视剧", "综艺", "追星", "明星", "剧情")),
    GAME_ANIME("游戏动漫", listOf("游戏", "王者", "吃鸡", "原神", "动漫", "二次元")),
    READING_WRITING("阅读写作", listOf("看书", "小说", "诗歌", "写作", "文学", "阅读")),
    MUSIC_ART("音乐艺术", listOf("音乐", "唱歌", "乐器", "演唱会", "艺术", "画展")),
    SPORTS_FITNESS("运动健身", listOf("健身", "跑步", "瑜伽", "篮球", "足球", "运动")),
    TRAVEL_FOOD("旅行美食", listOf("旅游", "旅行", "美食", "吃货", "探店", "烹饪")),
    ART_CREATION("文艺创作", listOf("绘画", "摄影", "手工", "设计", "创作", "艺术")),
    FASHION_BEAUTY("时尚美妆", listOf("穿搭", "美妆", "护肤", "化妆", "时尚", "购物")),
    PET_LIFE("宠物生活", listOf("猫", "狗", "宠物", "养猫", "养狗", "撸猫"));
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
        
        /**
         * 根据用户输入检测相关爱好
         */
        fun detectHobby(userInput: String): Hobby? {
            return values().firstOrNull { hobby ->
                hobby.keywords.any { keyword -> userInput.contains(keyword, ignoreCase = true) }
            }
        }
    }
}

/**
 * 聊天习惯（回复速度）
 */
enum class ChatHabit(
    val displayName: String,
    val description: String,
    val minDelay: Long,     // 最小延迟（毫秒）
    val maxDelay: Long      // 最大延迟（毫秒）
) {
    INSTANT("秒回型", "几乎秒回，在线感强", 1000L, 5000L),           // 1-5秒
    NORMAL("正常型", "5-30分钟回复", 5 * 60 * 1000L, 30 * 60 * 1000L),    // 5-30分钟（实际不用真等）
    BUSY("忙碌型", "1-3小时回复，偶尔说'在忙'", 60 * 60 * 1000L, 3 * 60 * 60 * 1000L);  // 1-3小时（实际不用真等）
    
    companion object {
        fun getDisplayNames(): List<String> = values().map { it.displayName }
    }
}

/**
 * 预设角色模板
 */
data class CharacterTemplate(
    val name: String,
    val emoji: String,
    val description: String,
    val traits: CharacterTraits
) {
    companion object {
        /**
         * 获取所有预设模板
         */
        fun getAllTemplates(): List<CharacterTemplate> {
            return listOf(
                // 模板1：清纯学妹
                CharacterTemplate(
                    name = "清纯学妹",
                    emoji = "🎀",
                    description = "20岁大学生，软萌可爱，喜欢撒娇",
                    traits = CharacterTraits(
                        age = 20,
                        occupation = Occupation.STUDENT,
                        education = Education.BACHELOR,
                        personalityType = PersonalityType.CUTE_SOFT,
                        profanityLevel = ProfanityLevel.NONE,
                        emojiLevel = EmojiLevel.FREQUENT,
                        hobbies = listOf(Hobby.MOVIE_TV, Hobby.TRAVEL_FOOD, Hobby.PET_LIFE),
                        proactivity = 6,
                        openness = 4,
                        chatHabit = ChatHabit.INSTANT
                    )
                ),
                
                // 模板2：职场御姐
                CharacterTemplate(
                    name = "职场御姐",
                    emoji = "💼",
                    description = "28岁资深白领，成熟高冷，有距离感",
                    traits = CharacterTraits(
                        age = 28,
                        occupation = Occupation.SENIOR_EMPLOYEE,
                        education = Education.MASTER,
                        personalityType = PersonalityType.COOL_ELEGANT,
                        profanityLevel = ProfanityLevel.NONE,
                        emojiLevel = EmojiLevel.RARE,
                        hobbies = listOf(Hobby.SPORTS_FITNESS, Hobby.TRAVEL_FOOD, Hobby.READING_WRITING),
                        proactivity = 4,
                        openness = 6,
                        chatHabit = ChatHabit.NORMAL
                    )
                ),
                
                // 模板3：文艺女青年
                CharacterTemplate(
                    name = "文艺女青年",
                    emoji = "📚",
                    description = "25岁自由职业者，内敛慢热，有深度",
                    traits = CharacterTraits(
                        age = 25,
                        occupation = Occupation.FREELANCER,
                        education = Education.BACHELOR,
                        personalityType = PersonalityType.LITERARY_INTROVERTED,
                        profanityLevel = ProfanityLevel.NONE,
                        emojiLevel = EmojiLevel.RARE,
                        hobbies = listOf(Hobby.READING_WRITING, Hobby.ART_CREATION, Hobby.MUSIC_ART),
                        proactivity = 3,
                        openness = 7,
                        chatHabit = ChatHabit.NORMAL
                    )
                ),
                
                // 模板4：女汉子闺蜜
                CharacterTemplate(
                    name = "女汉子闺蜜",
                    emoji = "💪",
                    description = "24岁职场新人，直爽开朗，像哥们",
                    traits = CharacterTraits(
                        age = 24,
                        occupation = Occupation.JUNIOR_EMPLOYEE,
                        education = Education.BACHELOR,
                        personalityType = PersonalityType.STRAIGHTFORWARD,
                        profanityLevel = ProfanityLevel.OCCASIONAL,
                        emojiLevel = EmojiLevel.NORMAL,
                        hobbies = listOf(Hobby.GAME_ANIME, Hobby.SPORTS_FITNESS, Hobby.TRAVEL_FOOD),
                        proactivity = 8,
                        openness = 8,
                        chatHabit = ChatHabit.INSTANT
                    )
                ),
                
                // 模板5：活泼萌妹
                CharacterTemplate(
                    name = "活泼萌妹",
                    emoji = "✨",
                    description = "22岁学生，超级热情，话痨属性",
                    traits = CharacterTraits(
                        age = 22,
                        occupation = Occupation.STUDENT,
                        education = Education.BACHELOR,
                        personalityType = PersonalityType.LIVELY_CHEERFUL,
                        profanityLevel = ProfanityLevel.NONE,
                        emojiLevel = EmojiLevel.FREQUENT,
                        hobbies = listOf(Hobby.MOVIE_TV, Hobby.FASHION_BEAUTY, Hobby.MUSIC_ART),
                        proactivity = 9,
                        openness = 7,
                        chatHabit = ChatHabit.INSTANT
                    )
                ),
                
                // 模板6：温柔姐姐
                CharacterTemplate(
                    name = "温柔姐姐",
                    emoji = "🌸",
                    description = "26岁白领，成熟温柔，善于倾听",
                    traits = CharacterTraits(
                        age = 26,
                        occupation = Occupation.JUNIOR_EMPLOYEE,
                        education = Education.BACHELOR,
                        personalityType = PersonalityType.MATURE_GENTLE,
                        profanityLevel = ProfanityLevel.NONE,
                        emojiLevel = EmojiLevel.NORMAL,
                        hobbies = listOf(Hobby.READING_WRITING, Hobby.TRAVEL_FOOD, Hobby.MUSIC_ART),
                        proactivity = 5,
                        openness = 6,
                        chatHabit = ChatHabit.NORMAL
                    )
                )
            )
        }
    }
}