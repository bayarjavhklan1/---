@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.bayrjavhlanmw.finance.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bayrjavhlanmw.finance.data.EntryFilter
import com.bayrjavhlanmw.finance.data.EntryType
import com.bayrjavhlanmw.finance.data.FinanceEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val displayDate = DateTimeFormatter.ofPattern("yyyy.MM.dd")
fun EntryType.label() = if (this == EntryType.INCOME) "Орлого" else "Зарлага"
fun EntryFilter.label() = when (this) {
    EntryFilter.ALL -> "Бүх бүртгэл"
    EntryFilter.INCOME -> "Зөвхөн орлого"
    EntryFilter.EXPENSE -> "Зөвхөн зарлага"
}

@Composable
fun FinanceApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") { backStackEntry ->
            val model: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val state by model.uiState.collectAsStateWithLifecycle()
            val savedId by backStackEntry.savedStateHandle.getStateFlow("savedEntryId", 0L).collectAsStateWithLifecycle()
            LaunchedEffect(savedId) {
                if (savedId != 0L) {
                    model.selectId(savedId)
                    backStackEntry.savedStateHandle["savedEntryId"] = 0L
                }
            }
            HomeScreen(state, model::previous, model::next, model::delete, model::dismissError,
                onAdd = { navController.navigate("entry/0") },
                onEdit = { navController.navigate("entry/$it") },
                onSettings = { navController.navigate("settings") })
        }
        composable("entry/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
            val model: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
            EntryScreen(model.state, model.entryId != 0L, model::type, model::name, model::amount, model::date,
                onBack = { navController.popBackStack() },
                onSave = { model.save { id ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("savedEntryId", id)
                    navController.popBackStack()
                } })
        }
        composable("settings") {
            val model: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val filter by model.filter.collectAsStateWithLifecycle()
            SettingsScreen(filter, model::select) { navController.popBackStack() }
        }
    }
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    previous: () -> Unit,
    next: () -> Unit,
    delete: (FinanceEntry) -> Unit,
    dismissError: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onSettings: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val current = state.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Санхүү", fontWeight = FontWeight.Bold) }, actions = {
                Box {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, "Цэс") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Тохиргоо") }, leadingIcon = { Icon(Icons.Default.Settings, null) },
                            onClick = { menuOpen = false; onSettings() })
                    }
                }
            })
        }
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            val landscape = maxWidth > maxHeight
            val entryContent: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(state.filter.label(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (current == null) "0 / 0" else "${state.index + 1} / ${state.entries.size}",
                            style = MaterialTheme.typography.labelLarge)
                    }
                    if (state.loading) Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else if (current != null) EntryDetails(current, state.filter, { onEdit(current.id) })
                    else EmptyEntries(state.filter)
                    state.error?.let { message ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                            IconButton(onClick = dismissError) { Icon(Icons.Default.Close, "Хаах") }
                        }
                    }
                }
            }
            val actions: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = previous, enabled = current != null && state.index > 0,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Өмнөх")
                        }
                        OutlinedButton(onClick = next, enabled = current != null && state.index < state.entries.lastIndex,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            Text("Дараах"); Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { current?.let { onEdit(it.id) } }, enabled = current != null,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            Icon(Icons.Default.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Засах")
                        }
                        OutlinedButton(onClick = { deleteId = current?.id }, enabled = current != null,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseColor),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Устгах")
                        }
                    }
                    Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Нэмэх")
                    }
                }
            }
            if (landscape) {
                Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Box(Modifier.weight(1f)) { entryContent() }
                    Box(Modifier.widthIn(max = 300.dp).weight(0.85f).padding(top = 40.dp)) { actions() }
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    entryContent()
                    actions()
                }
            }
        }
    }
    val pending = state.entries.firstOrNull { it.id == deleteId }
    if (pending != null) AlertDialog(
        onDismissRequest = { deleteId = null },
        icon = { Icon(Icons.Default.DeleteOutline, null) },
        title = { Text("Бүртгэл устгах уу?") },
        text = { Text("“${pending.name}” бүртгэл устах болно.") },
        confirmButton = { TextButton(onClick = { deleteId = null; delete(pending) }) { Text("Устгах", color = ExpenseColor) } },
        dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Болих") } }
    )
}

@Composable
private fun EntryDetails(entry: FinanceEntry, filter: EntryFilter, onEdit: () -> Unit) {
    var revealed by rememberSaveable(entry.id, filter) { mutableStateOf(false) }
    val income = entry.type == EntryType.INCOME
    val accent = if (income) IncomeColor else ExpenseColor
    Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = onEdit, onLongClickLabel = "Бүртгэл засах"),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(if (income) Icons.Default.SouthWest else Icons.Default.NorthEast, null, tint = accent)
                Text(entry.type.label(), color = accent, fontWeight = FontWeight.Bold)
            }
            Text(entry.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            val amount = if (revealed) Money.format(entry.amountMinor) else "**** ₮"
            Row(Modifier.fillMaxWidth().combinedClickable(onClick = { revealed = !revealed }, onLongClick = onEdit)
                .semantics { contentDescription = if (revealed) "Дүн нуух" else "Дүн харуулах" },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                var size by remember(amount) { mutableStateOf(32f) }
                Text(amount, fontSize = size.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                    onTextLayout = { if (it.hasVisualOverflow && size > 16) size -= 2 },
                    color = if (revealed) accent else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically))
                Icon(if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(LocalDate.ofEpochDay(entry.dateEpochDay).format(displayDate), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyEntries(filter: EntryFilter) {
    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.AccountBalanceWallet, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(if (filter == EntryFilter.ALL) "Бүртгэл байхгүй" else "${filter.label()} бүртгэл алга", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun EntryScreen(
    state: EntryFormState, editing: Boolean, onType: (EntryType) -> Unit,
    onName: (String) -> Unit, onAmount: (String) -> Unit, onDate: (LocalDate) -> Unit,
    onBack: () -> Unit, onSave: () -> Unit
) {
    var dateOpen by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(title = { Text(if (editing) "Бүртгэл засах" else "Бүртгэл нэмэх") }, navigationIcon = {
            IconButton(onClick = onBack, enabled = !state.saving) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Буцах") }
        })
    }) { padding ->
        if (state.loading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (state.missing) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(state.error ?: "Бүртгэл олдсонгүй.") }
        else Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp).widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                EntryType.entries.forEachIndexed { index, type ->
                    SegmentedButton(selected = state.type == type, onClick = { onType(type) }, enabled = !state.saving,
                        shape = SegmentedButtonDefaults.itemShape(index, EntryType.entries.size)) { Text(type.label()) }
                }
            }
            OutlinedTextField(value = state.name, onValueChange = onName, label = { Text("Гүйлгээний нэр") },
                singleLine = true, enabled = !state.saving, isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.amount, onValueChange = onAmount, label = { Text("Мөнгөн дүн") },
                suffix = { Text("₮") }, singleLine = true, enabled = !state.saving, isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.date.format(displayDate), onValueChange = {}, label = { Text("Огноо") }, readOnly = true,
                trailingIcon = { IconButton(onClick = { dateOpen = true }, enabled = !state.saving) { Icon(Icons.Default.CalendarMonth, "Огноо сонгох") } },
                modifier = Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onSave, enabled = !state.saving, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                if (state.saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp)); Text(if (state.saving) "Хадгалж байна" else "Хадгалах")
            }
        }
    }
    if (dateOpen) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = state.date.toEpochDay() * 86_400_000L)
        DatePickerDialog(onDismissRequest = { dateOpen = false },
            confirmButton = { TextButton(onClick = {
                dateState.selectedDateMillis?.let { onDate(LocalDate.ofEpochDay(it / 86_400_000L)) }; dateOpen = false
            }) { Text("Сонгох") } },
            dismissButton = { TextButton(onClick = { dateOpen = false }) { Text("Болих") } }
        ) { DatePicker(state = dateState) }
    }
}

@Composable
private fun SettingsScreen(filter: EntryFilter, onSelect: (EntryFilter) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Тохиргоо") }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Буцах") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Харагдах бүртгэл", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                EntryFilter.entries.forEach { option ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).selectable(selected = option == filter,
                        onClick = { onSelect(option) }, role = Role.RadioButton),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = option == filter, onClick = null)
                        Spacer(Modifier.width(12.dp)); Text(option.label())
                    }
                }
            }
        }
    }
}
