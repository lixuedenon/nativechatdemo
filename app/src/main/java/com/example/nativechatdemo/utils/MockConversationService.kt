// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/MockConversationService.kt
// 文件类型：Kotlin Object

package com.example.nativechatdemo.utils

import com.example.nativechatdemo.data.model.*
import org.json.JSONArray
import org.json.JSONObject

object MockConversationService {

    /**
     * 生成所有对话场景
     */
    fun generateAllScenarios(): List<ConversationScenario> {
        val scenarios = mutableListOf<ConversationScenario>()

        // 女生篇（分析男生）
        scenarios.add(generateMaleScenario1())

        // 男生篇（分析女生）
        scenarios.add(generateFemaleScenario1())

        return scenarios
    }

    /**
     * 女生篇 - 场景1：初次见面聊天（分析男生）
     */
    private fun generateMaleScenario1(): ConversationScenario {
        val dialogue = listOf(
            DialogueTurn(1, "你好啊，终于见到你了", "你好！很高兴见到你", false),
            DialogueTurn(2, "今天天气不错呢", "是啊，特别适合出来走走", false),
            DialogueTurn(3, "你平时喜欢做什么？", "我喜欢看电影和旅游，你呢？", true),
            DialogueTurn(4, "我也喜欢看电影！最近有什么推荐的吗？", "最近有部科幻片挺不错的，叫《流浪地球2》", false),
            DialogueTurn(5, "哇，我也想看！改天一起去？", "好啊，周末有空吗？", false),
            DialogueTurn(6, "周末可以！对了，你做什么工作的？", "我在互联网公司做产品经理", false),
            DialogueTurn(7, "听起来很厉害！工作压力大吗？", "还好，虽然忙但挺有成就感的。你呢？", true),
            DialogueTurn(8, "我是设计师，最近在做一个新项目", "设计师啊，难怪审美这么好", false),
            DialogueTurn(9, "哈哈，谢谢夸奖～你平时下班都做什么？", "一般会健身或者看书，偶尔约朋友打球", false),
            DialogueTurn(10, "听起来生活挺充实的", "是啊，工作和生活都要平衡嘛", false),
            DialogueTurn(11, "那你周末一般在家还是出去玩？", "看心情，天气好就出去走走，下雨就宅家", false),
            DialogueTurn(12, "我们好像挺合拍的，改天一起出去玩吧", "好啊，那就周末约电影？", true)
        )

        val keyPoints = listOf(
            KeyPoint(
                atTurnIndex = 3,
                warning = "⚠️ 注意！对方在询问你的兴趣爱好\n这是建立共同话题的关键时刻",
                correctResponse = "我喜欢看电影和旅游，你呢？",
                wrongOptions = listOf("没什么", "随便吧", "不知道"),
                analysis = "主动分享2-3个具体兴趣，并反问对方，既展示自己又给对方延续话题的机会。避免过于简短或消极的回答。",
                optionAnalysis = listOf(
                    OptionAnalysis("没什么", "过于简短，让对话无法继续，显得你不重视这次交流", 0),
                    OptionAnalysis("随便吧", "态度消极，会让对方觉得你没兴趣聊天", -5),
                    OptionAnalysis("不知道", "逃避问题，错失建立共同话题的机会", -5),
                    OptionAnalysis("我喜欢看电影和旅游，你呢？", "完美回答！既分享了具体兴趣，又反问对方，延续话题", 30)
                )
            ),
            KeyPoint(
                atTurnIndex = 7,
                warning = "⚠️ 对方在关心你的工作状态\n这是展示你积极生活态度的机会",
                correctResponse = "还好，虽然忙但挺有成就感的。你呢？",
                wrongOptions = listOf("特别累，每天加班", "还行吧", "不想聊工作"),
                analysis = "回答工作问题时要保持积极态度，同时反问对方，避免抱怨或过于简短。",
                optionAnalysis = listOf(
                    OptionAnalysis("特别累，每天加班", "满满的负能量，会让气氛变沉重", 5),
                    OptionAnalysis("还行吧", "敷衍回答，没有展开话题", 10),
                    OptionAnalysis("不想聊工作", "直接拒绝话题，显得不礼貌", -10),
                    OptionAnalysis("还好，虽然忙但挺有成就感的。你呢？", "积极乐观，还关心对方，展现良好心态", 30)
                )
            ),
            KeyPoint(
                atTurnIndex = 12,
                warning = "⚠️ 关键邀约来了！\n你的回应会直接影响关系发展",
                correctResponse = "好啊，那就周末约电影？",
                wrongOptions = listOf("再说吧", "看情况", "我考虑一下"),
                analysis = "对方释放积极信号时，要给予明确且热情的回应，推进关系发展。",
                optionAnalysis = listOf(
                    OptionAnalysis("再说吧", "模棱两可，让对方不确定你的态度", 5),
                    OptionAnalysis("看情况", "敷衍回答，可能让对方失望", 0),
                    OptionAnalysis("我考虑一下", "犹豫不决，错失良机", 5),
                    OptionAnalysis("好啊，那就周末约电影？", "积极回应并提出具体计划，完美推进关系", 30)
                )
            )
        )

        return ConversationScenario(
            id = "conv_male_001",
            title = "初次见面聊天",
            targetGender = "male",
            category = "dating",
            difficulty = 1,
            dialogueJson = dialogueToJson(dialogue),
            keyPointsJson = keyPointsToJson(keyPoints)
        )
    }

    /**
     * 男生篇 - 场景1：咖啡厅约会（分析女生）
     */
    private fun generateFemaleScenario1(): ConversationScenario {
        val dialogue = listOf(
            DialogueTurn(1, "这家咖啡厅环境真不错", "是啊，我也很喜欢这里", false),
            DialogueTurn(2, "你经常来这里吗？", "偶尔会来，喜欢这里的安静氛围", false),
            DialogueTurn(3, "你今天的衣服很好看", "谢谢～这是新买的，还担心不太合适呢", true),
            DialogueTurn(4, "很适合你，显得很有气质", "你这么说我就放心啦", false),
            DialogueTurn(5, "对了，你平时周末都做什么？", "一般会去逛街或者约朋友喝茶，你呢？", false),
            DialogueTurn(6, "我喜欢运动，打篮球、跑步什么的", "听起来很健康！我也想养成运动的习惯", false),
            DialogueTurn(7, "那改天一起去跑步吧，我可以带你", "好啊，不过我体力可能跟不上你", true),
            DialogueTurn(8, "没关系，慢慢来就好，重在坚持", "有你这个'教练'我应该能坚持下去", false),
            DialogueTurn(9, "哈哈，那说定了。你喜欢什么运动？", "我比较喜欢瑜伽和游泳", false),
            DialogueTurn(10, "游泳不错，夏天一起去游泳怎么样？", "好啊，到时候你可要教我哦", false),
            DialogueTurn(11, "没问题！你住哪边啊？离这里远吗？", "不远，打车十几分钟就到了", false),
            DialogueTurn(12, "那挺方便的，以后约会就来这附近", "嗯嗯，期待下次见面", true)
        )

        val keyPoints = listOf(
            KeyPoint(
                atTurnIndex = 3,
                warning = "⚠️ 对方夸奖你的外表\n这是展示自信和给予回应的好机会",
                correctResponse = "谢谢～这是新买的，还担心不太合适呢",
                wrongOptions = listOf("哪有，不好看", "还行吧", "你别开玩笑了"),
                analysis = "收到夸奖时大方接受，适当解释，还可以顺势延续话题。过度谦虚或否定会让气氛尴尬。",
                optionAnalysis = listOf(
                    OptionAnalysis("哪有，不好看", "过度谦虚，否定对方的眼光", 5),
                    OptionAnalysis("还行吧", "冷淡回应，让对方尴尬", 0),
                    OptionAnalysis("你别开玩笑了", "不接受夸奖，打击对方积极性", -5),
                    OptionAnalysis("谢谢～这是新买的，还担心不太合适呢", "大方接受+适度谦虚+延续话题", 30)
                )
            ),
            KeyPoint(
                atTurnIndex = 7,
                warning = "⚠️ 对方邀请你一起运动\n回应态度会影响你们的互动机会",
                correctResponse = "好啊，不过我体力可能跟不上你",
                wrongOptions = listOf("我不太喜欢运动", "再说吧", "我怕太累"),
                analysis = "接受邀约时表现出兴趣，适当表达小担忧显得真实可爱，但不要直接拒绝。",
                optionAnalysis = listOf(
                    OptionAnalysis("我不太喜欢运动", "直接拒绝，错失相处机会", -10),
                    OptionAnalysis("再说吧", "模糊回答，让对方不确定", 5),
                    OptionAnalysis("我怕太累", "消极态度，缺乏积极性", 0),
                    OptionAnalysis("好啊，不过我体力可能跟不上你", "接受邀约+表达小担忧，真实又可爱", 30)
                )
            ),
            KeyPoint(
                atTurnIndex = 12,
                warning = "⚠️ 对方暗示希望继续见面\n你的回应决定关系走向",
                correctResponse = "嗯嗯，期待下次见面",
                wrongOptions = listOf("再看吧", "到时候再说", "以后有机会再约"),
                analysis = "对方释放积极信号时，要明确表达期待，推进关系。模糊回答会让对方觉得你不感兴趣。",
                optionAnalysis = listOf(
                    OptionAnalysis("再看吧", "态度不明，让对方失望", 5),
                    OptionAnalysis("到时候再说", "敷衍回答，显得没兴趣", 0),
                    OptionAnalysis("以后有机会再约", "客套话，暗示不想继续发展", -5),
                    OptionAnalysis("嗯嗯，期待下次见面", "明确表达期待，鼓励关系发展", 30)
                )
            )
        )

        return ConversationScenario(
            id = "conv_female_001",
            title = "咖啡厅约会",
            targetGender = "female",
            category = "dating",
            difficulty = 1,
            dialogueJson = dialogueToJson(dialogue),
            keyPointsJson = keyPointsToJson(keyPoints)
        )
    }

    /**
     * 将对话列表转换为JSON字符串
     */
    private fun dialogueToJson(dialogue: List<DialogueTurn>): String {
        val jsonArray = JSONArray()
        dialogue.forEach { turn ->
            val json = JSONObject()
            json.put("index", turn.index)
            json.put("partnerSays", turn.partnerSays)
            json.put("mySays", turn.mySays)
            json.put("isKeyPoint", turn.isKeyPoint)
            jsonArray.put(json)
        }
        return jsonArray.toString()
    }

    /**
     * 将关键点列表转换为JSON字符串
     */
    private fun keyPointsToJson(keyPoints: List<KeyPoint>): String {
        val jsonArray = JSONArray()
        keyPoints.forEach { kp ->
            val json = JSONObject()
            json.put("atTurnIndex", kp.atTurnIndex)
            json.put("warning", kp.warning)
            json.put("correctResponse", kp.correctResponse)

            val wrongOptionsArray = JSONArray()
            kp.wrongOptions.forEach { wrongOptionsArray.put(it) }
            json.put("wrongOptions", wrongOptionsArray)

            json.put("analysis", kp.analysis)

            val analysisArray = JSONArray()
            kp.optionAnalysis.forEach { oa ->
                val oaJson = JSONObject()
                oaJson.put("optionText", oa.optionText)
                oaJson.put("analysis", oa.analysis)
                oaJson.put("score", oa.score)
                analysisArray.put(oaJson)
            }
            json.put("optionAnalysis", analysisArray)

            jsonArray.put(json)
        }
        return jsonArray.toString()
    }
}