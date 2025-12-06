package com.example.myfirstkotlinapp.ui.screen

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.ActivityRoutineCreateBinding
import com.example.myfirstkotlinapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CreateRoutineActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoutineInputScreen(
                onNext = {
                    finish()
                }
            )
        }
    }
}

@Composable
fun RoutineInputScreen(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    AndroidViewBinding(ActivityRoutineCreateBinding::inflate) {

        progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE // 루틴 생성 버튼 이후 로딩바.
        nextBtn.isEnabled = !isLoading

        nextBtn.setOnClickListener {
            val goal = editGoal.text.toString().trim()

            if (goal.isEmpty()) {
                Toast.makeText(context, "목표를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scope.launch {
                val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val token = sharedPref.getString("access_token", null)

                if (token.isNullOrBlank()) {
                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                try {
                    isLoading = true
                    val authedApi = RetrofitClient.createAuthorizedClient(token)

                    val userInfo = withContext(Dispatchers.IO) {
                        authedApi.getCurrentUser()
                    }
                    val userId = userInfo.id

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val today = sdf.format(Date())

                    val response = authedApi.generatePlan(
                        userId = userId,
                        date = today,
                        constraints = goal
                    )

                    if (response.isSuccessful) {
                        onNext()
                    } else {
                        Toast.makeText(context, "루틴 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }

        backBtn.setOnClickListener {
            (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
        }
    }
}
