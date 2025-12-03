package com.example.myfirstkotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.ActivityGoalSelectionBinding
import com.example.myfirstkotlinapp.network.RetrofitClient
import com.example.myfirstkotlinapp.ui.theme.MyFirstKotlinAppTheme
import kotlinx.coroutines.launch

class GoalSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 회원가입에서 넘겨줄 예정인 토큰 (지금은 null이어도 컴파일에는 문제 없음)
        val accessToken = intent.getStringExtra("access_token")

        setContent {
            MyFirstKotlinAppTheme {
                GoalSelectionXmlScreen(
                    accessToken = accessToken,
                    onGoalSelectedAndSaved = { selectedGoal: String ->
                        // ▶ 목표 저장 후 "현재 상태" 화면으로 이동
                        val next = Intent(
                            this@GoalSelectionActivity,
                            CurrentStatusActivity::class.java
                        )
                        startActivity(next)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun GoalSelectionXmlScreen(
    accessToken: String?,
    onGoalSelectedAndSaved: (String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    AndroidViewBinding(ActivityGoalSelectionBinding::inflate) {

        // 뒤로가기
        btnBackFromGoal.setOnClickListener {
            (context as? GoalSelectionActivity)?.finish()


        }

        fun handleClick(goalString: String) {
            coroutineScope.launch {
                try {
                    // 1) 서버에 goalString 전송
                    val api = RetrofitClient.exerciseApi
                    val response = api.setGoal(
                        token = "Bearer $accessToken",
                        goal = goalString,
                        userId = ?
                    )

                    if (response.isSuccessful) {
                        // 2) 성공 시 콜백 호출 → 상위에서 다음 Activity로 이동
                        onGoalSelectedAndSaved(goalString)
                    } else {
                        Toast.makeText(
                            context,
                            "목표 저장 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "네트워크 오류: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // 버튼 3개 클릭 시 각각 다른 코드값 전송
        btnGoalFatLoss.setOnClickListener { handleClick("FAT_LOSS") }
        btnGoalMuscleGain.setOnClickListener { handleClick("MUSCLE_GAIN") }
        btnGoalEndurance.setOnClickListener { handleClick("ENDURANCE") }
    }
}
