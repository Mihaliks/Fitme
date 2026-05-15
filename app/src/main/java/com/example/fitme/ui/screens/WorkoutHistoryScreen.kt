package com.example.fitme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutHistoryScreen(onBack: () -> Unit = {}) {
    val viewModel: WorkoutsViewModel = viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val history by viewModel.workoutHistory.collectAsState()
    val skippedSessionIds by viewModel.skippedSessionIds.collectAsState()
    val activePlanId by viewModel.activePlanId.collectAsState()
    val nextWorkoutPreview by viewModel.nextWorkoutPreview.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "История тренировок",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { item ->
                val isSkipped = skippedSessionIds.contains(item.session.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSkipped) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                         else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.session.date.format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.templateName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (isSkipped) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = if (isSkipped) "Пропущено" else "Выполнено",
                            tint = if (isSkipped) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (history.isEmpty()) {
                item {
                    Text(
                        text = "У вас пока нет выполненных тренировок.",
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Next Workout Preview
        if (activePlanId != null && nextWorkoutPreview != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Следующая тренировка",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextWorkoutPreview!!.template.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.markWorkoutVisited(activePlanId!!) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text("Отметить")
                        }
                        OutlinedButton(
                            onClick = { viewModel.markWorkoutSkipped(activePlanId!!) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("Пропустить")
                        }
                    }
                }
            }
        }
    }
}
