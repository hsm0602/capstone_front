package com.example.myfirstkotlinapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.example.myfirstkotlinapp.databinding.HomeEmptyScreenBinding
import java.util.Date
import java.util.TimeZone
import java.util.Calendar
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.myfirstkotlinapp.network.RetrofitClient

@Composable
fun HomeEmptyScreen(
    modifier: Modifier = Modifier,
    onCreateRoutineClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    // 오늘 날짜 (캘린더에 쓰고 싶으면)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val todayDate by remember { mutableStateOf(Date()) }

    AndroidViewBinding(
        modifier = modifier,
        factory = HomeEmptyScreenBinding::inflate
    ) {
        // 필요하면 여기서 캘린더 바인딩 (동적 날짜)
        bindCalendar(todayDate)

        // 프로필 버튼 클릭
//        btnProfile.setOnClickListener {
//            onProfileClick()
//        }

        // "루틴 생성하기" 카드 클릭
        createRoutineCard.setOnClickListener {

            // ⭐ 이 안에서 백엔드 POST 실행하려면 launch 필요함
            coroutineScope.launch {

                try {
                    // 1. 토큰
                    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    val token = sharedPref.getString("access_token", null)

                    if (token.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val authedApi = RetrofitClient.createAuthorizedClient(token)

                    // 2. 유저 정보
                    val userInfo = withContext(Dispatchers.IO) {
                        authedApi.getCurrentUser()
                    }
                    val userId = userInfo.id

                    // 3. 날짜 포맷
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val today = sdf.format(Date())

                    // 4. 플랜 생성 POST 요청
                    val response = withContext(Dispatchers.IO) {
                        authedApi.generatePlan(
                            userId = userId,
                            date = today
                        )
                    }

                    if (response.isSuccessful) {
                        // ⭐ 5. 성공 후 다음 화면으로 이동
                        withContext(Dispatchers.Main) {
                            onCreateRoutineClick()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "루틴 생성에 실패했습니다. (${response.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "루틴 생성 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

fun HomeEmptyScreenBinding.bindCalendar(today: Date) {
    val tz = TimeZone.getTimeZone("Asia/Seoul")

    val todayCal = Calendar.getInstance(tz).apply {
        time = today
    }

    val startCal = Calendar.getInstance(tz).apply {
        time = today
        add(Calendar.DAY_OF_MONTH, -3) // 앞뒤 3일씩 보여주는 예시
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
            container.setBackgroundColor(0xFF303437.toInt())   // 오늘: 검정 배경
            textView.setTextColor(0xFFF2F4F5.toInt())          // 흰 글씨
        } else {
            container.setBackgroundColor(0x00000000)           // 투명
            textView.setTextColor(0xFF979C9E.toInt())          // 회색
        }
    }
}