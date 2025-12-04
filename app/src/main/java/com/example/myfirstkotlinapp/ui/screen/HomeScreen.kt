package com.example.myfirstkotlinapp.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.HomeScreenBinding
import com.example.myfirstkotlinapp.network.RetrofitClient
import com.example.myfirstkotlinapp.ui.model.ExercisePlan
import com.example.myfirstkotlinapp.ui.model.ExerciseSet
import com.example.myfirstkotlinapp.ui.model.ExerciseRecordDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import java.util.Calendar


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onStartWorkout: (List<ExercisePlan>, List<Int>) -> Unit,
    onNavigateEmpty: () -> Unit
) {
    val context = LocalContext.current
    val todayDate by remember { mutableStateOf(Date()) }

    // 백엔드에서 가져온 데이터 보관용 상태
    var plans by remember { mutableStateOf<List<ExercisePlan>>(emptyList()) }
    var recordIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 1) HomeScreen에 들어오자마자 운동 기록 불러오기
    LaunchedEffect(Unit) {
        try {
            // 1-1. 토큰 가져오기
            val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val token = sharedPref.getString("access_token", null)

            if (token.isNullOrBlank()) {
                errorMessage = "로그인이 필요합니다."
                isLoading = false
                return@LaunchedEffect
            }

            // 1-2. 인증된 Retrofit 클라이언트 생성
            val authedApi = RetrofitClient.createAuthorizedClient(token)

            // 1-3. 현재 유저 정보 호출 (suspend 함수라고 가정)
            val userInfo = withContext(Dispatchers.IO) {
                authedApi.getCurrentUser()
            }
            val userId = userInfo.id

            // 1-4. 오늘 날짜 구해서 문자열로 포맷
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val today = sdf.format(Date())
            // 예: "2025-11-20"

            // 1-5. 오늘 날짜의 운동 기록 가져오기
            val dtoList: List<ExerciseRecordDto> = withContext(Dispatchers.IO) {
                authedApi.getExerciseRecord(
                    userId = userId,
                    date = today
                )
            }

            if (dtoList.isEmpty()) {
                onNavigateEmpty()
                return@LaunchedEffect
            }

            // 1-6. DTO → ExercisePlan + recordIds 변환
            val (mappedPlans, mappedRecordIds) = mapRecordsToPlans(dtoList)
            plans = mappedPlans
            recordIds = mappedRecordIds
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "운동 목록을 불러오지 못했습니다."
            isLoading = false
        }
    }

    // 2) XML ↔ Compose 바인딩
    AndroidViewBinding(
        modifier = modifier,
        factory = HomeScreenBinding::inflate
    ) {
        // 여기서 this == HomeScreenBinding

        // 오류가 있으면 간단하게 Toast (원하면 Text로 화면에 표시해도 됨)
        errorMessage?.let { msg ->
            Toast.makeText(root.context, msg, Toast.LENGTH_SHORT).show()
        }

        // 로딩 상태면 나중에 ProgressBar 등을 연결해도 됨
        // ex) progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

        // 달력 바인딩 (오늘 기준)
        bindCalendar(todayDate)

        // 예: tvExerciseTitle1.text = exercisePlans.getOrNull(0)?.name ?: "운동 1"
        bindPlansToCards(plans)
        // 3) 재생 버튼 클릭 → 이미 로딩된 데이터로 onStartWorkout 호출
        btnPlayRoutine.setOnClickListener {
            if (plans.isEmpty() || recordIds.isEmpty()) {
                Toast.makeText(root.context, "운동 데이터를 아직 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onStartWorkout(plans, recordIds)
        }
    }
}

/**
 * 서버에서 받은 ExerciseRecordDto 리스트를
 * 화면/세션에서 사용할 ExercisePlan 리스트와 recordIds 리스트로 변환.
 *
 * - 같은 exerciseId 끼리 하나의 ExercisePlan으로 묶는다.
 * - recordIds는 WorkoutSessionManager 에 넘겨서 세트별 PATCH 등에 사용.
 */
fun mapRecordsToPlans(
    records: List<ExerciseRecordDto>
): Pair<List<ExercisePlan>, List<Int>> {

    // exerciseId 기준으로 그룹핑
    val grouped: Map<Int, List<ExerciseRecordDto>> = records.groupBy { it.exerciseId }
    // groupBy는 LinkedHashMap을 쓰기 때문에 "처음 등장한 순서"가 유지됨

    val plans: List<ExercisePlan> = grouped.map { (exerciseId, recordList) ->
        ExercisePlan(
            id = exerciseId,
            name = recordList.firstOrNull()?.exerciseName ?: "알 수 없는 운동",
            sets = recordList.map { dto ->
                ExerciseSet(
                    weight = dto.weight.toInt(),   // 필요하면 Float로 바꿔도 됨
                    reps = dto.reps,
                    isCompleted = dto.isCompleted
                )
            }
        )
    }

    // recordIds도 같은 grouped 순서를 그대로 따라가도록 flatMap
    val recordIds: List<Int> = grouped.values
        .flatMap { list -> list.map { it.id } }

    return plans to recordIds
}

private fun setProgressBar(
    container: LinearLayout,
    completedCount: Int,
    totalCount: Int
) {
    if (totalCount <= 0) {
        // 세트가 없는 경우 모두 회색
        for (i in 0 until container.childCount) {
            val segment = container.getChildAt(i)
            segment.setBackgroundColor(0xFFE9E9E9.toInt()) // #E9E9E9
        }
        return
    }

    val segmentCount = container.childCount
    val ratio = completedCount.toFloat() / totalCount.toFloat()
    val filledSegments = ((ratio * segmentCount).toInt()).coerceIn(0, segmentCount)

    for (i in 0 until segmentCount) {
        val segment = container.getChildAt(i)
        if (i < filledSegments) {
            segment.setBackgroundColor(0xFF2260FF.toInt()) // 파란색 #2260FF
        } else {
            segment.setBackgroundColor(0xFFE9E9E9.toInt()) // 연회색 #E9E9E9
        }
    }
}

fun HomeScreenBinding.bindPlansToCards(plans: List<ExercisePlan>) {

    val cards = listOf(
        Triple(routineItem1, tvExerciseTitle1, tvSetCount1) to
                Pair(tvExerciseSetInfo1, progressContainer1),

        Triple(routineItem2, tvExerciseTitle2, tvSetCount2) to
                Pair(tvExerciseSetInfo2, progressContainer2),

        Triple(routineItem3, tvExerciseTitle3, tvSetCount3) to
                Pair(tvExerciseSetInfo3, progressContainer3),

        Triple(routineItem4, tvExerciseTitle4, tvSetCount4) to
                Pair(tvExerciseSetInfo4, progressContainer4),
    )

    cards.forEachIndexed { index, entry ->
        val plan = plans.getOrNull(index)

        val (containerTriple, infoPair) = entry
        val (itemView, titleView, setCountView) = containerTriple
        val (setInfoView, progressContainer) = infoPair

        if (plan == null) {
            itemView.visibility = View.GONE
        } else {
            itemView.visibility = View.VISIBLE

            // 운동 이름
            titleView.text = plan.name

            // 완료 세트 수
            val completedCount = plan.sets.count { it.isCompleted }

            // "완료세트 / 전체세트"
            setCountView.text = "$completedCount / ${plan.sets.size}"

            // "15회 X N세트" (첫 세트 기준)
            val firstSet = plan.sets.firstOrNull()
            setInfoView.text = if (firstSet != null) {
                "${firstSet.reps}회 X ${plan.sets.size}세트"
            } else {
                "${plan.sets.size}세트"
            }

            // 🔥 진행도 바 색칠
            setProgressBar(
                container = progressContainer,
                completedCount = completedCount,
                totalCount = plan.sets.size
            )
        }
    }
}

fun HomeScreenBinding.bindCalendar(today: Date) {
    val tz = TimeZone.getTimeZone("Asia/Seoul")

    // 오늘 날짜 기준 Calendar
    val todayCal = Calendar.getInstance(tz).apply {
        time = today
        // 필요하면 firstDayOfWeek 설정도 가능
        // firstDayOfWeek = Calendar.MONDAY
    }

    // 오늘 기준으로 앞뒤 3일 = 총 7일 보여주기 (원하는대로 조정 가능)
    val startCal = Calendar.getInstance(tz).apply {
        time = today
        add(Calendar.DAY_OF_MONTH, -3)
    }

    // 7칸(앞뒤 3일 + 오늘) 컨테이너와 텍스트 뷰 리스트
    val dayContainers = listOf(
        dayItem1, dayItem2, dayItem3, dayItem4, dayItem5, dayItem6, dayItem7
    )

    val dayTexts = listOf(
        tvDay1, tvDay2, tvDay3, tvDay4, tvDay5, tvDay6, tvDay7
    )

    dayContainers.zip(dayTexts).forEachIndexed { index, (container, textView) ->
        val cal = Calendar.getInstance(tz).apply {
            time = startCal.time
            add(Calendar.DAY_OF_MONTH, index)
        }

        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        textView.text = dayOfMonth.toString()

        val isToday =
            cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH)

        if (isToday) {
            // 오늘: 까만 배경 + 흰 글씨 (기존 15번 스타일)
            container.setBackgroundColor(0xFF303437.toInt())   // #303437
            textView.setTextColor(0xFFF2F4F5.toInt())          // #F2F4F5
        } else {
            // 나머지: 투명 배경 + 회색 글씨
            container.setBackgroundColor(0x00000000)           // 투명
            textView.setTextColor(0xFF979C9E.toInt())          // #979C9E
        }
    }
}
