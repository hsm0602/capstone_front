package com.example.myfirstkotlinapp.ui.model

import com.google.gson.annotations.SerializedName

data class ExerciseRecordUpdateDto(
    @SerializedName("exercise_time") val exerciseTime: Int, // 초 단위
    @SerializedName("rest_time") val restTime: Int,      // 초 단위
    @SerializedName("is_completed") val isCompleted: Boolean
)
