package com.example.fitme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.temporal.ChronoUnit

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)

fun calculateAchievements(history: List<HistoryItem>): List<Achievement> {
    val workoutCount = history.size
    val maxDuration = history.maxOfOrNull { it.session.totalDuration ?: 0 } ?: 0
    val sortedDates = history.map { it.session.date }.sortedDescending().distinct()
    var maxStreak = if (sortedDates.isNotEmpty()) 1 else 0
    var currentStreak = maxStreak

    for (i in 1 until sortedDates.size) {
        val daysBetween = ChronoUnit.DAYS.between(sortedDates[i], sortedDates[i - 1])
        if (daysBetween == 1L) {
            currentStreak++
            if (currentStreak > maxStreak) maxStreak = currentStreak
        } else {
            currentStreak = 1
        }
    }

    return listOf(
        Achievement(
            id = "first_workout",
            title = "Первый шаг",
            description = "Проведена первая тренировка",
            icon = Icons.Default.DirectionsRun,
            isUnlocked = workoutCount >= 1
        ),
        Achievement(
            id = "five_workouts",
            title = "Новичок",
            description = "Завершено 5 тренировок",
            icon = Icons.Default.Star,
            isUnlocked = workoutCount >= 5
        ),
        Achievement(
            id = "ten_workouts",
            title = "Уверенный старт",
            description = "Завершено 10 тренировок",
            icon = Icons.Default.FitnessCenter,
            isUnlocked = workoutCount >= 10
        ),
        Achievement(
            id = "fifty_workouts",
            title = "Железная воля",
            description = "Завершено 50 тренировок!",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = workoutCount >= 50
        ),
        Achievement(
            id = "marathon",
            title = "Марафонец",
            description = "Тренировка длилась более 120 минут",
            icon = Icons.Default.Timer,
            isUnlocked = maxDuration >= 120
        ),
        Achievement(
            id = "unstoppable",
            title = "Неудержимый",
            description = "10 дней тренировок подряд",
            icon = Icons.Default.Whatshot,
            isUnlocked = maxStreak >= 10
        )
    )
}

@Composable
fun AchievementsSection(history: List<HistoryItem>) {
    val achievements = calculateAchievements(history)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Достижения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        achievements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { achievement ->
                    Box(modifier = Modifier.weight(1f)) {
                        AchievementCard(achievement)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val containerColor = if (achievement.isUnlocked)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val contentColor = if (achievement.isUnlocked)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = achievement.title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
