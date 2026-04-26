package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitme.data.entities.Visit
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {

    @Insert
    suspend fun insertVisit(visit: Visit): Long

    @Update
    suspend fun updateVisit(visit: Visit)

    @Delete
    suspend fun deleteVisit(visit: Visit)

    @Query("SELECT * FROM visits ORDER BY date DESC")
    fun getAllVisits(): Flow<List<Visit>>

    @Query("SELECT * FROM visits WHERE id = :visitId")
    suspend fun getVisitById(visitId: Int): Visit?
}
