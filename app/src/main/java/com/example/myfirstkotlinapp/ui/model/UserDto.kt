package com.example.myfirstkotlinapp.ui.model

data class UserDto(
    val id: Int,
    val username: String,
    val email: String,

    val user_goal: String?,               // 목표명
    val goal_state_weight: Float?,        // 목표 체중
    val goal_state_bodyfat: Float?        // 목표 체지방률

)
