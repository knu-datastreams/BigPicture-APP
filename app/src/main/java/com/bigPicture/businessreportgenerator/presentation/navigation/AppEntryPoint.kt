package com.bigPicture.businessreportgenerator.presentation.navigation

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bigPicture.businessreportgenerator.ErrorScreen
import com.bigPicture.businessreportgenerator.data.local.StockViewModel
import com.bigPicture.businessreportgenerator.data.remote.ApiStatus
import com.bigPicture.businessreportgenerator.data.remote.ApiViewModel
import com.bigPicture.businessreportgenerator.data.remote.api.FCMToken
import com.bigPicture.businessreportgenerator.data.remote.dto.ReportRequest
import com.bigPicture.businessreportgenerator.presentation.features.analyst.AnalystViewmodel
import com.bigPicture.businessreportgenerator.presentation.features.news.NewsViewModel
import com.bigPicture.businessreportgenerator.presentation.onboarding.OnboardingScreen
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * 앱 진입점 화면 - 온보딩 또는 메인 화면 표시
 */
@Composable
fun AppEntryPoint() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }

    val newsViewModel: NewsViewModel = viewModel()
    val apiViewModel: ApiViewModel = viewModel()
    val apiState by apiViewModel.state.collectAsState()

    val stockViewModel: StockViewModel = koinViewModel()
    val analystViewModel: AnalystViewmodel = koinViewModel()

    val prefs = context.getSharedPreferences("user_prefs", 0)

    LaunchedEffect(Unit) {
        onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

        apiViewModel.updateApiMessage("서버와의 연결을 확인하는 중...")
        val connected = withContext(Dispatchers.IO) {
            apiViewModel.sendPing()
        }
        Log.d("bigPicture", "connected : $connected")

        if (connected) {
            apiViewModel.updateApiMessage("사용자 정보를 확인하는 중...")
            Log.d("bigPicture", "onboardingCompleted : $onboardingCompleted")

            if (onboardingCompleted == true) {
                withContext(Dispatchers.IO) {
                    val uuid = prefs.getString("uuid", null) ?: "-1"
                    var fcm = getFcmTokenSuspend(prefs)
                    apiViewModel.updateApiMessage("토큰을 전송하는 중...")
                    Log.d("bigPicture", "uuid : $uuid")
                    apiViewModel.sendToken(
                        FCMToken(
                            uuid = uuid,
                            fcmToken = fcm
                        )
                    )
                }

                launch(Dispatchers.IO) {
                    val today = LocalDate.now()
                    val todayWeek = today.dayOfWeek.value - 1

                    val weekOfYear = today.get(WeekFields.ISO.weekOfWeekBasedYear())
                    val year = today.year
                    val reportKey = "report_requested_${year}_$weekOfYear"

                    val alreadyRequested = prefs.getBoolean(reportKey, false)

                    val reportDayList = prefs.getString("report_days", null)
                        ?.split(",")?.mapNotNull { it.toInt() }
                    Log.d("bigPicture", "reportList : $reportDayList, today : $todayWeek")
                    Log.d("bigPicture", "alreadyRequested : $alreadyRequested")

                    if (reportDayList?.contains(todayWeek) == true && alreadyRequested == false ) {
                        Log.d(
                            "bigPicture",
                            "today is in reportList : ${reportDayList.contains(todayWeek)}"
                        )
                        val date = today.toString()
                        val riskTolerance = prefs.getString("risk_tolerance", "") ?: ""
                        val reportComplexity = prefs.getString("report_complexity", "") ?: ""
                        val interests =
                            prefs.getStringSet("interests", null)?.toList() ?: emptyList()

                        stockViewModel.getLatestStocksGroupByDate().firstOrNull()?.let { stocks ->
                            val filtered = stocks.filter { it.date != date }

                            val deferred = filtered.map { stock ->
                                val request = ReportRequest(
                                    reportType = "stock",
                                    stockName = stock.stockName,
                                    riskTolerance = riskTolerance,
                                    reportDifficultyLevel = reportComplexity,
                                    interestAreas = interests
                                )
                                async {
                                    analystViewModel.requestReport(request)
                                    stockViewModel.updateStockDate(stock.stockName, date)
                                }
                            }
                            deferred.awaitAll()
                            prefs.edit() { putBoolean(reportKey, true) }
                        }
                    }
                }
            }

            apiViewModel.updateApiMessage("환율 불러오는 중...")
            withContext(Dispatchers.IO) {
                newsViewModel.fetchExchange()
            }
            apiViewModel.updateApiMessage("로딩 완료")
            delay(500)
            apiViewModel.updateApiStatus(ApiStatus.DONE)
        } else {
            apiViewModel.updateApiStatus(ApiStatus.ERROR)
        }
    }

    when (apiState.isLoading) {
        ApiStatus.LOADING -> AnimatedVisibility(visible = true) {
            LoadingScreen(message = apiState.message)
        }
        ApiStatus.ERROR -> ErrorScreen()
        ApiStatus.DONE -> {
            if (onboardingCompleted == false) {
                OnboardingScreen(
                    onComplete = { reportRequest ->
                        coroutineScope.launch(Dispatchers.IO) {
                            apiViewModel.sendToken(
                                FCMToken(
                                    uuid = prefs.getString("uuid", "-1") ?: "-1",
                                    fcmToken = getFcmTokenSuspend(prefs)
                                )
                            )
                            analystViewModel.requestReport(reportRequest)
                        }
                        prefs.edit { putBoolean("onboarding_completed", true) }
                        onboardingCompleted = true
                    }
                )
            } else {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun getFcmTokenSuspend(prefs: SharedPreferences): String =
    suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    prefs.edit() { putString("fcm_token", token) }
                    Log.d("bigPicture", "fcm token : $token")
                    continuation.resume(token, onCancellation = null)
                } else {
                    Log.d("bigPicture", "FCM 토큰 가져오기 실패: ${task.exception?.message}")
                    continuation.resume("-1", onCancellation = null)
                }
            }
    }
