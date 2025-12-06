package com.example.myfirstkotlinapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.ActivityMyPageBinding
import com.example.myfirstkotlinapp.network.RetrofitClient
import com.example.myfirstkotlinapp.ui.model.CreateBodyCompositionRequest
import com.example.myfirstkotlinapp.ui.model.UserDto
import com.example.myfirstkotlinapp.ui.mypage.BodyCompositionChartView
import com.example.myfirstkotlinapp.ui.theme.MyFirstKotlinAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyFirstKotlinAppTheme {
                MyPageXmlScreen()
            }
        }
    }
}

@Composable
fun MyPageXmlScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 🔐 토큰 가져오기
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val token = prefs.getString("access_token", null)

    if (token == null) {
        Toast.makeText(context, "다시 로그인해 주세요.", Toast.LENGTH_SHORT).show()
        if (context is Activity) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            context.finish()
        }
        return
    }

    // Retrofit API 생성
    val exerciseApi = remember(token) { RetrofitClient.createAuthorizedClient(token) }

    // 현재 그래프 지표
    var currentMetric by remember { mutableStateOf(BodyCompositionChartView.Metric.WEIGHT) }

    // 캐싱된 사용자 정보
    var cachedUser by remember { mutableStateOf<UserDto?>(null) }

    // 최초 실핼 여부
    var initialized by remember { mutableStateOf(false) }


    // ===========================
    //  📌 UI + 로직 시작
    // ===========================
    AndroidViewBinding(ActivityMyPageBinding::inflate) {

        // -----------------------
        // 1️⃣ 사용자 목표 UI 업데이트 함수
        // -----------------------
        fun updateGoalDisplay(user: UserDto) {
            tvUserName.text = user.username
            tvUserGoal.text = user.user_goal ?: "설정된 목표 없음"

            tvGoalValue.text = when (currentMetric) {
                BodyCompositionChartView.Metric.WEIGHT -> {
                    user.goal_state_weight?.let { "%.1f kg".format(it) } ?: "-- kg"
                }
                BodyCompositionChartView.Metric.BODY_FAT -> {
                    user.goal_state_bodyfat?.let { "%.1f %%".format(it) } ?: "-- %"
                }
            }
        }


        // -----------------------
        // 2️⃣ 그래프 로드 함수 (최근 7개)
        // -----------------------
        fun reloadWeeklyChart() {
            coroutineScope.launch {
                try {
                    val metricParam =
                        if (currentMetric == BodyCompositionChartView.Metric.WEIGHT) "weight"
                        else "body_fat"
                    val userId = exerciseApi.getCurrentUser().id
                    val weekly = exerciseApi.getWeeklyBodyComposition(
                        userId,
                        metric = metricParam,
                        days = 7
                    )

                    val points = weekly.points.mapIndexed { index, dto ->
                        BodyCompositionChartView.Point(
                            dayIndex = index,
                            weight = dto.weight,
                            bodyFatPercentage = dto.body_fat_percentage
                        )
                    }

                    chartBodyComposition.setMetric(currentMetric)
                    chartBodyComposition.setData(points)

                } catch (e: Exception) {
                    Toast.makeText(context, "그래프 로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // -----------------------
        // 3️⃣ 사용자 정보 불러오기
        // -----------------------
        fun reloadUserInfo() {
            coroutineScope.launch {
                try {
                    val user = exerciseApi.getCurrentUser()
                    cachedUser = user

                    updateGoalDisplay(user)
                    reloadWeeklyChart()

                } catch (e: Exception) {
                    Toast.makeText(context, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // -----------------------
        // 4️⃣ UI 이벤트 처리
        // -----------------------

        btnBack.setOnClickListener {
            (context as? Activity)?.finish()
        }

        cardUser.setOnClickListener {
            coroutineScope.launch {
                try {
                    val user = cachedUser ?: exerciseApi.getCurrentUser()
                    val intent = Intent(context, GoalSelectionActivity::class.java)
                    intent.putExtra("userId", user.id)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "사용자 정보 없음", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnMetricToggle.setOnClickListener {
            currentMetric =
                if (currentMetric == BodyCompositionChartView.Metric.WEIGHT)
                    BodyCompositionChartView.Metric.BODY_FAT
                else
                    BodyCompositionChartView.Metric.WEIGHT

            cachedUser?.let { updateGoalDisplay(it) }
            reloadWeeklyChart()
        }

        btnAddRecord.setOnClickListener {
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_body_composition_input, null)
            val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
            val etBodyFat = dialogView.findViewById<EditText>(R.id.etBodyFat)

            AlertDialog.Builder(context)
                .setTitle("체성분 입력")
                .setView(dialogView)
                .setPositiveButton("저장") { _, _ ->
                    val weight = etWeight.text.toString().toFloatOrNull()
                    val bodyFat = etBodyFat.text.toString().toFloatOrNull()

                    if (weight == null) {
                        Toast.makeText(context, "올바른 체중 입력 필요", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    val request = CreateBodyCompositionRequest(
                        measured_at = today,
                        weight = weight,
                        body_fat_percentage = bodyFat ?: 0f
                    )

                    coroutineScope.launch {
                        try {
                            val res = exerciseApi.createBodyComposition(request)
                            if (res.isSuccessful) {
                                Toast.makeText(context, "기록 저장됨", Toast.LENGTH_SHORT).show()
                                reloadWeeklyChart()
                            } else Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }


        // -----------------------
        // 5️⃣ 최초 로드
        // -----------------------
        if (!initialized) {
            initialized = true
            reloadUserInfo()
        }
    }
}
