package com.navigine.indoornavigationdemo.presentation.ui.screens.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navigine.indoornavigationdemo.presentation.ui.main.MainViewModel
import com.navigine.indoornavigationdemo.presentation.ui.screens.management.EventFormViewModel
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    eventId: String?, // The ID comes from navigation as a String
    mainViewModel: MainViewModel, // To get the current user
    eventFormViewModel: EventFormViewModel = viewModel(),
//    onSaveSuccess: () -> Unit,
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    // --- STATE OBSERVATION ---
    val uiState by eventFormViewModel.uiState.collectAsState()//Property delegate must have a 'getValue(Nothing?, KProperty0<ERROR CLASS: Cannot infer argument for type parameter T>)' method. None of the following functions is applicable:
   // fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T
    val formState = eventFormViewModel.formState //need to create extension property
    val currentUser by mainViewModel.currentUser.collectAsState()

    // --- EFFECT HANDLERS ---
    // This runs once when the screen is first shown, to load the event for editing or prepare a new form.
    LaunchedEffect(eventId) {
        eventFormViewModel.loadEvent(eventId?.toIntOrNull()) //Unresolved reference 'loadEvent'.
    }

    // This observes the `saveSuccess` flag. When it becomes true, we navigate back.
    LaunchedEffect(uiState.saveSuccess) {//unresolved reference saveSuccess
        if (uiState.saveSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("event_saved", true)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Event" else "Create Event") }, //unresolved reference
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to dashboard")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- FORM FIELDS ---
            OutlinedTextField(
                value = formState.eventName,
                onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventName = it)) }, //unresolved reference
                label = { Text("Event Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = formState.eventDescription,
                onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventDescription = it)) },
                label = { Text("Event Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = formState.eventType,
                onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventType = it)) },
                label = { Text("Event Type (e.g., Conference)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = formState.eventLocation,
                onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventLocation = it)) },
                label = { Text("Event Location / Venue") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

            // --- Category Dropdown Menu ---
            ExposedDropdownMenuBox(
                expanded = isCategoryDropdownExpanded,
                onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor() // This is crucial for connecting the text field to the dropdown
                        .fillMaxWidth(),
                    readOnly = true,
                    value = eventFormViewModel.selectedDropdownCategory,
                    onValueChange = {}, // No-op because it's read-only
                    label = { Text("Event Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false }
                ) {
                    eventFormViewModel.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                // Call the ViewModel function to update the state
                                eventFormViewModel.onCategoryDropdownSelect(category)
                                // Close the dropdown
                                isCategoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Conditional "Others" Text Field ---
            // This will smoothly slide in and out of view.
            AnimatedVisibility(
                visible = eventFormViewModel.selectedDropdownCategory == "Others",
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                OutlinedTextField(
                    value = eventFormViewModel.customCategoryText,
                    onValueChange = { eventFormViewModel.customCategoryText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter Custom Category") },
                    placeholder = { Text("e.g., Community Meetup") },
                    singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = formState.eventMaxPax,
                onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventMaxPax = it)) },
                label = { Text("Maximum Capacity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isEditMode) {
                Spacer(Modifier.height(16.dp))

                var isStatusMenuExpanded by remember { mutableStateOf(false) }
                val statusOptions = listOf("UPCOMING", "ONGOING", "CANCELLED", "ENDED")

                ExposedDropdownMenuBox(
                    expanded = isStatusMenuExpanded,
                    onExpandedChange = { isStatusMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = formState.eventStatus,
                        onValueChange = {}, // The dropdown items handle the change
                        readOnly = true,
                        label = { Text("Event Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStatusMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor() // This is important for the dropdown to anchor correctly
                    )
                    ExposedDropdownMenu(
                        expanded = isStatusMenuExpanded,
                        onDismissRequest = { isStatusMenuExpanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    // Update the ViewModel's state with the new status
                                    eventFormViewModel.onFormStateChange(formState.copy(eventStatus = option))
                                    isStatusMenuExpanded = false // Close the menu
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    label = "Start Date",
                    value = formState.eventStartDate,
                    onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventStartDate = it)) },
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    label = "Start Time",
                    value = formState.eventStartTime,
                    onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventStartTime = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePickerField(
                    label = "End Date",
                    value = formState.eventEndDate,
                    onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventEndDate = it)) },
                    modifier = Modifier.weight(1f)
                )
                TimePickerField(
                    label = "End Time",
                    value = formState.eventEndTime,
                    onValueChange = { eventFormViewModel.onFormStateChange(formState.copy(eventEndTime = it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // --- ERROR DISPLAY ---
            if (uiState.error != null) { //unresolved reference
                Spacer(Modifier.height(16.dp))
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(32.dp))

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    // We need the current user's ID to create or edit an event.
                    currentUser?.let { user ->
                        eventFormViewModel.saveEvent(user.userId)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading && currentUser != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Save Event", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
//private
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Parse the current value to set the initial date in the picker
    val (initialYear, initialMonth, initialDay) = try {
        val date = LocalDate.parse(value)
        Triple(date.year, date.monthValue - 1, date.dayOfMonth)
    } catch (e: Exception) {
        // Fallback to today if the string is empty or invalid
        Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            // Format the selected date into "yyyy-MM-dd"
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            val formattedDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            onValueChange(formattedDate)
        }, initialYear, initialMonth, initialDay
    )

    OutlinedTextField(
        value = value,
        onValueChange = {}, // Input is read-only
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Date",
                modifier = Modifier.clickable { datePickerDialog.show() }
            )
        },
        modifier = modifier
    )
}

@Composable
//private
fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val (initialHour, initialMinute) = try {
        val time = LocalTime.parse(value)
        Pair(time.hour, time.minute)
    } catch (e: Exception) {
        Pair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            // Format the selected time into "HH:mm:ss"
            val selectedTime = LocalTime.of(hourOfDay, minute,0)
            // Using a specific pattern to ensure seconds are included
            val formattedTime = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()))
            onValueChange(formattedTime)
        }, initialHour, initialMinute, true // true for 24-hour format
    )

    OutlinedTextField(
        value = value,
        onValueChange = {}, // Input is read-only
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            // Using the same icon for simplicity, but you could use a clock icon
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Time",
                modifier = Modifier.clickable { timePickerDialog.show() }
            )
        },
        modifier = modifier
    )
}