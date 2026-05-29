package com.example.fitme.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitme.data.entities.enums.BodyRegion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Level2Screen(onBack: () -> Unit) {
    val viewModel: WorkoutsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(androidx.activity.compose.LocalActivity.current as androidx.activity.ComponentActivity)
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val templates by viewModel.templatesByRegion.collectAsState()

    BackHandler(enabled = selectedRegion != null) { viewModel.selectRegion(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedRegion?.toRussian() ?: "lvl 2: По группам мышц", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { if (selectedRegion != null) viewModel.selectRegion(null) else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (selectedRegion == null) {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(BodyRegion.entries) { region -> RegionCard(region) { viewModel.selectRegion(region) } }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (templates.isEmpty()) {
                    item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("Тренировок не найдено", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                } else {
                    items(templates) { (template, exercises) -> TemplateCard(template, exercises, viewModel) }
                }
            }
        }
    }
}

@Composable
fun RegionCard(region: BodyRegion, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(110.dp).clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(region.toRussian(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
    }
}

