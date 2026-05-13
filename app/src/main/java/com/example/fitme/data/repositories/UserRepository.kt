package com.example.fitme.data.repositories

import com.example.fitme.data.dao.UserDao
import com.example.fitme.data.entities.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun createUser(name: String, age: Int, weight: Float, height: Float) {
        userDao.insertOrIgnore(
            User(
                id = CURRENT_USER_ID,
                name = name,
                age = age,
                weight = weight,
                height = height,
            )
        )
    }

    suspend fun getUser(): User? = userDao.getUserById(CURRENT_USER_ID)

    suspend fun getCurrentPlanId(): Int? = userDao.getUserById(CURRENT_USER_ID)?.activePlan

    suspend fun setName(name: String) = userDao.setName(CURRENT_USER_ID, name)
    suspend fun setAge(age: Int) = userDao.setAge(CURRENT_USER_ID, age)
    suspend fun setWeight(weight: Float) = userDao.setWeight(CURRENT_USER_ID, weight)
    suspend fun setHeight(height: Float) = userDao.setHeight(CURRENT_USER_ID, height)

    suspend fun setActivePlan(planId: Int?) = userDao.setActivePlan(CURRENT_USER_ID, planId)

    fun observeActivePlan(): Flow<Int?> = userDao.observeActivePlan(CURRENT_USER_ID)

    companion object {
        const val CURRENT_USER_ID = 1
    }
}
