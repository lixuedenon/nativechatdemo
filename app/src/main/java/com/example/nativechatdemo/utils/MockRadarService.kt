// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/MockRadarService.kt
// 文件类型：Kotlin Object (单例工具类)

package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.RadarScenario
import org.json.JSONArray
import org.json.JSONObject

object MockRadarService {

    /**
     * 生成所有预设场景
     */
    fun generateAllScenarios(): List<RadarScenario> {
        val scenarios = mutableListOf<RadarScenario>()

        // 学习模式场景
        scenarios.addAll(generateLearnScenarios("female")) // 男生篇：与女生交往
        scenarios.addAll(generateLearnScenarios("male"))   // 女生篇：与男生交往

        // 练习模式场景
        scenarios.addAll(generatePracticeScenarios("female"))
        scenarios.addAll(generatePracticeScenarios("male"))

        return scenarios
    }

    /**
     * 生成学习模式场景
     */
    private fun generateLearnScenarios(targetGender: String): List<RadarScenario> {
        val scenarios = mutableListOf<RadarScenario>()

        if (targetGender == "female") {
            // 男生篇 - 学习模式（与女生交往）
            scenarios.add(
                RadarScenario(
                    id = "learn_female_dating_001",
                    type = "learn",
                    category = "dating",
                    difficulty = 1,
                    targetGender = "female",
                    contextDescription = "第一次约会后，微信聊天",
                    partnerMessage = "今天挺开心的",
                    correctResponse = "",
                    wrongResponses = "",
                    radarPoints = generateRadarPointsJson(
                        listOf(
                            RadarPoint("嗯嗯", "过于敷衍，让对方觉得你不重视", "我也是！今天和你聊天特别轻松"),
                            RadarPoint("就打打游戏", "缺乏生活情趣，没有展开话题", "我喜欢运动和看书，偶尔也玩玩游戏放松。你呢？")
                        )
                    ),
                    analysis = "这段对话中有两个明显的雷区。第一个是回复'嗯嗯'过于敷衍，会让对方觉得你不重视这次对话。第二个是说'就打打游戏'显得缺乏生活情趣，而且没有展开话题的意图。"
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "learn_female_dating_002",
                    type = "learn",
                    category = "dating",
                    difficulty = 2,
                    targetGender = "female",
                    contextDescription = "认识一周后，她主动提起自己的兴趣",
                    partnerMessage = "我最近在学钢琴",
                    correctResponse = "",
                    wrongResponses = "",
                    radarPoints = generateRadarPointsJson(
                        listOf(
                            RadarPoint("哦", "冷淡回应，错失展现兴趣的机会", "哇，钢琴！我一直觉得会弹琴的女生特别优雅"),
                            RadarPoint("我不太懂音乐", "自我否定，没有尝试了解对方", "真厉害！学了多久了？能弹什么曲子给我听听吗？")
                        )
                    ),
                    analysis = "对方分享自己的兴趣爱好时，是建立共同话题的绝佳机会。冷淡回应或自我否定都会让对话陷入僵局。正确做法是展现真诚的兴趣和好奇心。"
                )
            )

        } else {
            // 女生篇 - 学习模式（与男生交往）
            scenarios.add(
                RadarScenario(
                    id = "learn_male_dating_001",
                    type = "learn",
                    category = "dating",
                    difficulty = 1,
                    targetGender = "male",
                    contextDescription = "刚加微信，他主动找你聊天",
                    partnerMessage = "你平时喜欢做什么呀？",
                    correctResponse = "",
                    wrongResponses = "",
                    radarPoints = generateRadarPointsJson(
                        listOf(
                            RadarPoint("没什么", "过于简短，让对话无法继续", "我喜欢看电影和逛街，你呢？"),
                            RadarPoint("随便聊聊", "消极态度，显得不重视", "我平时喜欢健身和烘焙，最近在学做蛋糕~")
                        )
                    ),
                    analysis = "对方主动询问你的兴趣，是想了解你、展开话题。过于简短或消极的回复会让对方感觉你不想聊，很容易让对话结束。"
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "learn_male_work_001",
                    type = "learn",
                    category = "work",
                    difficulty = 2,
                    targetGender = "male",
                    contextDescription = "同事关系，他今天看起来有点累",
                    partnerMessage = "工作好累啊",
                    correctResponse = "",
                    wrongResponses = "",
                    radarPoints = generateRadarPointsJson(
                        listOf(
                            RadarPoint("那就别干了", "过于激进，不切实际", "辛苦了，要不要喝杯咖啡休息一下？"),
                            RadarPoint("我也累", "只顾自己，没有给予关怀", "最近项目多吗？压力大的时候记得劳逸结合~")
                        )
                    ),
                    analysis = "对方表达疲惫时，是在寻求理解和关心。过于激进的建议或只关注自己都不合适，应该给予共情和实际的关怀。"
                )
            )
        }

        return scenarios
    }

    /**
     * 生成练习模式场景
     */
    private fun generatePracticeScenarios(targetGender: String): List<RadarScenario> {
        val scenarios = mutableListOf<RadarScenario>()

        if (targetGender == "female") {
            // 男生篇 - 练习模式
            scenarios.add(
                RadarScenario(
                    id = "practice_female_dating_001",
                    type = "practice",
                    category = "dating",
                    difficulty = 1,
                    targetGender = "female",
                    contextDescription = "约会时，她问了一个敏感问题",
                    partnerMessage = "你觉得我胖吗？",
                    correctResponse = "你很匀称啊，别告诉我你要减肥",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "不胖啊",
                            "不但不胖，而且很优雅",
                            "什么胖不胖的，健康最重要"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "她希望听到你对她外形的肯定，同时也在试探你的态度",
                        options = listOf(
                            OptionAnalysis(1, "不胖啊", "虽然回答了问题，但显得敷衍，缺乏真诚感", 10),
                            OptionAnalysis(2, "不但不胖，而且很优雅", "加入了具体赞美，但略显刻意", 20),
                            OptionAnalysis(3, "什么胖不胖的，健康最重要", "转移话题，回避了她的问题，可能让她不满", 5),
                            OptionAnalysis(4, "你很匀称啊，别告诉我你要减肥", "既肯定了她的身材，又表达了关心，还带点幽默", 30)
                        )
                    )
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "practice_female_dating_002",
                    type = "practice",
                    category = "dating",
                    difficulty = 2,
                    targetGender = "female",
                    contextDescription = "她在生理期，心情不太好",
                    partnerMessage = "肚子好疼...",
                    correctResponse = "多喝热水，我给你买个暖宝宝送过去",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "多喝热水",
                            "是不是吃坏东西了？",
                            "忍一忍就好了"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "她在寻求安慰和关心，希望你能理解并给予实际帮助",
                        options = listOf(
                            OptionAnalysis(1, "多喝热水", "虽然关心，但太过老套，缺乏实际行动", 5),
                            OptionAnalysis(2, "是不是吃坏东西了？", "理解错误，可能让她更不舒服", 0),
                            OptionAnalysis(3, "忍一忍就好了", "冷漠回应，完全没有关心", -10),
                            OptionAnalysis(4, "多喝热水，我给你买个暖宝宝送过去", "既有关心，又有实际行动，最佳回答", 30)
                        )
                    )
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "practice_female_friend_001",
                    type = "practice",
                    category = "friend",
                    difficulty = 1,
                    targetGender = "female",
                    contextDescription = "她失恋了，找你倾诉",
                    partnerMessage = "我们分手了...",
                    correctResponse = "怎么了？慢慢说，我听着",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "那个人不适合你",
                            "分就分了，没什么大不了的",
                            "是不是你的问题？"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "她此刻很伤心，需要有人倾听和陪伴，而不是评判",
                        options = listOf(
                            OptionAnalysis(1, "那个人不适合你", "过早评判对方，可能引起反感", 5),
                            OptionAnalysis(2, "分就分了，没什么大不了的", "轻视她的感受，显得冷漠", 0),
                            OptionAnalysis(3, "是不是你的问题？", "质疑她，让她更难过", -10),
                            OptionAnalysis(4, "怎么了？慢慢说，我听着", "给予倾听和陪伴，最合适的回应", 25)
                        )
                    )
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "practice_female_work_001",
                    type = "practice",
                    category = "work",
                    difficulty = 3,
                    targetGender = "female",
                    contextDescription = "女同事工作出错，被领导批评了",
                    partnerMessage = "今天被骂了，心情超差",
                    correctResponse = "辛苦了，要不要一起去吃个饭，聊聊？",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "是不是你没做好？",
                            "领导脾气就这样，别在意",
                            "下次注意点就好了"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "她需要情绪上的支持和理解，而不是分析原因",
                        options = listOf(
                            OptionAnalysis(1, "是不是你没做好？", "质疑她，让她更委屈", -5),
                            OptionAnalysis(2, "领导脾气就这样，别在意", "轻描淡写，没有真正关心", 5),
                            OptionAnalysis(3, "下次注意点就好了", "说教式回应，缺乏共情", 10),
                            OptionAnalysis(4, "辛苦了，要不要一起去吃个饭，聊聊？", "给予安慰和陪伴，并提供实际帮助", 25)
                        )
                    )
                )
            )

        } else {
            // 女生篇 - 练习模式
            scenarios.add(
                RadarScenario(
                    id = "practice_male_dating_001",
                    type = "practice",
                    category = "dating",
                    difficulty = 1,
                    targetGender = "male",
                    contextDescription = "他约你周末出去玩",
                    partnerMessage = "周末有空吗？一起出去玩？",
                    correctResponse = "好啊！你想去哪儿？",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "看情况吧",
                            "不确定，到时候再说",
                            "有点累，不太想出门"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "他在试探你的态度，希望得到积极回应",
                        options = listOf(
                            OptionAnalysis(1, "看情况吧", "模棱两可，让对方觉得你不够重视", 5),
                            OptionAnalysis(2, "不确定，到时候再说", "敷衍的态度，可能让他失望", 0),
                            OptionAnalysis(3, "有点累，不太想出门", "直接拒绝，还显得消极", -10),
                            OptionAnalysis(4, "好啊！你想去哪儿？", "积极回应，并展现兴趣，最佳答案", 25)
                        )
                    )
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "practice_male_dating_002",
                    type = "practice",
                    category = "dating",
                    difficulty = 2,
                    targetGender = "male",
                    contextDescription = "他给你发了自拍",
                    partnerMessage = "刚健身完，累死了[图片]",
                    correctResponse = "哇，身材保持得真好！",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "哦",
                            "健身有什么用",
                            "看起来好累"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "他在展示自己的生活状态，希望得到你的肯定和关注",
                        options = listOf(
                            OptionAnalysis(1, "哦", "冷淡回应，让他觉得你不感兴趣", 0),
                            OptionAnalysis(2, "健身有什么用", "否定他的努力，很伤人", -15),
                            OptionAnalysis(3, "看起来好累", "只关注负面，没有鼓励", 5),
                            OptionAnalysis(4, "哇，身材保持得真好！", "肯定他的付出，给予赞美", 30)
                        )
                    )
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "practice_male_friend_001",
                    type = "practice",
                    category = "friend",
                    difficulty = 2,
                    targetGender = "male",
                    contextDescription = "他向你抱怨工作压力大",
                    partnerMessage = "最近压力好大，不知道该怎么办",
                    correctResponse = "压力大的时候要学会放松，要不要一起去散散心？",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "那就换个工作",
                            "谁不累啊",
                            "自己调整一下心态吧"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "他在向你寻求理解和支持，希望得到情绪上的安慰",
                        options = listOf(
                            OptionAnalysis(1, "那就换个工作", "建议太轻率，不切实际", 5),
                            OptionAnalysis(2, "谁不累啊", "冷漠回应，没有共情", -5),
                            OptionAnalysis(3, "自己调整一下心态吧", "说教式，缺乏实际帮助", 10),
                            OptionAnalysis(4, "压力大的时候要学会放松，要不要一起去散散心？", "给予理解和陪伴，提供实际帮助", 25)
                        )
                    )
                )
            )

            scenarios.add(
                RadarScenario(
                    id = "practice_male_work_001",
                    type = "practice",
                    category = "work",
                    difficulty = 3,
                    targetGender = "male",
                    contextDescription = "男同事项目成功，升职了",
                    partnerMessage = "终于升职了，请你吃饭吧！",
                    correctResponse = "恭喜！你一直很努力，实至名归",
                    wrongResponses = generateOptionsJson(
                        listOf(
                            "运气真好",
                            "有什么了不起的",
                            "还行吧"
                        )
                    ),
                    radarPoints = "",
                    analysis = generatePracticeAnalysisJson(
                        intent = "他想分享喜悦，希望得到你的祝贺和肯定",
                        options = listOf(
                            OptionAnalysis(1, "运气真好", "否定他的努力，只归功于运气", 5),
                            OptionAnalysis(2, "有什么了不起的", "嫉妒心态，非常失礼", -15),
                            OptionAnalysis(3, "还行吧", "冷淡回应，让他失望", 0),
                            OptionAnalysis(4, "恭喜！你一直很努力，实至名归", "真诚祝贺，肯定他的付出", 30)
                        )
                    )
                )
            )
        }

        return scenarios
    }

    /**
     * 生成雷区点JSON
     */
    private fun generateRadarPointsJson(points: List<RadarPoint>): String {
        val jsonArray = JSONArray()
        points.forEach { point ->
            val json = JSONObject()
            json.put("text", point.text)
            json.put("reason", point.reason)
            json.put("betterWay", point.betterWay)
            jsonArray.put(json)
        }
        return jsonArray.toString()
    }

    /**
     * 生成选项JSON
     */
    private fun generateOptionsJson(options: List<String>): String {
        val jsonArray = JSONArray()
        options.forEach { option ->
            jsonArray.put(option)
        }
        return jsonArray.toString()
    }

    /**
     * 生成练习模式分析JSON
     */
    private fun generatePracticeAnalysisJson(
        intent: String,
        options: List<OptionAnalysis>
    ): String {
        val json = JSONObject()
        json.put("intent", intent)

        val optionsArray = JSONArray()
        options.forEach { opt ->
            val optJson = JSONObject()
            optJson.put("index", opt.index)
            optJson.put("text", opt.text)
            optJson.put("analysis", opt.analysis)
            optJson.put("score", opt.score)
            optionsArray.put(optJson)
        }
        json.put("options", optionsArray)

        return json.toString()
    }

    /**
     * 雷区点数据类
     */
    data class RadarPoint(
        val text: String,
        val reason: String,
        val betterWay: String
    )

    /**
     * 选项分析数据类
     */
    data class OptionAnalysis(
        val index: Int,
        val text: String,
        val analysis: String,
        val score: Int
    )
}