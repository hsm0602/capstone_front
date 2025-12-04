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
import com.example.myfirstkotlinapp.databinding.ActivityRoutineCreateBinding
import com.example.myfirstkotlinapp.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * CreateRoutineActivity
 *
 * - 사용자가 "운동 루틴 목표"를 입력하는 화면
 * - XML(ActivityRoutineCreate.xml)을 Compose 안에서 ViewBinding 형태로 사용함
 * - 목표 입력 후 → 서버에 PATCH 요청으로 목표 저장 → RoutineResultActivity로 이동
 */
class CreateRoutineActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Home 화면에서 Intent로 전달된 사용자 ID (루틴 생성 시 필요한 값)
        val userId = intent.getIntExtra("userId", -1)

        // 화면을 Compose 기반으로 렌더링
        setContent {
            RoutineInputScreen(
                userId = userId,
                onNext = { goal ->
                    // 목표 입력이 성공하면 다음 화면(RoutineResultActivity)로 이동
                    val intent = Intent(this, RoutineResultActivity::class.java)
                    intent.putExtra("goal", goal)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
            )
        }
    }
}

/**
 * RoutineInputScreen()
 *
 * - Compose 구조 안에서 XML(ViewBinding)을 그대로 사용하는 화면
 * - ActivityRoutineCreateBinding 을 inflate 하여 editGoal / nextBtn / backBtn 접근 가능
 * - 목표 입력 후 Retrofit API 호출 → 성공 시 onNext() 콜백 실행
 */
@Composable
fun RoutineInputScreen(
    userId: Int,
    onNext: (String) -> Unit       // 목표 저장이 성공했을 때 다음 화면으로 넘겨주는 콜백
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()   // Retrofit 실행할 코루틴 scope

    // ⬇️ XML(ActivityRoutineCreate.xml)을 Compose 안에서 그대로 사용하기
    AndroidViewBinding(ActivityRoutineCreateBinding::inflate) {

        /**
         * [다음 버튼 클릭 시 동작]
         * 1. 입력된 목표(goal) 문자열 읽기
         * 2. 빈 칸 체크
         * 3. Retrofit 으로 PATCH 요청 (goal 업데이트)
         * 4. 성공 → onNext(goal) 실행하여 다음 액티비티로 이동
         */
        nextBtn.setOnClickListener {
            val goal = editGoal.text.toString().trim()

            // 🔍 입력값 체크
            if (goal.isEmpty()) {
                Toast.makeText(context, "목표를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 네트워크 요청 (코루틴 사용)
            scope.launch {
                try {
                    val response = RetrofitClient.authApi.patchGoal(
                        userId = userId,
                        goal = goal
                    )

                    // 성공했을 때 onNext(goal) 콜백 호출 → 다음 화면으로 이동
                    if (response.isSuccessful) {
                        onNext(goal)
                    } else {
                        Toast.makeText(context, "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    // 네트워크 끊김, 서버 오류 등 예외 처리
                    Toast.makeText(context, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
