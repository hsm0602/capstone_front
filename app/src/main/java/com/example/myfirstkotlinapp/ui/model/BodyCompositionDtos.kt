package com.example.myfirstkotlinapp.ui.model

// 마이페이지 전용 체성분 DTO들
// ⚠️ 백엔드에서 아직 구현되지 않았다면, 서버 스펙에 맞게 필드명/타입을 조정해 주세요.

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
