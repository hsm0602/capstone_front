package com.example.myfirstkotlinapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myfirstkotlinapp.session.WorkoutSessionManager
import com.example.myfirstkotlinapp.ui.model.ExercisePlan

@Composable
fun WorkoutScreen() {
    var isDoingWorkout by remember { mutableStateOf(false) } // 운동 flag
    var isResting by remember { mutableStateOf(false) } // 휴식 flag
    var isWorkoutComplete by remember { mutableStateOf(false) } // 운동 완료 flag
    var selectedPlans by remember { mutableStateOf<List<ExercisePlan>>(emptyList()) } // 운동 목록
    var recordIds by remember { mutableStateOf<List<Int>>(emptyList()) } // 각 운동 목록의 DB 레코드 id

    // 운동 목록을 세션 매니저로 관리, 운동 시간 계산.
    val sessionManager = remember(selectedPlans, recordIds) {
        if (selectedPlans.isNotEmpty() && recordIds.isNotEmpty()) {
            WorkoutSessionManager(selectedPlans, recordIds)
        } else null
    }

    // 첫 운동 시작 시 startWorkout 호출 보장.
    LaunchedEffect(isDoingWorkout, sessionManager) {
        if (isDoingWorkout && sessionManager != null && !isResting && !isWorkoutComplete) {
            sessionManager.startWorkout()
        }
    }

    when {
        isWorkoutComplete && sessionManager != null -> {
            WorkoutCompleteScreen(
                sessionManager = sessionManager,
                onFinish = {
                    isDoingWorkout = false
                    isResting = false
                    isWorkoutComplete = false
                    selectedPlans = emptyList()
                    recordIds = emptyList()
                }
            )
        }

        isResting && sessionManager != null -> {
            IntermediateScreen(
                sessionManager = sessionManager,
                restDurationSec = 30,
                onRestComplete = {
                    sessionManager.endRest()
                    sessionManager.advanceToNextSet()

                    // 다음 세트가 있는지 확인하고 운동 시작
                    if (!sessionManager.isFinished) {
                        isResting = false
                        isDoingWorkout = true  // 다음 세트 수행
                    } else {
                        // 모든 운동이 끝난 경우
                        isResting = false
                        isWorkoutComplete = true
                    }
                }
            )
        }

        isDoingWorkout && sessionManager != null -> { // 운동 중일 때
            PoseCameraScreen(
                sessionManager = sessionManager,
                onSetComplete = {
                    sessionManager.endWorkout()
                    sessionManager.markSetAsCompleted()

                    if (sessionManager.isFinished) {
                        isDoingWorkout = false
                        isWorkoutComplete = true
                    } else {
                        sessionManager.startRest()
                        isDoingWorkout = false
                        isResting = true
                        // → 휴식 화면으로 이동
                    }
                }
            )
        }

        else -> { // 홈 화면
            HomeScreen(
                onStartWorkout = { plans, ids ->
                    selectedPlans = plans // 운동 목록 저장
                    recordIds = ids
                    isDoingWorkout = true // 운동 시작
                }
            )
        }
    }
}