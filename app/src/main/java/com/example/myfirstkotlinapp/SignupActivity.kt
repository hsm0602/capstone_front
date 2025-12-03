package com.example.myfirstkotlinapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myfirstkotlinapp.ui.theme.MyFirstKotlinAppTheme

import android.content.Intent

class SignupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstKotlinAppTheme {
                SignupScreen (
                    onSignupSuccess = {
                        // 🔵 회원가입 성공 후: 목표선택 화면으로 이동
                        val intent = Intent(
                            this@SignupActivity,
                            GoalSelectionActivity::class.java   // 네가 만든 Activity 이름
                        )
                        startActivity(intent)

                        // 이 액티비티는 종료해서 뒤로가기 눌렀을 때 다시 안 오게
                        finish()
                    }
                )
            }
        }
    }
}
