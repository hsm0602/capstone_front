// WorkoutSessionManager.kt (세트별 운동 및 휴식 시간 기록 포함)

package com.example.myfirstkotlinapp.session

import com.example.myfirstkotlinapp.ui.model.ExercisePlan
import com.example.myfirstkotlinapp.ui.model.ExerciseSet

class WorkoutSessionManager(
    val plans: List<ExercisePlan>,
    val recordIds: List<Int> // 세트별 recordId 저장
) {
    private var currentExerciseIndex = 0
    var currentSetIndex = 0

    private var _completedSets: MutableList<Pair<Int, Int>> = mutableListOf()
    val completedSets: List<Pair<Int, Int>> get() = _completedSets

    private val _setTimings = mutableListOf<SetTiming>()
    val setTimings: List<SetTiming> get() = _setTimings

    val isFinished: Boolean
        get() = currentExerciseIndex >= plans.size

    val currentExercise: ExercisePlan?
        get() = if (isFinished) null else plans[currentExerciseIndex]

    val currentSet: ExerciseSet?
        get() = currentExercise?.sets?.getOrNull(currentSetIndex)

    fun startWorkout() {
        val recordId = recordIds.getOrNull(getFlatSetIndex()) ?: return
        _setTimings.add(SetTiming(recordId = recordId).apply {
            workoutStart = System.currentTimeMillis()
        })
    }

    fun endWorkout() {
        _setTimings.lastOrNull()?.workoutEnd = System.currentTimeMillis()
    }

    fun startRest() {
        _setTimings.lastOrNull()?.restStart = System.currentTimeMillis()
    }

    fun endRest() {
        _setTimings.lastOrNull()?.restEnd = System.currentTimeMillis()
    }

    fun markSetAsCompleted() {
        _completedSets.add(Pair(currentExerciseIndex, currentSetIndex))
    }

    fun advanceToNextSet() {
        val sets = currentExercise?.sets ?: return
        currentSetIndex++
        if (currentSetIndex >= sets.size) {
            currentExerciseIndex++
            currentSetIndex = 0
        }

        // 다음 세트가 있는 경우에만 새로운 SetTiming 생성
//        if (!isFinished) {
//            val recordId = recordIds.getOrNull(getFlatSetIndex()) ?: return
//            _setTimings.add(SetTiming(recordId = recordId).apply {
//                workoutStart = System.currentTimeMillis()
//            })
//        }
    }

    private fun getFlatSetIndex(): Int {
        var index = 0
        for (i in 0 until currentExerciseIndex) {
            index += plans[i].sets.size
        }
        return index + currentSetIndex
    }

    fun getWorkoutSummary(): Pair<Long, Long> {
        val totalWorkout = _setTimings.sumOf { it.workoutDuration() }
        val totalRest = _setTimings.sumOf { it.restDuration() }
        return totalWorkout * 1000 to totalRest * 1000 // milliseconds
    }

    fun getNowSetInfo(): Pair<ExercisePlan, ExerciseSet>? {
        val exercise = currentExercise ?: return null
        val set = currentSet ?: return null
        return exercise to set
    }

    fun getNextSetInfo(): Pair<ExercisePlan, ExerciseSet>? {
        if (isFinished) return null

        var exIdx = currentExerciseIndex
        var setIdx = currentSetIndex

        // "지금 세트"가 방금 끝난 세트고, 휴식이 그 뒤에 시작되는 상태이므로
        // NEXT는 (현재 세트 인덱스 + 1) 기준
        setIdx += 1

        val currentSets = plans[exIdx].sets
        if (setIdx >= currentSets.size) {
            // 다음 운동으로 넘어감
            exIdx += 1
            if (exIdx >= plans.size) return null   // 다음 세트 없음 (전체 루틴 끝)
            setIdx = 0
        }

        val nextExercise = plans[exIdx]
        val nextSet = nextExercise.sets.getOrNull(setIdx) ?: return null
        return nextExercise to nextSet
    }

}

data class SetTiming(
    val recordId: Int,
    var workoutStart: Long = 0L,
    var workoutEnd: Long = 0L,
    var restStart: Long = 0L,
    var restEnd: Long = 0L
) {
    fun workoutDuration(): Long = ((workoutEnd - workoutStart) / 1000).coerceAtLeast(0)
    fun restDuration(): Long = ((restEnd - restStart) / 1000).coerceAtLeast(0)
}