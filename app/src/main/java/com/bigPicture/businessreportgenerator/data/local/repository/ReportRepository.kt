package com.bigPicture.businessreportgenerator.data.local.repository

import com.bigPicture.businessreportgenerator.data.local.dao.ReportDao
import com.bigPicture.businessreportgenerator.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val dao: ReportDao) {

    fun observeReports(): Flow<List<ReportEntity>> = dao.observeReports()

    suspend fun insertReport(entity: ReportEntity) =          // suspend 함수
        dao.insertReport(entity)
}
