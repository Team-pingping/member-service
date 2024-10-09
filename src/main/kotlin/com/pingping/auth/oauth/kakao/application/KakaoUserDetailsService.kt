package com.pingping.auth.oauth.kakao.application

import com.pingping.auth.oauth.kakao.domain.KakaoUserDetails
import com.pingping.auth.oauth.kakao.domain.KakaoUserInfo
import com.pingping.user.domain.User
import com.pingping.user.domain.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class KakaoUserDetailsService(
        private val userRepository: UserRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val kakaoUserInfo = KakaoUserInfo(oAuth2User.attributes)

        val email = kakaoUserInfo.getEmail() ?: throw OAuth2AuthenticationException("Email is required")

        val user = userRepository.findByEmail(email)
                ?: userRepository.save(User.createFirstLoginUser(email))

        val authority = SimpleGrantedAuthority(user.role.name)

        return KakaoUserDetails(
                user.id,
                user.email,
                listOf(authority),
                oAuth2User.attributes
        )
    }
}