package org.dhis2.dqapp.ui

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.dhis2.dqapp.AuthMode
import org.dhis2.dqapp.MainViewModel
import org.dhis2.dqapp.PeriodType
import org.dhis2.dqapp.R
import org.dhis2.dqapp.StatusType
import org.dhis2.dqapp.data.LqasState
import org.dhis2.dqapp.ui.theme.AppBackgroundBrush
import org.dhis2.dqapp.ui.theme.DQAppTheme
import org.dhis2.dqapp.ui.theme.Muted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DqAppRoot(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState

    DQAppTheme {
        Surface(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackgroundBrush)
                    .padding(16.dp)
            ) {
                if (!state.connected) {
                    LoginScreen(viewModel)
                } else {
                    AppScreen(viewModel)
                }

                if (state.isBusy) {
                    BusyOverlay(
                        message = if (state.status.type == StatusType.MUTED && state.status.message.isNotBlank()) {
                            state.status.message
                        } else {
                            "Processing..."
                        }
                    )
                }

                if (state.showSessionLogin) {
                    SessionLoginOverlay(
                        baseUrl = state.baseUrl,
                        onCancel = { viewModel.cancelSessionLogin() },
                        onCookie = { cookie -> viewModel.onSessionCookie(cookie) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoginScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        LoginBrandHeader()
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.baseUrl,
                        onValueChange = viewModel::updateBaseUrl,
                        label = { Text("DHIS2 Base URL") },
                        placeholder = { Text("https://your-dhis2.org") },
                        modifier = Modifier.width(280.dp)
                    )

                    if (state.authMode == AuthMode.BASIC) {
                        OutlinedTextField(
                            value = state.username,
                            onValueChange = viewModel::updateUsername,
                            label = { Text("Username") },
                            modifier = Modifier.width(200.dp)
                        )
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::updatePassword,
                            label = { Text("Password") },
                            modifier = Modifier.width(200.dp),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                    }

                    OutlinedTextField(
                        value = state.districtLevel,
                        onValueChange = viewModel::updateDistrictLevel,
                        label = { Text("District Level") },
                        modifier = Modifier.width(150.dp)
                    )

                    OutlinedTextField(
                        value = state.hfLevel,
                        onValueChange = viewModel::updateHfLevel,
                        label = { Text("HF Level") },
                        modifier = Modifier.width(150.dp)
                    )

                    Button(onClick = { viewModel.connect() }) {
                        Text("Connect")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                StatusText(state.status)
            }
        }
    }
}

@Composable
private fun LoginBrandHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_brand_facility),
            contentDescription = "LQAS Insight logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(42.dp)
        )
        Column {
            Text(
                text = "LQAS Insight",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Facility Data Validation",
                color = Muted,
                fontSize = 12.sp
            )
            Text(
                text = "HISP-PAK",
                color = Muted,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState
    val context = LocalContext.current
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    val csvSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val csv = pendingCsv
        pendingCsv = null
        if (uri == null) {
            viewModel.setStatus("Excel export cancelled.")
            return@rememberLauncherForActivityResult
        }
        if (csv == null) {
            viewModel.setStatus("No report to export.", StatusType.DANGER)
            return@rememberLauncherForActivityResult
        }
        exportCsvToUri(
            context = context,
            csvContent = csv,
            outputUri = uri,
            onSuccess = { msg -> viewModel.setStatus(msg, StatusType.SUCCESS) },
            onError = { msg -> viewModel.setStatus(msg, StatusType.DANGER) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = viewModel::logout) {
                Text("Logout")
            }
        }

        Text(
            text = "Workflow: Select District + Period + Dataset -> set Data Elements count -> Load/Generate -> Enter Y/N (optional S/X) -> Calculate -> Save.",
            color = Muted,
            fontSize = 12.sp
        )
        Text(
            text = "Y=YES, N=NO, S=SKIP, X=MISSING (X is excluded from totals).",
            color = Muted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(
                        label = "Period Type",
                        value = when (state.periodType) {
                            PeriodType.MONTHLY -> "Monthly"
                            PeriodType.DAILY -> "Daily"
                            PeriodType.CUSTOM -> "Custom Period ID"
                        },
                        options = listOf(
                            "Monthly" to PeriodType.MONTHLY,
                            "Daily" to PeriodType.DAILY,
                            "Custom Period ID" to PeriodType.CUSTOM
                        ),
                        onSelect = viewModel::updatePeriodType
                    )

                    when (state.periodType) {
                        PeriodType.MONTHLY -> OutlinedTextField(
                            value = state.periodMonth,
                            onValueChange = viewModel::updatePeriodMonth,
                            label = { Text("Month (YYYY-MM)") },
                            modifier = Modifier.width(200.dp)
                        )
                        PeriodType.DAILY -> OutlinedTextField(
                            value = state.periodDay,
                            onValueChange = viewModel::updatePeriodDay,
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.width(200.dp)
                        )
                        PeriodType.CUSTOM -> OutlinedTextField(
                            value = state.periodCustom,
                            onValueChange = viewModel::updatePeriodCustom,
                            label = { Text("Period ID") },
                            placeholder = { Text("e.g. 2024Q1") },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DropdownField(
                        label = "District (OU)",
                        value = state.districts.find { it.id == state.selectedDistrictId }?.name ?: "",
                        options = state.districts.map { it.name to it.id },
                        onSelect = viewModel::updateSelectedDistrict
                    )

                    DropdownField(
                        label = "Dataset",
                        value = state.datasets.find { it.id == state.selectedDatasetId }?.name ?: "",
                        options = state.datasets.map { it.name to it.id },
                        onSelect = viewModel::updateSelectedDataset
                    )

                    OutlinedTextField(
                        value = state.benchmark,
                        onValueChange = viewModel::updateBenchmark,
                        label = { Text("Benchmark (%)") },
                        modifier = Modifier.width(150.dp)
                    )

                    OutlinedTextField(
                        value = state.dataElementCount,
                        onValueChange = viewModel::updateDataElementCount,
                        label = { Text("Data Elements (Random)") },
                        modifier = Modifier.width(190.dp)
                    )

                    DropdownField(
                        label = "Saved Records",
                        value = state.savedKeys.find { it == state.selectedSavedKey }?.let { viewModel.formatSavedKey(it) } ?: "-- select --",
                        options = listOf("-- select --" to "") + state.savedKeys.map { viewModel.formatSavedKey(it) to it },
                        onSelect = viewModel::updateSelectedSavedKey,
                        width = 260.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(onClick = viewModel::loadOrGenerate) { Text("Load / Generate") }
                    Button(onClick = viewModel::calculate) { Text("Calculate") }
                    Button(onClick = viewModel::save) { Text("Save") }
                    Button(onClick = {
                        val csv = viewModel.buildReportCsv()
                        if (csv == null) {
                            viewModel.setStatus("No report to export. Load/Generate sample first.", StatusType.DANGER)
                            return@Button
                        }
                        pendingCsv = csv
                        csvSaveLauncher.launch("lqas_report_${System.currentTimeMillis()}.csv")
                    }) { Text("Export Excel") }
                    Button(onClick = {
                        val html = viewModel.buildReportHtml()
                        if (html == null) {
                            viewModel.setStatus(
                                "No report to export. Load/Generate sample first.",
                                StatusType.DANGER
                            )
                            return@Button
                        }
                        viewModel.setStatus("Opening print preview...")
                        exportHtmlToPdf(
                            context = context,
                            html = html,
                            onSuccess = { message ->
                                viewModel.setStatus(message, StatusType.SUCCESS)
                            },
                            onError = { message ->
                                viewModel.setStatus(message, StatusType.DANGER)
                            }
                        )
                    }) { Text("Export PDF") }
                    Button(onClick = viewModel::loadSaved) { Text("Load Saved") }
                    Button(onClick = viewModel::refreshSaved) { Text("Refresh Saved") }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        StatusText(state.status)
        Spacer(modifier = Modifier.height(12.dp))

        ReportTable(
            state = state.currentState,
            entryOptions = viewModel.entryOptions(),
            onEntryChange = viewModel::updateEntry,
            onFacilityIncludeChange = viewModel::updateFacilityIncluded
        )
    }
}

@Composable
private fun StatusText(status: org.dhis2.dqapp.UiStatus) {
    val color = when (status.type) {
        StatusType.MUTED -> Muted
        StatusType.SUCCESS -> Color(0xFF067647)
        StatusType.DANGER -> Color(0xFFB42318)
    }
    if (status.message.isNotBlank()) {
        Text(text = status.message, color = color, fontSize = 12.sp)
    }
}

@Composable
private fun <T> DropdownField(
    label: String,
    value: String,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit,
    width: androidx.compose.ui.unit.Dp = 220.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.width(width)) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (labelText, valObj) ->
                DropdownMenuItem(
                    text = { Text(labelText) },
                    onClick = {
                        expanded = false
                        onSelect(valObj)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReportTable(
    state: LqasState?,
    entryOptions: List<String>,
    onEntryChange: (String, String, String) -> Unit,
    onFacilityIncludeChange: (String, Boolean) -> Unit
) {
    if (state == null) {
        Text(text = "No sample loaded yet.", color = Muted)
        return
    }

    if (state.sample.facilities.isEmpty() || state.sample.dataElements.isEmpty()) {
        Text(text = "No sample loaded yet.", color = Muted)
        return
    }

    val hScroll = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ReportHeader(state)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(hScroll)
            ) {
                Column {
                    TableHeaderRow(state)
                    Divider()
                    state.sample.facilities.forEachIndexed { index, ou ->
                        TableDataRow(
                            index = index + 1,
                            facility = ou,
                            dataElements = state.sample.dataElements,
                            entries = state.entries[ou.id].orEmpty(),
                            computed = state.computed.perOu[ou.id],
                            included = state.includeInOverall[ou.id] ?: false,
                            entryOptions = entryOptions,
                            onEntryChange = onEntryChange,
                            onIncludeChange = { onFacilityIncludeChange(ou.id, it) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportHeader(state: LqasState) {
    val overall = state.computed.overall.averageCoveragePct
    val periodLabel = formatPeriodReadable(state.meta.period)
    val updatedLabel = formatDateTime(state.meta.updatedAt)

    Column {
        Text(text = "LQAS Data Accuracy Report", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "District: ${state.meta.district.name}", fontSize = 13.sp, color = Muted)
        Text(text = "Dataset: ${state.meta.dataset.name}", fontSize = 13.sp, color = Muted)
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Badge(text = "Period: ${periodLabel}")
            Badge(text = "Benchmark: ${state.meta.benchmark}%")
            Badge(text = "Updated: ${updatedLabel}")
            if (overall != null) {
                Badge(text = "Average: ${overall}%", accent = true)
            }
        }
    }
}

@Composable
private fun Badge(text: String, accent: Boolean = false) {
    val bg = if (accent) Color(0xFFE6FFFB) else Color(0xFFEEF2FF)
    val fg = if (accent) Color(0xFF115E59) else Color(0xFF1E3A8A)

    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TableHeaderRow(state: LqasState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TableCell(text = "#", width = 40.dp, isHeader = true)
        TableCell(text = "UC / Parent", width = 170.dp, isHeader = true)
        TableCell(text = "Health Facility", width = 200.dp, isHeader = true)
        TableCell(text = "Denominator", width = 110.dp, isHeader = true)
        state.sample.dataElements.forEachIndexed { index, de ->
            TableCell(text = "${index + 1}. ${de.name}", width = 120.dp, isHeader = true)
        }
        TableCell(text = "Total Correct (Y)", width = 120.dp, isHeader = true)
        TableCell(text = "Sample Size", width = 120.dp, isHeader = true)
        TableCell(text = "Coverage %", width = 120.dp, isHeader = true)
        TableCell(text = ">= ${state.meta.benchmark}%", width = 120.dp, isHeader = true)
    }
}

@Composable
private fun TableDataRow(
    index: Int,
    facility: org.dhis2.dqapp.data.Ref,
    dataElements: List<org.dhis2.dqapp.data.Ref>,
    entries: Map<String, String>,
    computed: org.dhis2.dqapp.data.PerOu?,
    included: Boolean,
    entryOptions: List<String>,
    onEntryChange: (String, String, String) -> Unit,
    onIncludeChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TableCell(text = index.toString(), width = 40.dp)
        TableCell(text = facility.parentName ?: "", width = 170.dp)
        TableCell(text = facility.name, width = 200.dp)
        IncludeCell(
            value = included,
            onChange = onIncludeChange
        )
        dataElements.forEach { de ->
            EntryCell(
                value = entries[de.id] ?: "",
                options = entryOptions,
                onChange = { onEntryChange(facility.id, de.id, it) }
            )
        }
        TableCell(text = computed?.correct?.toString() ?: "", width = 120.dp)
        TableCell(text = computed?.sampleSize?.toString() ?: "", width = 120.dp)
        TableCell(text = computed?.coveragePct?.let { "${it}%" } ?: "", width = 120.dp)
        TableCell(text = computed?.let { if (it.benchPass) "Yes" else "No" } ?: "", width = 120.dp)
    }
}

@Composable
private fun IncludeCell(
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Checkbox(
            checked = value,
            onCheckedChange = { onChange(it) }
        )
    }
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false) {
    val weight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            fontSize = 12.sp,
            fontWeight = weight
        )
    }
}

@Composable
private fun EntryCell(
    value: String,
    options: List<String>,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val display = if (value.isBlank()) "" else value

    Box(
        modifier = Modifier
            .width(120.dp)
            .padding(4.dp)
            .clickable { expanded = true }
    ) {
        Surface(
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = display,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                fontSize = 12.sp
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("(empty)") },
                onClick = {
                    expanded = false
                    onChange("")
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onChange(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionLoginOverlay(
    baseUrl: String,
    onCancel: () -> Unit,
    onCookie: (String) -> Unit
) {
    val context = LocalContext.current
    val cookieManager = CookieManager.getInstance()
    var handled by remember { mutableStateOf(false) }
    val url = baseUrl.trimEnd('/')

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xCC0B1526)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1526))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Session Login", color = Color.White, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onCancel) { Text("Close", color = Color.White) }
            }

            AndroidView(
                factory = {
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                val cookie = cookieManager.getCookie(baseUrl)
                                if (!handled && !cookie.isNullOrBlank() && cookie.contains("JSESSIONID")) {
                                    handled = true
                                    onCookie(cookie)
                                }
                            }
                        }
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BusyOverlay(message: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0x800B1526)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "HISP-PAK",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = message,
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatPeriodReadable(period: String): String {
    return when {
        Regex("^\\d{6}$").matches(period) -> "${period.substring(0, 4)}-${period.substring(4, 6)}"
        Regex("^\\d{8}$").matches(period) -> "${period.substring(0, 4)}-${period.substring(4, 6)}-${period.substring(6, 8)}"
        else -> period
    }
}

private fun formatDateTime(iso: String): String {
    return try {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (ex: Exception) {
        iso
    }
}
