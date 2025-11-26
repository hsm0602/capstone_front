package com.example.myfirstkotlinapp.ui.model

import com.google.gson.annotations.SerializedName

data class ExerciseRecordDto(
    val id: Int,
    @SerializedName("exercise_id") val exerciseId: Int,
    @SerializedName("exercise_name") val exerciseName: String,
    val date: String,
    val weight: Float,
    val reps: Int,
    @SerializedName("is_completed") val isCompleted: Boolean
)