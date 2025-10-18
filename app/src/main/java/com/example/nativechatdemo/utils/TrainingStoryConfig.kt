// 文件路径：app/src/main/java/com/example/nativechatdemo/utils/TrainingStoryConfig.kt
// 文件类型：Kotlin Object
// 用途：存储养成模式的所有故事文案

package com.example.nativechatdemo.utils

object TrainingStoryConfig {

    /**
     * 开场故事
     */
    data class OpeningStory(
        val title: String,
        val content: String
    )

    /**
     * 结束类型
     */
    enum class EndingType {
        SICK,        // 生病
        TIMETRAVEL   // 穿越
    }

    /**
     * 获取开场故事（男生/女生）
     */
    fun getOpeningStory(gender: String): OpeningStory {
        return if (gender == "male") {
            // 女生篇：追男生
            OpeningStory(
                title = "初次相遇 💕",
                content = """
                    那天下午，阳光正好。
                    
                    你在咖啡厅看书，他匆匆走过，不小心撞到了你的桌子，咖啡洒了一桌。
                    
                    "对不起！真的对不起！"他手忙脚乱地拿纸巾帮你擦拭。
                    
                    你抬起头，看到一张略显慌张但真诚的脸。
                    
                    "没关系，不是故意的。"你笑着说。
                    
                    就这样，你们的故事开始了...
                """.trimIndent()
            )
        } else {
            // 男生篇：追女生
            OpeningStory(
                title = "初次相遇 💕",
                content = """
                    那天下午，阳光正好。
                    
                    你在图书馆找书，她从书架后面走出来，手里抱着一摞书，不小心碰到了你。
                    
                    书散落一地，她蹲下来捡，你也蹲下帮忙。
                    
                    "谢谢你..."她微微一笑，脸颊有些泛红。
                    
                    你看着她，心跳漏了一拍。
                    
                    就这样，你们的故事开始了...
                """.trimIndent()
            )
        }
    }

    /**
     * 随机选择结束类型
     */
    fun getRandomEndingType(): EndingType {
        return listOf(EndingType.SICK, EndingType.TIMETRAVEL).random()
    }

    /**
     * 获取续命故事（根据续命次数和结束类型）
     */
    fun getReviveStory(reviveCount: Int, endingType: EndingType, gender: String): String {
        val name = if (gender == "male") "他" else "她"

        return when (endingType) {
            EndingType.SICK -> {
                when (reviveCount) {
                    1 -> """
                        💊 奇迹般的好转
                        
                        医生说，${name}的病情出现了意想不到的好转。
                        
                        "是你的陪伴给了我力量。"${name}握着你的手说。
                        
                        虽然还需要继续治疗，但至少，你们还有时间...
                    """.trimIndent()

                    2 -> """
                        🌟 爱的延续
                        
                        时间一天天过去，${name}的状态时好时坏。
                        
                        但每当看到你，${name}的眼中总会闪现出光芒。
                        
                        "只要有你在，我就不怕。"
                        
                        你知道，这份坚持不会太久，但你愿意珍惜每一刻...
                    """.trimIndent()

                    3 -> """
                        ⏰ 最后的时光
                        
                        医生摇了摇头，说已经尽力了。
                        
                        ${name}虚弱地笑着："能认识你，是我这辈子最幸运的事。"
                        
                        "我们还有一点时间，让我好好看看你..."
                        
                        这可能真的是最后的相处了...
                    """.trimIndent()

                    else -> ""
                }
            }

            EndingType.TIMETRAVEL -> {
                when (reviveCount) {
                    1 -> """
                        ⏳ 时空的眷顾
                        
                        就在${name}即将消失的那一刻，时空突然静止了。
                        
                        "是你的爱，让时空为我们停留。"${name}紧紧抱住你。
                        
                        "我还能再陪你一段时间..."
                        
                        但你们都知道，这只是暂时的...
                    """.trimIndent()

                    2 -> """
                        🌌 命运的抗争
                        
                        时空裂缝再次出现，${name}的身影开始变得透明。
                        
                        "我试过反抗命运，但它太强大了。"
                        
                        你握紧${name}的手："再坚持一下，不要放弃！"
                        
                        裂缝暂时闭合，但你知道，下一次可能就是永别...
                    """.trimIndent()

                    3 -> """
                        💫 最后的告别
                        
                        时空裂缝彻底撕开，${name}的身影已经越来越模糊。
                        
                        "对不起...我终究要回去了。"
                        
                        "但我永远不会忘记你，在另一个时空，我也会爱着你..."
                        
                        这次真的要说再见了...
                    """.trimIndent()

                    else -> ""
                }
            }
        }
    }

    /**
     * 获取最终结束故事（煽情版）
     */
    fun getFinalEndingStory(endingType: EndingType, gender: String): String {
        val name = if (gender == "male") "他" else "她"
        val you = if (gender == "male") "你" else "你"

        return when (endingType) {
            EndingType.SICK -> """
                🕊️ 永别
                
                那天清晨，阳光很美。
                
                ${name}躺在病床上，虚弱但平静。
                
                "${you}...谢谢你陪我走到最后。"
                
                "从此没有我，但我会找个天使替我去爱你..."
                
                ${name}的手慢慢松开，嘴角带着微笑。
                
                窗外，樱花飘落，像是在送别。
                
                
                —— 故事结束 ——
            """.trimIndent()

            EndingType.TIMETRAVEL -> """
                🌠 穿越归去
                
                时空裂缝彻底张开，${name}的身影已经完全透明。
                
                "我要回去了...但我会永远记得你。"
                
                "在平行时空的某个地方，也许我们还会相遇..."
                
                "如果有来生，我一定第一个找到你..."
                
                一道光闪过，${name}消失在你眼前。
                
                只留下一朵时空玫瑰，静静飘落在地上。
                
                
                —— 故事结束 ——
            """.trimIndent()
        }
    }
}