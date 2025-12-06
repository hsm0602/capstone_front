package com.example.myfirstkotlinapp.ui.model

// 마이페이지 전용 체성분 DTO들

data class BodyCompositionPointDto(
    val measured_at: String,          // "yyyy-MM-dd"
    val weight: Float?,               // kg
    val body_fat_percentage: Float?   // %
)

data class WeeklyBodyCompositionResponse(
    val points: List<BodyCompositionPointDto>
)

data class CreateBodyCompositionRequest(
    val measured_at: String,          // "yyyy-MM-dd"
    val weight: Float,
    val body_fat_percentage: Float
)
