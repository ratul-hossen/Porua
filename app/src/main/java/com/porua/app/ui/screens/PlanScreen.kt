package com.porua.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.data.local.entity.StudyTaskEntity
import com.porua.app.ui.components.BottomNavItem
import com.porua.app.ui.components.PoruaBottomNav
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeLg
import com.porua.app.ui.theme.ShapeMd
import com.porua.app.ui.theme.ShapeSm
import com.porua.app.ui.viewmodel.PoruaUiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun PlanScreen(
    uiState: PoruaUiState,
    eventFlow: SharedFlow<String>,
    onNavigateBottom: (BottomNavItem) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (StudyTaskEntity) -> Unit,
    onAddTask: (title: String, subject: String, duration: Int, priority: String, category: String) -> Unit
) {
    val colors = LocalPoruaColors.current
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        eventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PoruaBottomNav(
                selectedItem = BottomNavItem.PLAN,
                onItemSelected = onNavigateBottom
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = colors.bg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Study Plan",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text
                        )
                        Text(
                            text = "Auto-generated day-by-day exam schedule",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(ShapeSm)
                            .background(colors.primarySoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${uiState.studyTasks.count { !it.isCompleted }} due today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Schedule Date Header
                Text(
                    text = "Today's Schedule",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.studyTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No study tasks scheduled. Tap '+' to create one!",
                            fontSize = 14.sp,
                            color = colors.textMuted
                        )
                    }
                }
            } else {
                items(uiState.studyTasks) { task ->
                    PlanTaskItem(
                        task = task,
                        onToggle = { onToggleTask(task.id, task.isCompleted) },
                        onDelete = { onDeleteTask(task) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Task Dialog (User Input & Validation requirement)
        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, subject, duration, priority, category ->
                    onAddTask(title, subject, duration, priority, category)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun PlanTaskItem(
    task: StudyTaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalPoruaColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLg)
            .background(colors.surface)
            .border(1.dp, colors.border, ShapeLg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Completion Checkbox
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (task.isCompleted) colors.success else Color.Transparent)
                .border(2.dp, if (task.isCompleted) colors.success else colors.border, CircleShape)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) colors.textMuted else colors.text
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${task.subject} • ${task.durationMinutes} min • Priority: ${task.priority}",
                fontSize = 12.sp,
                color = colors.textMuted
            )
        }

        // Delete button
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete task",
            tint = colors.textMuted,
            modifier = Modifier
                .size(20.dp)
                .clickable { onDelete() }
        )
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, duration: Int, priority: String, category: String) -> Unit
) {
    val colors = LocalPoruaColors.current

    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    val subjects = listOf("Chemistry", "Physics", "Higher Math", "English", "Biology", "ICT")
    var selectedSubject by remember { mutableStateOf(subjects[0]) }
    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    var durationMinutes by remember { mutableFloatStateOf(20f) }

    val priorities = listOf("High", "Medium", "Low")
    var selectedPriority by remember { mutableStateOf("Medium") }

    val categories = listOf("Revise", "Practice", "Learn")
    var selectedCategory by remember { mutableStateOf("Practice") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Revision Task",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.text
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Input 1: TextField with Validation
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Task / Topic Title") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("Title cannot be empty", color = colors.danger)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input 2: Subject DropdownMenu
                Text("Subject", fontSize = 12.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeSm)
                        .background(colors.surface2)
                        .clickable { isSubjectDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedSubject, fontSize = 14.sp, color = colors.text)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = isSubjectDropdownExpanded,
                        onDismissRequest = { isSubjectDropdownExpanded = false }
                    ) {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedSubject = sub
                                    isSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input 3: Slider for Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duration", fontSize = 12.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                    Text("${durationMinutes.toInt()} minutes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                }
                Slider(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    valueRange = 10f..90f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input 4: Priority RadioButtons
                Text("Priority", fontSize = 12.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    priorities.forEach { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedPriority = p }
                                .padding(end = 12.dp)
                        ) {
                            RadioButton(
                                selected = selectedPriority == p,
                                onClick = { selectedPriority = p },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                            Text(text = p, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            PoruaPrimaryButton(
                text = "Save Task",
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onConfirm(
                            title,
                            selectedSubject,
                            durationMinutes.toInt(),
                            selectedPriority,
                            selectedCategory
                        )
                    }
                },
                height = 44.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        },
        containerColor = colors.surface
    )
}
