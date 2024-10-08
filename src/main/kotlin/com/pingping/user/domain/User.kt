package com.pingping.user.domain

import com.pingping.global.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class User(
        @Column(nullable = false, updatable = false)
        val email: String,

        @Enumerated(EnumType.STRING)
        val role: Role,

        @Column
        var nickName: String? = null,

        @Column
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
}