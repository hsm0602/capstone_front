package com.example.myfirstkotlinapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.myfirstkotlinapp.ui.model.SignupRequestDto
import com.example.myfirstkotlinapp.network.RetrofitClient


@Composable
fun SignupScreen(onSignupSuccess: (Int) -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("회원가입", fontSize = 24.sp)

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val request = SignupRequestDto(username, email, password)
                        val response = RetrofitClient.authApi.signup(request)
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                                onSignupSuccess(body.id)
                            }
                        } else {
                            Toast.makeText(context, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("회원가입", color = Color.White)
        }
    }
}
