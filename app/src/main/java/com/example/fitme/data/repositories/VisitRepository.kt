package com.example.fitme.data.repositories

import com.example.fitme.data.dao.VisitDao
import com.example.fitme.data.entities.Visit
import kotlinx.coroutines.flow.Flow

class VisitRepository(private val visitDao: VisitDao) {

    //всегда хранит все посещения причем по убыванию даты. Нужно для отображения списка посещений.
    val allVisits: Flow<List<Visit>> = visitDao.getAllVisits()

    suspend fun insertVisit(visit: Visit): Long {
        return visitDao.insertVisit(visit)
    }

    suspend fun updateVisit(visit: Visit) {
        visitDao.updateVisit(visit)
    }

    suspend fun deleteVisit(visit: Visit) {
        visitDao.deleteVisit(visit)
    }

    suspend fun getVisitById(visitId: Int): Visit? {
        return visitDao.getVisitById(visitId)
    }
}
