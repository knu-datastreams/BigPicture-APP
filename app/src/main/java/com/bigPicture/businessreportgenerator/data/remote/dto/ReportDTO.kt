package com.bigPicture.businessreportgenerator.data.remote.dto

import android.util.Log
import com.bigPicture.businessreportgenerator.data.domain.GraphData
import com.bigPicture.businessreportgenerator.data.local.entity.ReportEntity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

// 기존 Data 클래스에 새로운 필드만 추가 (nullable로 설정하여 호환성 유지)
data class Data(
    val title: String,
    val summaryReport: String,
    val economyReport: String,
    val stockReport: String,
    val exchangesString: String? = null,      // 새로운 필드들을 nullable로 추가
    val koreaInterestsString: String? = null,
    val usInterestsString: String? = null,
    val stockInfosString: String? = null
)

data class ReportRequest(
    val reportType: String?,
    val stockName: String?,
    val riskTolerance: String,
    val reportDifficultyLevel: String,
    val interestAreas: List<String>
)

// JSON 파싱을 위한 데이터 클래스들
data class ExchangeData(
    val exchangeDate: String?,
    val exchangeRate: Float?
)

data class InterestData(
    val interestDate: String?,
    val interestRate: Float?
)

class ReportResponse {
    @SerializedName("code") private var code: Int? = null
    @SerializedName("status") private var status: String? = null
    @SerializedName("message") private var message: String? = null
    @SerializedName("data") private var data: Data? = null

    override fun toString(): String {
        return "ReportResponse(code=$code, status=$status, message=$message, data=$data)"
    }

    fun toDomain(): ReportEntity {
        val reportType = determineReportType()

        // 레포트 타입에 따라 그래프 데이터 생성
        val graphData = if (hasNewFields()) {
            generateGraphDataFromNewFields(data, reportType)
        } else {
            emptyList()
        }

        val gson = Gson()

        Log.d("BigPicture", "레포트 타입: $reportType")
        Log.d("BigPicture", "생성된 그래프 데이터: $graphData")

        return ReportEntity(
            title = data?.title ?: "",
            content = buildDetailedContent(reportType),
            summary = data?.summaryReport ?: "",
            date = System.currentTimeMillis(),
            type = reportType,
            graphDataJson = if (graphData.isNotEmpty()) gson.toJson(graphData) else null
        )
    }

    // 레포트 타입 결정
    private fun determineReportType(): String {
        return when {
            !data?.economyReport.isNullOrBlank() && data?.stockReport.isNullOrBlank() -> "경제 분석"
            !data?.stockReport.isNullOrBlank() && data?.economyReport.isNullOrBlank() -> "주식 분석"
            !data?.economyReport.isNullOrBlank() && !data?.stockReport.isNullOrBlank() -> "통합 분석"
            else -> "AI 분석"
        }
    }

    // 새로운 필드들이 있는지 확인
    private fun hasNewFields(): Boolean {
        return data?.let {
            !it.exchangesString.isNullOrBlank() ||
                    !it.koreaInterestsString.isNullOrBlank() ||
                    !it.usInterestsString.isNullOrBlank() ||
                    !it.stockInfosString.isNullOrBlank()
        } ?: false
    }

    private fun buildDetailedContent(reportType: String): String {
        return when (reportType) {
            "경제 분석" -> {
                buildString {
                    appendLine("## 📊 경제 동향 분석")
                    appendLine()
                    appendLine(data?.economyReport ?: "")
                }
            }
            "주식 분석" -> {
                buildString {
                    appendLine("## 📈 주식 시장 분석")
                    appendLine()
                    appendLine(data?.stockReport ?: "")
                }
            }
            "통합 분석" -> {
                buildString {
                    appendLine("## 📊 경제 동향 분석")
                    appendLine()
                    appendLine(data?.economyReport ?: "")
                    appendLine()
                    appendLine("## 📈 주식 시장 분석")
                    appendLine()
                    appendLine(data?.stockReport ?: "")
                }
            }
            else -> {
                // 기본 처리
                buildString {
                    if (!data?.economyReport.isNullOrBlank()) {
                        appendLine("## 📊 경제 동향 분석")
                        appendLine()
                        appendLine(data?.economyReport ?: "")
                        appendLine()
                    }

                    if (!data?.stockReport.isNullOrBlank()) {
                        appendLine("## 📈 주식 시장 분석")
                        appendLine()
                        appendLine(data?.stockReport ?: "")
                    }
                }
            }
        }
    }

    private fun generateGraphDataFromNewFields(data: Data?, reportType: String): List<GraphData> {
        if (data == null) return emptyList()

        val graphList = mutableListOf<GraphData>()

        // 레포트 타입에 따라 그래프 우선순위 조정
        when (reportType) {
            "경제 분석" -> {
                // 경제 레포트: 환율, 금리 우선
                addExchangeChart(data, graphList)
                addKoreaInterestChart(data, graphList)
                addUSInterestChart(data, graphList)
                addStockChart(data, graphList)  // 마지막에 추가
            }
            "주식 분석" -> {
                // 주식 레포트: 주식 데이터 우선
                addStockChart(data, graphList)
                addExchangeChart(data, graphList)
                addKoreaInterestChart(data, graphList)
                addUSInterestChart(data, graphList)
            }
            else -> {
                // 기본: 모든 차트 추가
                addExchangeChart(data, graphList)
                addKoreaInterestChart(data, graphList)
                addUSInterestChart(data, graphList)
                addStockChart(data, graphList)
            }
        }

        Log.d("BigPicture", "생성된 그래프 데이터 개수: ${graphList.size}")
        return graphList
    }

    // 개별 차트 추가 함수들
    private fun addExchangeChart(data: Data, graphList: MutableList<GraphData>) {
        data.exchangesString?.let { exchangesString ->
            if (exchangesString.isNotBlank()) {
                val exchangeData = parseFinancialString(exchangesString, "환율")
                if (exchangeData.isNotEmpty()) {
                    graphList.add(
                        GraphData(
                            type = "LINE_CHART",
                            title = "원달러 환율 추이",
                            description = "최근 환율 변동 현황",
                            data = exchangeData
                        )
                    )
                }
            }
        }
    }

    private fun addKoreaInterestChart(data: Data, graphList: MutableList<GraphData>) {
        data.koreaInterestsString?.let { koreaInterestsString ->
            if (koreaInterestsString.isNotBlank()) {
                val krRateData = parseFinancialString(koreaInterestsString, "한국금리")
                if (krRateData.isNotEmpty()) {
                    graphList.add(
                        GraphData(
                            type = "BAR_CHART",
                            title = "한국 기준금리",
                            description = "한국은행 기준금리 변동 추이",
                            data = krRateData
                        )
                    )
                }
            }
        }
    }

    private fun addUSInterestChart(data: Data, graphList: MutableList<GraphData>) {
        data.usInterestsString?.let { usInterestsString ->
            if (usInterestsString.isNotBlank()) {
                val usRateData = parseFinancialString(usInterestsString, "미국금리")
                if (usRateData.isNotEmpty()) {
                    graphList.add(
                        GraphData(
                            type = "BAR_CHART",
                            title = "미국 연방금리",
                            description = "연준 기준금리 변동 추이",
                            data = usRateData
                        )
                    )
                }
            }
        }
    }

    private fun addStockChart(data: Data, graphList: MutableList<GraphData>) {
        data.stockInfosString?.let { stockInfosString ->
            if (stockInfosString.isNotBlank()) {
                val stockData = parseFinancialString(stockInfosString, "주식")
                if (stockData.isNotEmpty()) {
                    graphList.add(
                        GraphData(
                            type = "LINE_CHART",
                            title = "주가 동향",
                            description = "주요 종목 가격 변동 추이",
                            data = stockData
                        )
                    )
                }
            }
        }
    }

    private fun parseFinancialString(dataString: String, dataType: String): Map<String, Float> {
        val result = mutableMapOf<String, Float>()

        try {
            Log.d("BigPicture", "$dataType 원본 데이터: $dataString")

            if (dataString.trim().startsWith("[")) {
                val gson = Gson()

                when (dataType) {
                    "환율" -> {
                        // 환율 데이터 파싱: [{"exchangeDate":"2025-04-27","exchangeRate":1430.6}...]
                        val listType = object : TypeToken<List<ExchangeData>>() {}.type
                        val exchangeList = gson.fromJson<List<ExchangeData>>(dataString, listType)

                        exchangeList?.forEach { exchange ->
                            val dateKey = exchange.exchangeDate?.substring(5) ?: "날짜"  // "04-27" 형태로 축약
                            result[dateKey] = exchange.exchangeRate ?: 0f
                        }
                    }
                    "한국금리" -> {
                        // 한국 금리 데이터 파싱: [{"interestDate":"2024-05-20","interestRate":3.5}...]
                        val listType = object : TypeToken<List<InterestData>>() {}.type
                        val interestList = gson.fromJson<List<InterestData>>(dataString, listType)

                        interestList?.forEach { interest ->
                            val dateKey = interest.interestDate?.substring(5) ?: "날짜"  // "05-20" 형태로 축약
                            result[dateKey] = interest.interestRate ?: 0f
                        }
                    }
                    "미국금리" -> {
                        // 미국 금리 데이터 파싱: [{"interestDate":"2025-04-01","interestRate":4.33}...]
                        val listType = object : TypeToken<List<InterestData>>() {}.type
                        val interestList = gson.fromJson<List<InterestData>>(dataString, listType)

                        interestList?.forEach { interest ->
                            val dateKey = interest.interestDate?.substring(5) ?: "날짜"  // "04-01" 형태로 축약
                            result[dateKey] = interest.interestRate ?: 0f
                        }
                    }
                    "주식" -> {
                        // 주식 데이터 파싱 (형태에 따라 다르게 처리)
                        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val stockList = gson.fromJson<List<Map<String, Any>>>(dataString, listType)

                        stockList?.forEachIndexed { index, item ->
                            val key = item["date"]?.toString()?.substring(5) ?: "주식${index + 1}"
                            val value = extractNumericValue(item["price"] ?: item["value"] ?: 0)
                            if (value > 0) result[key] = value
                        }
                    }
                }
            } else {
                // 다른 형태의 데이터 처리 (기존 로직 유지)
                when {
                    dataString.contains(":") -> {
                        dataString.split(",").forEach { pair ->
                            val parts = pair.split(":")
                            if (parts.size == 2) {
                                val key = parts[0].trim()
                                val value = extractNumericValue(parts[1].trim())
                                if (value > 0) result[key] = value
                            }
                        }
                    }
                    dataString.contains(",") -> {
                        dataString.split(",").forEachIndexed { index, valueStr ->
                            val value = extractNumericValue(valueStr.trim())
                            if (value > 0) result["${dataType}${index + 1}"] = value
                        }
                    }
                    else -> {
                        val value = extractNumericValue(dataString)
                        if (value > 0) result[dataType] = value
                    }
                }
            }

            Log.d("BigPicture", "$dataType 파싱 결과: $result")

        } catch (e: Exception) {
            Log.e("BigPicture", "$dataType 데이터 파싱 오류: ${e.message}")
            // 파싱 실패시 빈 결과 반환 (에러 시에는 차트를 표시하지 않음)
        }

        return result
    }

    private fun extractNumericValue(value: Any): Float {
        return when (value) {
            is Number -> value.toFloat()
            is String -> {
                // 문자열에서 숫자 추출 (쉼표, % 등 제거)
                value.replace(",", "")
                    .replace("%", "")
                    .replace("원", "")
                    .replace("$", "")
                    .toFloatOrNull() ?: 0f
            }
            else -> 0f
        }
    }
}