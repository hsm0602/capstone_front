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

class GoalSettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getIntExtra("userId", -1)

        if (userId == -1) {
            finish()
            return
        }

        setContent {
            MyFirstKotlinAppTheme {
                GoalStateScreen(
                    userId = userId,
                    onUpdateSuccess = {
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

        backBtn.setOnClickListener {
            onBack()
        }

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
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    GoalSettingXmlScreen(
        onBack = {
            (context as? Activity)?.finish()
        },
        onNext = { heightStr, weightStr, bodyFatStr ->
            val height = heightStr.toFloatOrNull()
            val weight = weightStr.toFloatOrNull()
            val pbf = bodyFatStr.toFloatOrNull()

            if (height == null || weight == null || pbf == null) {
                Toast.makeText(context, "키/몸무게/체지방률을 숫자로 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@GoalSettingXmlScreen
            }

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
                        onUpdateSuccess()
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