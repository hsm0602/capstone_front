package com.example.myfirstkotlinapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.myfirstkotlinapp.MainActivity
import com.example.myfirstkotlinapp.network.RetrofitClient
import com.example.myfirstkotlinapp.ui.theme.MyFirstKotlinAppTheme
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.core.content.edit
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.ActivityLoginBinding


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstKotlinAppTheme {
                LoginXmlScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, WelcomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginXmlScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidViewBinding(ActivityLoginBinding::inflate) {
        val etId = etEmail
        val etPw = etPassword

        btnLogin.setOnClickListener {
            val userId = etId.text?.toString().orEmpty()
            val password = etPw.text?.toString().orEmpty()

            coroutineScope.launch {
                try {
                    val response = RetrofitClient.authApi.login(userId, password)
                    if (response.isSuccessful) {
                        val token = response.body()?.accessToken
                        if (token != null) {
                            val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                            sharedPref.edit { putString("access_token", token) }
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "토큰이 비어 있습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 회원가입
        tvSignUp.setOnClickListener {
            context.startActivity(Intent(context, SignupActivity::class.java))
        }
    }
}
