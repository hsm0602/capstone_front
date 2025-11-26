package com.example.myfirstkotlinapp.ui.model

import java.util.UUID

// 운동 1개 (ex: 스쿼트)
data class ExercisePlan(
    val id: Int,
    val name: String,                       // 운동 이름
    val sets: List<ExerciseSet>             // 세트 리스트
)

// 세트 1개 (ex: 60kg 10회)
data class ExerciseSet(
    val weight: Int,    // 중량 (kg)
    val reps: Int,      // 횟수
    val isCompleted: Boolean
)