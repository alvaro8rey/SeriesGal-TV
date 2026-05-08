package com.seriegel.tv.core.config

import com.seriegel.tv.BuildConfig

object ServerConfig {
    val API_BASE_URL: String = BuildConfig.API_BASE_URL
    val WEB_BASE_URL: String = BuildConfig.WEB_BASE_URL

    fun streamUrl(path: String): String {
        return WEB_BASE_URL.trimEnd('/') + "/" + path.trimStart('/')
    }

    fun coverUrl(contentId: String): String {
        return "${WEB_BASE_URL.trimEnd('/')}/images/$contentId.jpg"
    }
}
