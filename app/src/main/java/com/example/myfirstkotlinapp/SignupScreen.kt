package com.example.myfirstkotlinapp

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.myfirstkotlinapp.ui.model.SignupRequestDto
import com.example.myfirstkotlinapp.network.RetrofitClient
import com.example.myfirstkotlinapp.databinding.ActivitySignUpBinding
import androidx.compose.ui.viewinterop.AndroidViewBinding
import android.app.Activity


@Composable
fun SignupScreen(onSignupSuccess: (Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidViewBinding(ActivitySignUpBinding::inflate) {

        tvBack.setOnClickListener {
            (context as? Activity)?.finish()
        }

        btnSignUp.setOnClickListener {
            val username = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "모든 값을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            coroutineScope.launch {
                try {
                    val request = SignupRequestDto(username, email, password)
                    val response = RetrofitClient.authApi.signup(request)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            onSignupSuccess(body.id) // userId 넘겨주기.
                        } else {
                            Toast.makeText(context, "응답이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "회원가입 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "에러: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

