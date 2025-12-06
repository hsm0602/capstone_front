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
import android.widget.LinearLayout
import java.util.Calendar
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onStartWorkout: (List<ExercisePlan>, List<Int>) -> Unit
) {
    val context = LocalContext.current
    val todayDate by remember { mutableStateOf(Date()) }

    var plans by remember { mutableStateOf<List<ExercisePlan>>(emptyList()) }
    var recordIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // 운동 목록 생성 함수.
    fun reloadPlans() {
        scope.launch {
            try {
                isLoading = true

                val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val token = sharedPref.getString("access_token", null)

                if (token.isNullOrBlank()) {
                    errorMessage = "로그인이 필요합니다."
                    isLoading = false
                    plans = emptyList()
                    recordIds = emptyList()
                    return@launch
                }

                val authedApi = RetrofitClient.createAuthorizedClient(token)

                val userInfo = withContext(Dispatchers.IO) {
                    authedApi.getCurrentUser()
                }
                val userId = userInfo.id

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                val today = sdf.format(Date())

                val dtoList: List<ExerciseRecordDto> = withContext(Dispatchers.IO) {
                    authedApi.getExerciseRecord(
                        userId = userId,
                        date = today
                    )
                }

                val (mappedPlans, mappedRecordIds) = mapRecordsToPlans(dtoList)
                plans = mappedPlans
                recordIds = mappedRecordIds
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "운동 목록을 불러오지 못했습니다."
                isLoading = false
                plans = emptyList()
                recordIds = emptyList()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reloadPlans()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        reloadPlans()
    }

    AndroidViewBinding(
        modifier = modifier,
        factory = HomeScreenBinding::inflate
    ) {

        errorMessage?.let { msg ->
            Toast.makeText(root.context, msg, Toast.LENGTH_SHORT).show()
        }

        // 마이 페이지.
        btnProfile.setOnClickListener {
            val intent = Intent(root.context, MyPageActivity::class.java)
            root.context.startActivity(intent)
        }

        // 달력 바인딩 (오늘 기준)
        bindCalendar(todayDate)

        if (plans.isEmpty()) {
            // 루틴 없을 때.
            routineCardContainer.visibility = View.GONE
            createRoutineCard.visibility = View.VISIBLE

            createRoutineCard.setOnClickListener {
                val intent = Intent(root.context, CreateRoutineActivity::class.java)
                root.context.startActivity(intent)
            }
        } else {
            // 루틴 있을 때.
            routineCardContainer.visibility = View.VISIBLE
            createRoutineCard.visibility = View.GONE

            // 카드 내용 채우기.
            bindPlansToCards(plans)

            btnPlayRoutine.setOnClickListener {
                if (plans.isEmpty() || recordIds.isEmpty()) {
                    Toast.makeText(root.context, "운동 데이터를 아직 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                onStartWorkout(plans, recordIds)
            }
        }
    }
}

// DB 운동 목록 레코드를 plan과 id로 분리하는 함수.
fun mapRecordsToPlans(
    records: List<ExerciseRecordDto>
): Pair<List<ExercisePlan>, List<Int>> {

    // exerciseId 기준으로 그룹핑
    val grouped: Map<Int, List<ExerciseRecordDto>> = records.groupBy { it.exerciseId }

    val plans: List<ExercisePlan> = grouped.map { (exerciseId, recordList) ->
        ExercisePlan(
            id = exerciseId,
            name = recordList.firstOrNull()?.exerciseName ?: "알 수 없는 운동",
            sets = recordList.map { dto ->
                ExerciseSet(
                    weight = dto.weight.toInt(),
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

// 카드 내에서 운동 진행도를 채우는 함수.
private fun setProgressBar(
    container: LinearLayout,
    completedCount: Int,
    totalCount: Int
) {
    if (totalCount <= 0) {
        for (i in 0 until container.childCount) {
            val segment = container.getChildAt(i)
            segment.setBackgroundColor(0xFFE9E9E9.toInt()) // 연회색
        }
        return
    }

    val segmentCount = container.childCount
    val ratio = completedCount.toFloat() / totalCount.toFloat()
    val filledSegments = ((ratio * segmentCount).toInt()).coerceIn(0, segmentCount)

    for (i in 0 until segmentCount) {
        val segment = container.getChildAt(i)
        if (i < filledSegments) {
            segment.setBackgroundColor(0xFF2260FF.toInt()) // 파란색
        } else {
            segment.setBackgroundColor(0xFFE9E9E9.toInt()) // 연회색
        }
    }
}

// 카드 내에 운동 목록을 넣는 함수.
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

            // "N회 X N세트"
            val firstSet = plan.sets.firstOrNull()
            setInfoView.text = if (firstSet != null) {
                "${firstSet.reps}회 X ${plan.sets.size}세트"
            } else {
                "${plan.sets.size}세트"
            }

            // 진행도 바 색칠
            setProgressBar(
                container = progressContainer,
                completedCount = completedCount,
                totalCount = plan.sets.size
            )
        }
    }
}

// 오늘 날짜를 기준으로 주간 달력을 만드는 함수.
fun HomeScreenBinding.bindCalendar(today: Date) {
    val tz = TimeZone.getTimeZone("Asia/Seoul")

    val todayCal = Calendar.getInstance(tz).apply {
        time = today
    }

    val startCal = Calendar.getInstance(tz).apply {
        time = today
        add(Calendar.DAY_OF_MONTH, -3)
    }

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
            container.setBackgroundColor(0xFF303437.toInt())
            textView.setTextColor(0xFFF2F4F5.toInt())
        } else {
            container.setBackgroundColor(0x00000000)
            textView.setTextColor(0xFF979C9E.toInt())
        }
    }
}

