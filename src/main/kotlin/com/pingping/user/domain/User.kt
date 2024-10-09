package com.pingping.user.domain

import com.pingping.global.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
        @Column(nullable = false, updatable = false)
        val email: String,

        @Enumerated(EnumType.STRING)
        val role: Role,

        var nickName: String? = null,

        var age: Int? = null,

        @Enumerated(EnumType.STRING)
        var gender: Gender? = null
) : BaseTimeEntity() {
    protected constructor() : this(
            email = "",
            role = Role.USER,
            nickName = null,
            age = null,
            gender = null
    )

    companion object {
        fun createFirstLoginUser(email: String): User {
            return User(
                    email = email,
                    role = Role.USER,
                    nickName = null,
                    age = null,
                    gender = null
            )
        }
    }
}