package com.pingping.global.exception

class CustomException(
        val content: ExceptionContent
) : RuntimeException(content.message)
