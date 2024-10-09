package com.pingping.auth.oauth.kakao.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

class KakaoUserInfo(
        private val attributes: Map<String, Any>
) {
    companion object {
        const val KAKAO_ACCOUNT = "kakao_account"
        const val EMAIL = "email"
    }

    fun getEmail(): String? {
        val kakaoAccount = attributes[KAKAO_ACCOUNT] as? Map<*, *>
        return kakaoAccount?.get(EMAIL) as? String
    }
}
