package com.pingping.auth.oauth.kakao.domain

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class KakaoUserDetails(
        private val id: Long,
        private val email: String,
        private val authorities: List<GrantedAuthority>,
        private val attributes: Map<String, Any>
) : OAuth2User {

    override fun getName(): String {
        return email
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getAttributes(): Map<String, Any> {
        return attributes
    }
}