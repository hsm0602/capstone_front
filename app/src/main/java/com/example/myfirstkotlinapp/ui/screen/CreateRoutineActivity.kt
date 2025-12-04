package com.example.myfirstkotlinapp.ui.screen

import android.content.Context
import android.content.Intent
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
        // 화면을 Compose 기반으로 렌더링
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
    val scope = rememberCoroutineScope()   // Retrofit 실행할 코루틴 scope

    // ⬇️ XML(ActivityRoutineCreate.xml)을 Compose 안에서 그대로 사용하기
    AndroidViewBinding(ActivityRoutineCreateBinding::inflate) {

        nextBtn.setOnClickListener {
            val goal = editGoal.text.toString().trim()

            // 🔍 입력값 체크
            if (goal.isEmpty()) {
                Toast.makeText(context, "목표를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 네트워크 요청 (코루틴 사용)
            scope.launch {
                val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val token = sharedPref.getString("access_token", null)

                if (token.isNullOrBlank()) {
                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch  // 코루틴만 종료
                }

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
            }
        }

        /**
         * [뒤로가기 버튼]
         * - 단순히 이전 화면으로 돌아감
         */
        backBtn.setOnClickListener {
            (context as ComponentActivity).onBackPressedDispatcher.onBackPressed()
        }
    }
}
