package com.pingping.global.exception

import org.springframework.http.HttpStatus

enum class ExceptionContent(val httpStatus: HttpStatus, val message: String) {

    //user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),

    // token
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "토큰이 요청 헤더에 없습니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트에 등록된 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    INVALID_USER_ID(HttpStatus.UNAUTHORIZED, "토큰에 포함된 사용자 ID가 유효하지 않습니다.")
}
