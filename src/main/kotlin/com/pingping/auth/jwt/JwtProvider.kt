package com.pingping.auth.jwt

import com.pingping.auth.jwt.JwtConst.TOKEN_ISSUER
import com.pingping.global.exception.CustomException
import com.pingping.global.exception.ExceptionContent
import com.pingping.user.domain.repository.UserRepository
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import javax.crypto.SecretKey
import java.util.*

private val log = KotlinLogging.logger {}

@Component
class JwtProvider(
        private val redisTemplate: RedisTemplate<String, String>,
        private val userRepository: UserRepository,
        @Value("\${jwt.secret_key}") private val base64Secret: String,
        @Value("\${jwt.access-token-validity-in-seconds}") accessTokenValiditySeconds: Long,
        @Value("\${jwt.refresh-token-validity-in-seconds}") refreshTokenValiditySeconds: Long
) {

    private val accessTokenTime = accessTokenValiditySeconds * 1000
    private val refreshTokenTime = refreshTokenValiditySeconds * 1000
    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initKey() {
        val keyBytes = Decoders.BASE64.decode(base64Secret)
        secretKey = Keys.hmacShaKeyFor(keyBytes)
    }

    fun createAccessToken(userId: Long): String {
        val user = userRepository.findById(userId)
                .orElseThrow { CustomException(ExceptionContent.USER_NOT_FOUND) }

        val userRole = user.role.name

        val claims = Jwts.claims().subject(userId.toString())
        claims.add("role", userRole)
        val jti = UUID.randomUUID().toString().substring(0, 16) + userId.toString()

        val now = Date()
        return Jwts.builder()
                .id(jti)
                .issuer(TOKEN_ISSUER)
                .claims(claims.build())
                .issuedAt(now)
                .expiration(Date(now.time + accessTokenTime))
                .signWith(secretKey)
                .compact().also {
                    log.info { "[createToken] 핑핑 엑세스 토큰 생성 완료" }
                }
    }

    fun createRefreshToken(userId: Long): String {
        val claims = Jwts.claims().subject(userId.toString())
        val now = Date()
        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .claims(claims.build())
                .issuedAt(now)
                .expiration(Date(now.time + refreshTokenTime))
                .signWith(secretKey)
                .compact().also {
                    log.info { "[createToken] 핑핑 리프레쉬 토큰 생성 완료" }
                }
    }

    fun resolveToken(request: HttpServletRequest): String {
        log.info { "[resolveToken] HTTP 헤더에서 Token 값 추출" }
        val jwtToken = request.getHeader("Authorization") ?: throw CustomException(ExceptionContent.TOKEN_MISSING)

        return if (jwtToken.startsWith("Bearer ")) {
            jwtToken.substring(7)
        } else {
            throw CustomException(ExceptionContent.TOKEN_INVALID)
        }
    }

    fun validateRefreshToken(token: String): Boolean {
        log.info { "[validateRefreshToken] 토큰 유효 체크 시작" }
        val claimsJws = Jwts.parser()
                .requireIssuer(TOKEN_ISSUER)
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)

        return claimsJws.payload.isNotEmpty().also {
            log.info { "[validateRefreshToken] 토큰 유효 체크 완료" }
        }
    }

    fun getUserId(accessToken: String): String {
        return try {
            val claims = Jwts.parser()
                    .requireIssuer(TOKEN_ISSUER)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .payload

            log.info { "[getUserIdFromAccessToken] UserId 추출" }
            log.info { "[UserId] : ${claims.subject}" }  // logger -> log
            log.info { "[JTI] : ${claims.id}" }  // logger -> log
            claims.subject
        } catch (ex: ExpiredJwtException) {
            val claims = ex.claims
            log.info { "[getUserIdFromAccessToken] 만료된 토큰에서 UserId 추출" }
            log.info { "[UserId] : ${claims.subject}" }
            log.info { "[JTI] : ${claims.id}" }
            claims.subject
        }
    }

    fun getJti(token: String): String {
        return try {
            Jwts.parser()
                    .requireIssuer(TOKEN_ISSUER)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload
                    .id
        } catch (ex: ExpiredJwtException) {
            ex.claims.id
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claimsJws = Jwts.parser()
                    .requireIssuer(TOKEN_ISSUER)
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)

            val jti = claimsJws.payload.id

            // 블랙리스트 확인
            if (redisTemplate.opsForSet().isMember("jti:blacklist", jti) == true) {
                throw CustomException(ExceptionContent.TOKEN_BLACKLISTED)
            }

            // 리프레시 토큰 만료 확인
            val refreshToken = redisTemplate.opsForValue()[getUserId(token)]
                    ?: throw CustomException(ExceptionContent.REFRESH_TOKEN_EXPIRED)

            // 최신 토큰 확인
            val existingAccessToken = redisTemplate.opsForValue()[refreshToken]
            if (existingAccessToken.isNullOrEmpty() || existingAccessToken != token) {
                redisTemplate.opsForSet().add("jti:blacklist", jti)
                throw CustomException(ExceptionContent.TOKEN_INVALID)
            }
            true
        } catch (e: ExpiredJwtException) {
            throw CustomException(ExceptionContent.TOKEN_EXPIRED)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(ExceptionContent.TOKEN_INVALID)
        } catch (e: MalformedJwtException) {
            throw CustomException(ExceptionContent.TOKEN_INVALID)
        } catch (e: Exception) {
            false
        }
    }


    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parser()
                .requireIssuer(TOKEN_ISSUER)
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

        val userId = claims.subject.toLongOrNull()
                ?: throw CustomException(ExceptionContent.INVALID_USER_ID)

        val principal = userRepository.findById(userId)
                .orElseThrow { CustomException(ExceptionContent.USER_NOT_FOUND) }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }
}