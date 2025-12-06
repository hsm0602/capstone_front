package com.example.myfirstkotlinapp.ui.screen

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.myfirstkotlinapp.session.WorkoutSessionManager
import kotlinx.coroutines.delay
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.IntermediateScreenBinding

@Composable
fun IntermediateScreen(
    sessionManager: WorkoutSessionManager,
    restDurationSec: Int = 30,
    onRestComplete: () -> Unit
) {
    var secondsRemaining by remember { mutableStateOf(restDurationSec) }

    // 휴식 타이머: 1초마다 감소
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
        // 원형 타이머 + 숫자 바인딩.

        // CircularProgressIndicator의 최대값을 휴식 시간으로 설정
        circleTimer.max = restDurationSec

        // 남은 시간에 따라 원형 타이머가 줄어들게.
        circleTimer.progress = secondsRemaining

        // 남은 시간.
        tvRestSecond.text = secondsRemaining.toString()

        // NOW / NEXT 세트 정보 바인딩.
        val nowInfo = sessionManager.getNowSetInfo()
        val nextInfo = sessionManager.getNextSetInfo()

        // NOW: 방금 수행한 세트.
        tvNowExerciseName.text = nowInfo?.first?.name ?: "-"
        tvNowReps.text = nowInfo?.second?.reps?.toString() ?: "-"

        // NEXT: 휴식 후에 진행할 세트.
        tvNextExerciseName.text = nextInfo?.first?.name ?: "-"
        tvNextReps.text = nextInfo?.second?.reps?.toString() ?: "-"
    }
}
