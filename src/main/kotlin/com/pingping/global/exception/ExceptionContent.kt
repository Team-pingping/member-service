package com.pingping.global.exception

import org.springframework.http.HttpStatus

enum class ExceptionContent(val httpStatus: HttpStatus, val message: String) {

    // 예시 예외 수정 필요
    BAD_REQUEST_STAGE_STATUS_TYPE(HttpStatus.BAD_REQUEST, "잘못된 요청입니다. 유효하지 않은 채용 상태입니다. (준비중, 합격, 불합격으로 입력해주세요.)"),
    NO_VALID_STAGE_FOUND(HttpStatus.NOT_FOUND, "채용 일정에 채용 전형이 존재하지 않습니다.")
}
