package com.example.myfirstkotlinapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.ActivitySurvey2Binding
import com.example.myfirstkotlinapp.network.RetrofitClient
import kotlinx.coroutines.launch

class SurveyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getIntExtra("userId", -1)

        setContent {
            SurvayXmlScreen(
                userId = userId,
                onNext = { userId ->
                    val intent = Intent(this, GoalSettingActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun SurvayXmlScreen(userId: Int, onNext: (Int) -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AndroidViewBinding(ActivitySurvey2Binding::inflate) {

        nextBtn.setOnClickListener {

            val height = editHeight.text.toString().trim()
            val weight = editWeight.text.toString().trim()
            val fat = editFat.text.toString().trim()

            if (height.isEmpty() || weight.isEmpty() || fat.isEmpty()) {
                Toast.makeText(context, "모든 값을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightF = height.toFloatOrNull()
            val weightF = weight.toFloatOrNull()
            val fatF = fat.toFloatOrNull()

            if (heightF == null || weightF == null || fatF == null) {
                Toast.makeText(context, "숫자로 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scope.launch {
                try {
                    val response = RetrofitClient.authApi.patchRecentState(
                        userId = userId,
                        height = heightF,
                        weight = weightF,
                        pbf = fatF
                    )

                    if (response.isSuccessful) {
                        Toast.makeText(context, "저장되었습니다!", Toast.LENGTH_SHORT).show()
                        onNext(userId)
                    } else {
                        Toast.makeText(context, "실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        backBtn.setOnClickListener {
            (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
        }
    }
}