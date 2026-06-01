package com.example.fitme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)
fun calculateAchievements(
    history: List<HistoryItem>,
    exerciseRecords: Map<Int, ExerciseRecordState>
): List<Achievement> {
    val workoutCount = history.size

    val hasLowRepRecord = exerciseRecords.values.any { it.lowRep != null }
    val hasHighRepRecord = exerciseRecords.values.any { it.highRep != null }

    return listOf(
        Achievement(
            id = "first_workout",
            title = "Первый шаг",
            description = "Проведена первая тренировка",
            icon = Icons.Default.DirectionsRun,
            isUnlocked = workoutCount >= 1
        ),
        Achievement(
            id = "ten_workouts",
            title = "Уверенный старт",
            description = "Завершено 10 тренировок",
            icon = Icons.Default.Star,
            isUnlocked = workoutCount >= 10
        ),
        Achievement(
            id = "fifty_workouts",
            title = "Железная воля",
            description = "Завершено 50 тренировок",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = workoutCount >= 50
        ),
        Achievement(
            id = "first_low_rep_record",
            title = "Первый малоповторный рекорд",
            description = "Записан первый рекорд в малоповторном слоте",
            icon = Icons.Default.FitnessCenter,
            isUnlocked = hasLowRepRecord
        ),
        Achievement(
            id = "first_high_rep_record",
            title = "Первый многоповторный рекорд",
            description = "Записан первый рекорд в многоповторном слоте",
            icon = Icons.Default.FitnessCenter,
            isUnlocked = hasHighRepRecord
        )
    )
}

@Composable
fun AchievementsSection(
    history: List<HistoryItem>,
    exerciseRecords: Map<Int, ExerciseRecordState>
) {
    val achievements = remember(history, exerciseRecords) {
        calculateAchievements(history, exerciseRecords)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Достижения",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        achievements.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {}
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement, modifier: Modifier = Modifier) {
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
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
