package com.example.myfirstkotlinapp.network

import com.example.myfirstkotlinapp.ui.model.SignupRequestDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PATCH
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

    @PATCH("goal/goal_state")
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
