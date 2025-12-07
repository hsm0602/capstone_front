package com.example.myfirstkotlinapp.network

import com.example.myfirstkotlinapp.ui.model.ExerciseRecordUpdateDto
import com.example.myfirstkotlinapp.ui.model.UserDto
import com.example.myfirstkotlinapp.ui.model.ExerciseRecordDto
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response
import com.example.myfirstkotlinapp.ui.model.WeeklyBodyCompositionResponse
import com.example.myfirstkotlinapp.ui.model.CreateBodyCompositionRequest

interface ExerciseApi {
    @PATCH("exercise/records/{record_id}")
    suspend fun patchExerciseRecord(
        @Path("record_id") recordId: Int,
        @Body data: ExerciseRecordUpdateDto
    )

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @GET("exercise/records")
    suspend fun getExerciseRecord(
        @Query("user_id") userId: Int,
        @Query("date") date: String   // today: "yyyy-MM-dd"
    ): List<ExerciseRecordDto>

    @GET("exercise/body_composition/weekly")
    suspend fun getWeeklyBodyComposition(
        @Query("user_id") userId: Int,
        @Query("metric") metric: String, // "weight" or "body_fat"
        @Query("days") days: Int = 7
    ): WeeklyBodyCompositionResponse

    @POST("body_composition")
    suspend fun createBodyComposition(
        @Body body: CreateBodyCompositionRequest
    ): Response<Void>


    @POST("plan/generate-and-save")
    suspend fun generatePlan(
        @Query("user_id") userId: Int,
        @Query("date") date: String,
        @Query("constraints") constraints: String
    ): Response<Void>
}
