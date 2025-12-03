package com.example.myfirstkotlinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.ActivityGoalSettingBinding
import com.example.myfirstkotlinapp.ui.theme.MyFirstKotlinAppTheme

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import com.example.myfirstkotlinapp.network.RetrofitClient
import kotlinx.coroutines.launch
import android.content.Intent

class GoalSettingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 회원가입 이후 / 이전 페이지에서 넘겨준 userId 받기
        val userId = intent.getIntExtra("userId", -1)

        // userId 못 받았으면 그냥 종료 (예외 상황 막기용)
        if (userId == -1) {
            finish()
            return
        }

        setContent {
            MyFirstKotlinAppTheme {
                GoalStateScreen(
                    userId = userId,
                    onUpdateSuccess = { userId ->
                        // ✅ 서버 저장 성공 후 홈(MainActivity)으로 이동
                        val intent = Intent(this@GoalSettingActivity, MainActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        // 현재 화면은 스택에서 제거
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun GoalSettingXmlScreen(
    onBack: () -> Unit,
    onNext: (height: String, weight: String, bodyFat: String) -> Unit
) {

    AndroidViewBinding(ActivityGoalSettingBinding::inflate) {
        // this = ActivityGoalSettingBinding

        // 뒤로가기
        tvBack.setOnClickListener {
            onBack()
        }

        // 다음 버튼
        btnNext.setOnClickListener {
            val height = etHeight.text?.toString().orEmpty()
            val weight = etWeight.text?.toString().orEmpty()
            val bodyFat = etBodyFat.text?.toString().orEmpty()

            onNext(height, weight, bodyFat)
        }
    }
}


@Composable
fun GoalStateScreen(
    userId: Int,
    onUpdateSuccess: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // XML 기반 화면을 그대로 사용
    GoalSettingXmlScreen(
        onBack = {
            // 뒤로가기 버튼 눌렀을 때
            (context as? Activity)?.finish()
        },
        onNext = { heightStr, weightStr, bodyFatStr ->
            // 문자열을 숫자로 변환
            val height = heightStr.toFloatOrNull()
            val weight = weightStr.toFloatOrNull()
            val pbf = bodyFatStr.toFloatOrNull()

            if (height == null || weight == null || pbf == null) {
                Toast.makeText(context, "키/몸무게/체지방률을 숫자로 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@GoalSettingXmlScreen
            }

            // 코루틴으로 PATCH 요청
            scope.launch {
                try {
                    val response = RetrofitClient.authApi.patchGoalState(
                        userId = userId,
                        height = height,
                        weight = weight,
                        pbf = pbf
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(context, "목표 상태가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        onUpdateSuccess(userId)
                    } else {
                        Toast.makeText(
                            context,
                            "서버 오류: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "네트워크 오류: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )
}