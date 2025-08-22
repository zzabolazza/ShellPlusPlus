package com.shellplusplus.data

object DefaultApps {
    val apps = listOf(
        AppConfig(
            packageName = "chatgpt",
            name = "ChatGPT",
            url = "https://chatgpt.com/"
        ),
        AppConfig(
            packageName = "perplexity",
            name = "Perplexity",
            url = "https://www.perplexity.ai/"
        ),
        AppConfig(
            packageName = "claude",
            name = "Claude",
            url = "https://claude.ai"
        ),
        AppConfig(
            packageName = "grok",
            name = "Grok",
            url = "https://grok.com"
        ),
        AppConfig(
            packageName = "yuewen",
            name = "跃问",
            url = "https://yuewen.cn"
        ),
        AppConfig(
            packageName = "kimi",
            name = "Kimi",
            url = "https://kimi.moonshot.cn/"
        ),
        AppConfig(
            packageName = "tiangong",
            name = "天工",
            url = "https://www.tiangong.cn/"
        ),
        AppConfig(
            packageName = "doubao",
            name = "豆包",
            url = "https://www.doubao.com/chat/"
        ),
        AppConfig(
            packageName = "mistral",
            name = "Mistral",
            url = "https://chat.mistral.ai/chat"
        ),
        AppConfig(
            packageName = "yuanbao",
            name = "元宝",
            url = "https://yuanbao.tencent.com"
        ),
        AppConfig(
            packageName = "tongyi",
            name = "通义千问",
            url = "https://tongyi.aliyun.com"
        ),
        AppConfig(
            packageName = "gemini",
            name = "Gemini",
            url = "https://gemini.google.com"
        ),
        AppConfig(
            packageName = "duckai",
            name = "DuckAI",
            url = "https://duckduckgo.com/?q=DuckDuckGo+AI+Chat&ia=chat&duckai=1"
        ),
        AppConfig(
            packageName = "chatglm",
            name = "ChatGLM",
            url = "https://chatglm.cn"
        ),
        AppConfig(
            packageName = "hailuoai",
            name = "海螺AI",
            url = "https://hailuoai.com/"
        ),
        AppConfig(
            packageName = "meta",
            name = "Meta AI",
            url = "https://www.meta.ai/"
        ),
        AppConfig(
            packageName = "poe",
            name = "Poe",
            url = "https://www.poe.com/"
        ),
        AppConfig(
            packageName = "deepseek",
            name = "Deepseek",
            url = "https://chat.deepseek.com/"
        ),
        AppConfig(
            packageName = "yiyan",
            name = "文心一言",
            url = "https://yiyan.baidu.com/"
        ),
        AppConfig(
            packageName = "copilot",
            name = "Microsoft Copilot",
            url = "https://copilot.microsoft.com/"
        ),
        AppConfig(
            packageName = "monica",
            name = "Monica",
            url = "https://monica.im/"
        ),
        AppConfig(
            packageName = "github-copilot",
            name = "GitHub Copilot",
            url = "https://github.com/copilot"
        ),
        AppConfig(
            packageName = "z",
            name = "Z.ai",
            url = "https://z.ai"
        ),
        AppConfig(
            packageName = "wps",
            name = "WPS AI",
            url = "https://copilot.wps.cn/"
        ),
        AppConfig(
            packageName = "huggingface",
            name = "Hugging Face",
            url = "https://huggingface.co/chat/"
        ),
        AppConfig(
            packageName = "openrouter",
            name = "OpenRouter",
            url = "https://openrouter.ai/"
        ),
        AppConfig(
            packageName = "youware",
            name = "YouWare",
            url = "https://www.youware.com/"
        ),
        AppConfig(
            packageName = "replit",
            name = "Replit",
            url = "https://replit.com/"
        ),
        AppConfig(
            packageName = "you",
            name = "You",
            url = "https://you.com/?chatMode=default"
        ),
        AppConfig(
            packageName = "N",
            name = "纳米搜索",
            url = "https://www.n.cn/"
        ),
        AppConfig(
            packageName = "xinghuo",
            name = "讯飞星火",
            url = "https://xinghuo.xfyun.cn/"
        ),
        AppConfig(
            packageName = "metaso",
            name = "秘塔AI",
            url = "https://metaso.cn/"
        )
    )
}

data class AppConfig(
    val packageName: String,
    val name: String,
    val url: String
)
