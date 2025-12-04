package com.example.myfirstkotlinapp.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.myfirstkotlinapp.session.WorkoutSessionManager
import com.example.myfirstkotlinapp.network.RetrofitClient
import com.example.myfirstkotlinapp.ui.model.ExerciseRecordUpdateDto


@Composable
fun WorkoutCompleteScreen(
    sessionManager: WorkoutSessionManager,
    onFinish: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var patchSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val token = sharedPref.getString("access_token", null)
                if (token != null) {
                    sessionManager.setTimings.forEach { set ->
                        val dto = ExerciseRecordUpdateDto(
                            exerciseTime = set.workoutDuration().toInt(),
                            restTime = set.restDuration().toInt(),
                            isCompleted = true
                        )
                        val authedApi = RetrofitClient.createAuthorizedClient(token)
                        authedApi.patchExerciseRecord(set.recordId, dto)
                    }
                    patchSent = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: 사용자에게 오류 알림 가능
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("운동이 완료되었습니다!", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        val (workoutMs, restMs) = sessionManager.getWorkoutSummary()
        Text("총 운동 시간: ${workoutMs / 1000}초")
        Text("총 휴식 시간: ${restMs / 1000}초")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFinish,
            enabled = patchSent
        ) {
            Text("메인 화면으로")
        }
    }
}
