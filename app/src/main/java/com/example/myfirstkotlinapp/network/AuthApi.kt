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
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("/signup")
    suspend fun signup(@Body signupRequest: SignupRequestDto): Response<SignupResponse>
    @PATCH("goal/recent_state") // 요청 경로. RetrofitClient의 baseUrl 뒤에 붙어서 백엔드로 요청을 보냄
    suspend fun patchRecentState(
        @Query("user_id") userId: Int, // 괄호 안의 "user_id"는 백엔드에서 정의한 register_recent_state함수가 입력값을 user_id, height, weight, pbf이라는 변수로 받기 때문에 변수명이 일치해야함. 괄호 뒤에 userId는 프런트 코드에서 접근하기 위한 변수. 프런트 코드에서는 userId를 사용해야함.
        @Query("height") height: Float, // 위 설명과 동일. 괄호 안은 백엔드 변수와 통일, 뒤는 프런트 코드에서 사용하는 변수
        @Query("weight") weight: Float, // 위 설명과 동일.
        @Query("pbf") pbf: Float // 위 설명과 동일.
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

