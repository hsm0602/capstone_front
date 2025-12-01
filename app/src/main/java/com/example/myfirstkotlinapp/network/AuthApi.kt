package com.example.myfirstkotlinapp.network

import com.example.myfirstkotlinapp.ui.model.SignupRequestDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApi {
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("/signup")
    suspend fun signup(@Body signupRequest: SignupRequestDto): Response<SignupResponse>
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
