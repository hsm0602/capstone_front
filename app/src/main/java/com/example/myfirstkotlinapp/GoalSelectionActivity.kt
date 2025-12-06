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

        val userId = intent.getIntExtra("userId", -1) // userId 넘겨 받기.

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
                        val next = Intent(
                            this@GoalSelectionActivity,
                            SurveyActivity::class.java
                        )
                        next.putExtra("userId", userId)
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
    userId: Int,
    onGoalSelectedAndSaved: (Int) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AndroidViewBinding(ActivityGoalSelectionBinding::inflate) {

        backBtn.setOnClickListener {
            (context as? GoalSelectionActivity)?.finish()
        }

        // 공통 클릭 처리 함수.
        fun handleClick(goalString: String) {
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.authApi.patchGoal(
                        userId = userId,
                        goal = goalString
                    )

                    if (response.isSuccessful) {
                        onGoalSelectedAndSaved(userId) // userId 넘겨주기.
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

        // 선택한 goalString 넘겨주기.
        btnGoalFatLoss.setOnClickListener   { handleClick("FAT_LOSS") }
        btnGoalMuscleGain.setOnClickListener { handleClick("MUSCLE_GAIN") }
        btnGoalEndurance.setOnClickListener  { handleClick("ENDURANCE") }
    }
}
