package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Job
import com.example.data.model.JobFilter
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.JobCard
import com.example.ui.components.RadarPulseBlip
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.RadarMint
import com.example.ui.theme.RadarTeal
import com.example.ui.viewmodel.JobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    jobViewModel: JobViewModel,
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val jobs by jobViewModel.jobs.collectAsState()
    val isLoading by jobViewModel.isLoading.collectAsState()
    val filter by jobViewModel.filter.collectAsState()
    val savedJobs by jobViewModel.savedJobs.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(filter.query) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Radar Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_radar_banner),
                contentDescription = "Radar banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x990A0F1D),
                                Color(0xFA0A0F1D)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadarPulseBlip(sizeDp = 24, color = RadarMint)
                        Text(
                            text = "CAREER RADAR LIVE",
                            color = RadarMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-Time FastAPI Scanner",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${jobs.size} matched opportunities aligned with your stack",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { jobViewModel.loadJobs() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Jobs",
                        tint = Color.White
                    )
                }
            }
        }

        // Search & Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    jobViewModel.updateSearchQuery(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("job_search_input"),
                placeholder = { Text("Search title, company, or skills...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RadarTeal)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            jobViewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RadarTeal,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filter Button with Badge
            BadgedBox(
                badge = {
                    if (filter.activeFilterCount > 0) {
                        Badge(containerColor = RadarMint) {
                            Text("${filter.activeFilterCount}")
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (filter.activeFilterCount > 0)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("filter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Open Filters",
                        tint = if (filter.activeFilterCount > 0) RadarTeal else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAll = filter.activeFilterCount == 0 && filter.query.isEmpty()
            FilterChip(
                selected = isAll,
                onClick = {
                    searchQuery = ""
                    jobViewModel.clearFilters()
                },
                label = { Text("All Jobs") }
            )

            val is90Plus = filter.minRadarScore == 90
            FilterChip(
                selected = is90Plus,
                onClick = {
                    jobViewModel.updateFilter(
                        filter.copy(minRadarScore = if (is90Plus) 0 else 90)
                    )
                },
                label = { Text("🎯 90%+ Match") }
            )

            val isRemote = filter.workMode.equals("Remote", ignoreCase = true)
            FilterChip(
                selected = isRemote,
                onClick = {
                    jobViewModel.updateFilter(
                        filter.copy(workMode = if (isRemote) null else "Remote")
                    )
                },
                label = { Text("🌐 Remote") }
            )

            val isHighSalary = filter.minSalary == 150
            FilterChip(
                selected = isHighSalary,
                onClick = {
                    jobViewModel.updateFilter(
                        filter.copy(minSalary = if (isHighSalary) 0 else 150)
                    )
                },
                label = { Text("💰 $150k+/yr") }
            )

            val isSenior = filter.experienceLevel.equals("Senior", ignoreCase = true)
            FilterChip(
                selected = isSenior,
                onClick = {
                    jobViewModel.updateFilter(
                        filter.copy(experienceLevel = if (isSenior) null else "Senior")
                    )
                },
                label = { Text("⚡ Senior") }
            )
        }

        // Active filter indicator if any
        if (filter.activeFilterCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filter.activeFilterCount} active filter(s) applied",
                    style = MaterialTheme.typography.bodySmall,
                    color = RadarTeal
                )
                Text(
                    text = "Clear all",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        searchQuery = ""
                        jobViewModel.clearFilters()
                    }
                )
            }
        }

        // Jobs Content List or Loading/Empty
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (isLoading && jobs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = RadarTeal)
                        Text(
                            text = "Scanning FastAPI Career Radar...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (jobs.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadarPulseBlip(sizeDp = 48, color = RadarCyan)
                            Text(
                                text = "No Radar Matches Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No active jobs currently match your search criteria. Try lowering the score threshold or clearing filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    jobViewModel.clearFilters()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RadarTeal)
                            ) {
                                Text("Reset All Filters")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = jobs,
                        key = { it.id }
                    ) { job ->
                        val isSaved = savedJobs.any { it.id == job.id }
                        JobCard(
                            job = job,
                            isSaved = isSaved,
                            onJobClick = { onJobClick(job.id) },
                            onToggleSave = { jobViewModel.toggleSaveJob(job) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = filter,
            onApplyFilter = { newFilter ->
                jobViewModel.updateFilter(newFilter)
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}
