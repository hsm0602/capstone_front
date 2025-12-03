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

        // ✅ 회원가입에서 넘겨준 userId 받기
        //   → SignupActivity 에서 putExtra("userId", userId) 로 넣어줘야 함
        val userId = intent.getIntExtra("userId", -1)

        if (userId == -1) {
            Toast.makeText(this, "userId 가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MyFirstKotlinAppTheme {
                GoalSelectionXmlScreen(
                    userId = userId,
                    onGoalSelectedAndSaved = { userId ->
                        // ✅ 목표까지 서버에 저장이 끝난 뒤에만 다음 화면으로 이동
                        //    다음 화면 이름은 팀에서 정한 Activity 로 교체
                        val next = Intent(
                            this@GoalSelectionActivity,
                            GoalSettingActivity::class.java
                        )
                        next.putExtra("userId", userId)
                        // 굳이 goal 이나 userId 를 들고 다닐 필요 없으면 extra 안 넣어도 됨
                        startActivity(next)
                        finish()  // 이 화면은 스택에서 제거
                    }
                )
            }
        }
    }
}

@Composable
fun GoalSelectionXmlScreen(
    userId: Int,
    onGoalSelectedAndSaved: (Int) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidViewBinding(ActivityGoalSelectionBinding::inflate) {

        // ⬅️ 뒤로가기
        btnBackFromGoal.setOnClickListener {
            (context as? GoalSelectionActivity)?.finish()
        }

        // 공통 클릭 처리 함수: goalString 하나만 다르게 넣어줌
        fun handleClick(goalString: String) {
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.authApi.patchGoal(
                        userId = userId,
                        goal = goalString
                    )

                    if (response.isSuccessful) {
                        // ✅ 서버에 정상 저장되었으니 상위 콜백 호출 → 다음 화면으로 이동
                        onGoalSelectedAndSaved(userId)
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

        // ⬇️ 3개 버튼 각각에서 다른 goalString 만 넘겨줌
        btnGoalFatLoss.setOnClickListener   { handleClick("FAT_LOSS") }
        btnGoalMuscleGain.setOnClickListener { handleClick("MUSCLE_GAIN") }
        btnGoalEndurance.setOnClickListener  { handleClick("ENDURANCE") }
    }
}
