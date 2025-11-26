package com.example.myfirstkotlinapp.network

import com.example.myfirstkotlinapp.ui.model.SignupRequestDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @FormUrlEncoded
    @POST("token") // 로그인을 통한 토큰 발급
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("/signup") // 회원가입
    suspend fun signup(@Body signupRequest: SignupRequestDto): Response<SignupResponse>

    @PATCH("goal/me") // 창수꺼
    suspend fun patchGoal(
        @Query("user_id") userId: Int,
        @Query("goal") goal: String
    ): Response<Void>

    @PATCH("goal/recent_state") // 선아꺼
    suspend fun patchRecentState(
        @Query("user_id") userId: Int,
        @Query("height") height: Float,
        @Query("weight") weight: Float,
        @Query("pbf") pbf: Float
    ): Response<Void>

    @PATCH("goal/goal_state") // 민준꺼
    suspend fun patchGoalState(
        @Query("user_id") userId: Int,
        @Query("height") height: Float,
        @Query("weight") weight: Float,
        @Query("pbf") pbf: Float
    ): Response<Void>
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class SignupResponse(
    val id: Int,
    val username: String,
    val email: String
)
