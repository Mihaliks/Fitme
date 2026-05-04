package com.example.fitme.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitme.data.AppDatabase
import com.example.fitme.data.entities.Plan
import com.example.fitme.data.repositories.UserRepository
import com.example.fitme.data.repositories.UserRepository.Companion.CURRENT_USER_ID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class UserRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = UserRepository(db.userDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun createUserCreatesProfileAtFixedId() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)
        val saved = repository.getUser()
        assertNotNull(saved)
        assertEquals(CURRENT_USER_ID, saved!!.id)
        assertEquals("Михаил", saved.name)
        assertEquals(25, saved.age)
        assertEquals(75f, saved.weight)
        assertEquals(180f, saved.height)
        assertNull(saved.activePlan)
    }

    @Test
    fun createUserDoesNotOverwrite() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "Сила")).toInt()
        repository.setActivePlan(planId)

        repository.createUser(name = "Другое имя", age = 99, weight = 1f, height = 1f)

        val saved = repository.getUser()!!
        assertEquals("Михаил", saved.name)
        assertEquals(25, saved.age)
        assertEquals(planId, saved.activePlan)
    }

    @Test
    fun getUserReturnsNullBeforeCreate() = runBlocking {
        assertNull(repository.getUser())
    }


    @Test
    fun setNameUpdatesOnlyName() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)

        repository.setName("Миша")

        val saved = repository.getUser()!!
        assertEquals("Миша", saved.name)
        assertEquals(25, saved.age)
        assertEquals(75f, saved.weight)
        assertEquals(180f, saved.height)
    }

    @Test
    fun setActivePlanIsReflectedInGetCurrentPlanId() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "Сила")).toInt()

        repository.setActivePlan(planId)

        assertEquals(planId, repository.getCurrentPlanId())
    }

    @Test
    fun setActivePlanWithNullClearsActivePlan() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "Сила")).toInt()
        repository.setActivePlan(planId)

        repository.setActivePlan(null)

        assertNull(repository.getCurrentPlanId())
    }

    @Test
    fun observeActivePlanEmitsCurrentValueAfterSet() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)
        val planId = db.workoutPlanDao().insertPlan(Plan(name = "Сила")).toInt()

        repository.setActivePlan(planId)

        val emitted = repository.observeActivePlan().first()
        assertEquals(planId, emitted)
    }

    @Test
    fun observeActivePlanReturnsNullByDefault() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)

        val emitted = repository.observeActivePlan().first()
        assertNull(emitted)
    }

    @Test
    fun deletingActivePlanResetsItToNull() = runBlocking {
        repository.createUser(name = "Михаил", age = 25, weight = 75f, height = 180f)
        val plan = Plan(name = "Сила")
        val planId = db.workoutPlanDao().insertPlan(plan).toInt()
        repository.setActivePlan(planId)

        db.workoutPlanDao().deletePlan(plan.copy(id = planId))

        assertNull(repository.getCurrentPlanId())
    }
}
