package com.seriegel.tv.domain.model

data class UserSession(
    val token: String,
    val username: String? = null,
)
