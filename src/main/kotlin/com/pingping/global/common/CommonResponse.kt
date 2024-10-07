package com.pingping.global.common

import org.springframework.http.HttpStatus

data class CommonResponse<D>(
        val code: Int,
        val message: String,
        val data: D? = null
) {
    companion object {
        fun <D> of(status: HttpStatus, message: String, data: D? = null): CommonResponse<D> {
            return CommonResponse(status.value(), message, data)
        }
    }
}
