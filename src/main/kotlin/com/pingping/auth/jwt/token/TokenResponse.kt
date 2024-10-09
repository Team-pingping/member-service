package com.pingping.auth.jwt.token

data class TokenResponse(
        val accessToken: String,
        val refreshToken: String
) {
    companion object {
        fun of(accessToken: String, refreshToken: String) = TokenResponse(accessToken, refreshToken)
    }
}