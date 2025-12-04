package com.example.myfirstkotlinapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstkotlinapp.session.WorkoutSessionManager
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.IntermediateScreenBinding
import kotlinx.coroutines.delay

@Composable
fun IntermediateScreen(
    sessionManager: WorkoutSessionManager,
    restDurationSec: Int = 30,
    onRestComplete: () -> Unit
) {
    var secondsRemaining by remember { mutableStateOf(restDurationSec) }

    // ⏱ 휴식 타이머: 1초마다 감소
    LaunchedEffect(restDurationSec) {
        secondsRemaining = restDurationSec
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        onRestComplete()
    }

    AndroidViewBinding(
        modifier = Modifier,
        factory = IntermediateScreenBinding::inflate
    ) {
        // --- 1) 원형 타이머 + 숫자 바인딩 ---

        // CircularProgressIndicator의 최대값을 휴식 시간으로 설정
        circleTimer.max = restDurationSec
        // 남은 시간을 그대로 progress로 쓸지, 경과 시간을 쓸지 선택 가능
        // 여기서는 남은 시간을 표시하도록 설정 (필요하면 반대로 바꿔도 됨)
        circleTimer.progress = secondsRemaining

        // 가운데 숫자
        tvRestSecond.text = secondsRemaining.toString()

        // --- 2) NOW / NEXT 세트 정보 바인딩 ---

        val nowInfo = sessionManager.getNowSetInfo()
        val nextInfo = sessionManager.getNextSetInfo()

        // NOW: 방금 수행한 세트
        tvNowExerciseName.text = nowInfo?.first?.name ?: "-"
        tvNowReps.text = nowInfo?.second?.reps?.toString() ?: "-"

        // NEXT: 휴식 후에 진행할 세트 (없으면 "-")
        tvNextExerciseName.text = nextInfo?.first?.name ?: "-"
        tvNextReps.text = nextInfo?.second?.reps?.toString() ?: "-"

        // 운동 이미지(iv_now_exercise, iv_next_exercise)는 지금은 공통 그림 사용 중이라면 그대로 두고,
        // 나중에 운동별 썸네일 매핑 로직 넣으면 됨.
        // ivNowExercise.setImageResource(...)
        // ivNextExercise.setImageResource(...)
    }
}
