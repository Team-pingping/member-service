package com.pingping.user.application

import com.pingping.user.domain.User
import com.pingping.user.domain.repository.UserRepository
import com.pingping.global.exception.CustomException
import com.pingping.global.exception.ExceptionContent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
        private val userRepository: UserRepository
) {

    fun getUserInfo(userId: Long): User {
        return userRepository.findById(userId)
                .orElseThrow { CustomException(ExceptionContent.USER_NOT_FOUND) }
    }
    @Transactional
    fun updateNickname(userId: Long, nickname: String) {
        val user = userRepository.findById(userId)
                .orElseThrow { CustomException(ExceptionContent.USER_NOT_FOUND) }

        user.nickName = nickname
    }

    @Transactional
    fun deleteUserByEmail(email: String) {
        val user = userRepository.findByEmail(email)
                ?: throw CustomException(ExceptionContent.USER_NOT_FOUND)

        userRepository.delete(user)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        userRepository.deleteById(userId)
    }
}
