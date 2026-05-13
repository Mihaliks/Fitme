package com.example.fitme.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitme.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(user: User): Long

    @Query("UPDATE users SET name = :name WHERE id = :userId")
    suspend fun setName(userId: Int, name: String)

    @Query("UPDATE users SET weight = :weight WHERE id = :userId")
    suspend fun setWeight(userId: Int, weight: Float)

    @Query("UPDATE users SET age = :age WHERE id = :userId")
    suspend fun setAge(userId: Int, age: Int)

    @Query("UPDATE users SET height = :height WHERE id = :userId")
    suspend fun setHeight(userId: Int, height: Float)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users ORDER BY id")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT active_plan FROM users WHERE id = :userId")
    fun observeActivePlan(userId: Int): Flow<Int?>

    @Query("UPDATE users SET active_plan = :planId WHERE id = :userId")
    suspend fun setActivePlan(userId: Int, planId: Int?)
}
