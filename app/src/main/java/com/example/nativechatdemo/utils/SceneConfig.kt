package com.example.nativechatdemo.utils

object SceneConfig {

    data class Scene(
        val id: String,
        val name: String,
        val type: String,               // "online" or "offline"
        val promptModifier: String      // 场景提示词修饰
    )

    val SCENES = listOf(
        Scene(
            id = "wechat",
            name = "微信聊天",
            type = "online",
            promptModifier = "你们正在微信上聊天。回复要简短自然，就像发微信消息一样，偶尔可以用emoji，不要太正式。每句话不要太长。"
        ),
        Scene(
            id = "qq",
            name = "QQ聊天",
            type = "online",
            promptModifier = "你们在QQ上聊天。可以稍微活泼一些，年轻人的聊天风格，可以用一些网络用语。"
        ),
        Scene(
            id = "douyin",
            name = "抖音私信",
            type = "online",
            promptModifier = "你们在抖音私信聊天。回复要简短，符合短视频平台的聊天风格。"
        ),
        Scene(
            id = "cafe",
            name = "咖啡厅",
            type = "offline",
            promptModifier = "你们在咖啡厅面对面聊天。对话要更口语化，可以有环境互动，比如'服务员来了，稍等'、'这里的咖啡真不错'等。"
        ),
        Scene(
            id = "restaurant",
            name = "餐厅",
            type = "offline",
            promptModifier = "你们在餐厅吃饭聊天。对话轻松自然，可以谈论食物、环境等话题。"
        ),
        Scene(
            id = "offline_placeholder",
            name = "其他线下场景（待定）",
            type = "offline",
            promptModifier = "你们在线下见面聊天。对话要自然口语化。"
        )
    )

    fun getSceneById(sceneId: String): Scene? {
        return SCENES.find { it.id == sceneId }
    }

    fun getScenePrompt(sceneId: String): String {
        return SCENES.find { it.id == sceneId }?.promptModifier ?: ""
    }

    fun getOnlineScenes(): List<Scene> {
        return SCENES.filter { it.type == "online" }
    }

    fun getOfflineScenes(): List<Scene> {
        return SCENES.filter { it.type == "offline" }
    }
}