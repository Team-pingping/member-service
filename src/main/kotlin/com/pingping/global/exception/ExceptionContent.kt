package com.pingping.global.exception

import org.springframework.http.HttpStatus

enum class ExceptionContent(val httpStatus: HttpStatus, val message: String) {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.")
}
