package com.example.myfirstkotlinapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.myfirstkotlinapp.network.RetrofitClient
import kotlinx.coroutines.launch

class CreateRoutineActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getIntExtra("userId", -1)

        setContent {
            RoutineInputScreen(
                userId = userId,
                onNext = { goal ->
                    // goal을 다음 액티비티로 넘기기
                    val intent = Intent(this, RoutineResultActivity::class.java)
                    intent.putExtra("goal", goal)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun RoutineInputScreen(
    userId: Int,
    onNext: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AndroidViewBinding(ActivityRoutineInputBinding::inflate) {

        nextBtn.setOnClickListener {

            val goal = editGoal.text.toString().trim()
            if (goal.isEmpty()) {
                Toast.makeText(context, "목표를 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scope.launch {
                try {
                    val response = RetrofitClient.authApi.patchGoal(
                        userId = userId,
                        goal = goal
                    )
                    if (response.isSuccessful) {
                        onNext(goal)
                    } else {
                        Toast.makeText(context, "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
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

@Composable
fun AndroidViewBinding(x0: inflate, content: @Composable () -> setOnClickListener) {
    TODO("Not yet implemented")
}