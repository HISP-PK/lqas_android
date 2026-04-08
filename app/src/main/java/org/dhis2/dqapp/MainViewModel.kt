package org.dhis2.dqapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.dqapp.data.AppPrefs
import org.dhis2.dqapp.data.Dhis2Client
import org.dhis2.dqapp.data.HttpException
import org.dhis2.dqapp.data.Dhis2Repository
import org.dhis2.dqapp.data.LqasState
import org.dhis2.dqapp.data.Meta
import org.dhis2.dqapp.data.Overall
import org.dhis2.dqapp.data.PerOu
import org.dhis2.dqapp.data.Ref
import org.dhis2.dqapp.data.Sample
import org.dhis2.dqapp.data.clearPrefs
import org.dhis2.dqapp.data.loadPrefs
import org.dhis2.dqapp.data.savePrefs
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val NAMESPACE = "lqas-accuracy"
private const val DEFAULT_DATA_ELEMENT_COUNT = 12
private val ENTRY_OPTIONS = listOf("Y", "N", "S", "X")


data class UiState(
    val connected: Boolean = false,
    val baseUrl: String = "",
    val authMode: AuthMode = AuthMode.BASIC,
    val username: String = "",
    val password: String = "",
    val districtLevel: String = "3",
    val hfLevel: String = "6",
    val dataElementCount: String = DEFAULT_DATA_ELEMENT_COUNT.toString(),
    val periodType: PeriodType = PeriodType.MONTHLY,
    val periodMonth: String = "",
    val periodDay: String = "",
    val periodCustom: String = "",
    val benchmark: String = "95",
    val districts: List<Ref> = emptyList(),
    val datasets: List<Ref> = emptyList(),
    val selectedDistrictId: String = "",
    val selectedDatasetId: String = "",
    val savedKeys: List<String> = emptyList(),
    val selectedSavedKey: String = "",
    val currentState: LqasState? = null,
    val status: UiStatus = UiStatus(),
    val isBusy: Boolean = false,
    val showSessionLogin: Boolean = false,
    val sessionCookie: String = ""
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val context = app.applicationContext
    private var repository: Dhis2Repository? = null
    private var scopePaths: List<String> = emptyList()
    private var isSuperUser: Boolean = false

    var uiState = androidx.compose.runtime.mutableStateOf(UiState())
        private set

    init {
        viewModelScope.launch {
            try {
                val prefs = loadPrefs(context)
                val now = LocalDate.now()
                val month = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val day = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                uiState.value = uiState.value.copy(
                    baseUrl = prefs.baseUrl,
                    authMode = AuthMode.BASIC,
                    username = prefs.username,
                    password = prefs.password,
                    districtLevel = prefs.districtLevel.toString(),
                    hfLevel = prefs.hfLevel.toString(),
                    dataElementCount = prefs.dataElementCount.toString(),
                    periodType = prefs.periodType,
                    periodMonth = month,
                    periodDay = day,
                    connected = false,
                    sessionCookie = ""
                )

            } catch (_: Throwable) {
                try {
                    clearPrefs(context)
                } catch (_: Throwable) {
                }
                val now = LocalDate.now()
                uiState.value = UiState(
                    periodMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    periodDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
            }
        }
    }

    fun updateBaseUrl(value: String) {
        uiState.value = uiState.value.copy(baseUrl = value)
        saveDraftPrefs()
    }

    fun updateAuthMode(value: AuthMode) {
        uiState.value = uiState.value.copy(authMode = value)
    }

    fun updateUsername(value: String) {
        uiState.value = uiState.value.copy(username = value)
        saveDraftPrefs()
    }

    fun updatePassword(value: String) {
        uiState.value = uiState.value.copy(password = value)
        saveDraftPrefs()
    }

    fun updateDistrictLevel(value: String) {
        uiState.value = uiState.value.copy(districtLevel = value)
    }

    fun updateHfLevel(value: String) {
        uiState.value = uiState.value.copy(hfLevel = value)
    }

    fun updateDataElementCount(value: String) {
        uiState.value = uiState.value.copy(dataElementCount = value)
        saveDraftPrefs()
    }

    fun updatePeriodType(value: PeriodType) {
        uiState.value = uiState.value.copy(periodType = value)
    }

    fun updatePeriodMonth(value: String) {
        uiState.value = uiState.value.copy(periodMonth = value)
    }

    fun updatePeriodDay(value: String) {
        uiState.value = uiState.value.copy(periodDay = value)
    }

    fun updatePeriodCustom(value: String) {
        uiState.value = uiState.value.copy(periodCustom = value)
    }

    fun updateBenchmark(value: String) {
        uiState.value = uiState.value.copy(benchmark = value)
    }

    fun updateSelectedDistrict(value: String) {
        uiState.value = uiState.value.copy(selectedDistrictId = value)
    }

    fun updateSelectedDataset(value: String) {
        uiState.value = uiState.value.copy(selectedDatasetId = value)
    }

    fun updateSelectedSavedKey(value: String) {
        uiState.value = uiState.value.copy(selectedSavedKey = value)
    }

    fun setStatus(message: String, type: StatusType = StatusType.MUTED) {
        uiState.value = uiState.value.copy(status = UiStatus(message, type))
    }

    private fun saveDraftPrefs() {
        val s = uiState.value
        viewModelScope.launch {
            try {
                savePrefs(
                    context,
                    AppPrefs(
                        baseUrl = s.baseUrl,
                        authMode = AuthMode.BASIC,
                        username = s.username,
                        password = s.password,
                        districtLevel = s.districtLevel.toIntOrNull() ?: 3,
                        hfLevel = s.hfLevel.toIntOrNull() ?: 6,
                        dataElementCount = s.dataElementCount.toIntOrNull() ?: DEFAULT_DATA_ELEMENT_COUNT,
                        periodType = s.periodType,
                        connected = s.connected,
                        sessionCookie = ""
                    )
                )
            } catch (_: Throwable) {
            }
        }
    }

    private fun buildClient(): Dhis2Client {
        val s = uiState.value
        return Dhis2Client(
            baseUrl = s.baseUrl,
            authMode = s.authMode,
            username = s.username,
            password = s.password,
            sessionCookie = s.sessionCookie
        )
    }

    private fun sanitizeBaseUrl(raw: String): String {
        var value = raw.trim().trimEnd('/')
        if (value.endsWith("/api")) {
            value = value.removeSuffix("/api")
        }
        return value
    }

    fun entryOptions(): List<String> = ENTRY_OPTIONS

    private fun formatPeriodReadable(period: String): String {
        return when {
            Regex("^\\d{6}$").matches(period) -> "${period.substring(0, 4)}-${period.substring(4, 6)}"
            Regex("^\\d{8}$").matches(period) -> "${period.substring(0, 4)}-${period.substring(4, 6)}-${period.substring(6, 8)}"
            else -> period
        }
    }

    private fun filterSavedKeysByDistrictScope(
        keys: List<String>,
        scopeDistrictIds: Set<String>
    ): List<String> {
        if (scopeDistrictIds.isEmpty()) return emptyList()
        return keys.filter { key ->
            val districtId = key.substringBefore("_", "")
            districtId.isNotBlank() && scopeDistrictIds.contains(districtId)
        }
    }

    fun connect(restoreOnly: Boolean = false) {
        val s0 = uiState.value
        val trimmedBaseUrl = sanitizeBaseUrl(s0.baseUrl)
        if (trimmedBaseUrl.isBlank()) {
            setStatus("Base URL is required.", StatusType.DANGER)
            return
        }
        if (trimmedBaseUrl != s0.baseUrl) {
            uiState.value = s0.copy(baseUrl = trimmedBaseUrl)
        }
        val s = uiState.value

        if (s.authMode == AuthMode.BASIC) {
            if (s.username.isBlank() || s.password.isBlank()) {
                setStatus("Username and password are required for Basic Auth.", StatusType.DANGER)
                return
            }
        }

        if (s.authMode == AuthMode.SESSION && s.sessionCookie.isBlank() && restoreOnly) {
            setStatus("Session cookie missing. Please login again.", StatusType.DANGER)
            uiState.value = s.copy(connected = false)
            return
        }

        if (s.authMode == AuthMode.SESSION && s.sessionCookie.isBlank() && !restoreOnly) {
            uiState.value = s.copy(showSessionLogin = true)
            return
        }

        val districtLevel = s.districtLevel.toIntOrNull()
        if (districtLevel == null || districtLevel < 1) {
            setStatus("District level must be a positive number.", StatusType.DANGER)
            return
        }
        val hfLevel = s.hfLevel.toIntOrNull() ?: 6

        viewModelScope.launch {
            uiState.value = uiState.value.copy(isBusy = true)
            try {
                setStatus("Loading districts and datasets...")
                repository = Dhis2Repository(buildClient())

                val repo = repository ?: return@launch
                val userScope = repo.loadUserScope()
                scopePaths = userScope.scopePaths
                isSuperUser = userScope.isSuperUser

                // Keep calls sequential to avoid unhandled sibling-coroutine failures
                // when auth/network errors happen simultaneously.
                val districtsDto = repo.loadDistricts(
                    level = districtLevel,
                    scopePaths = scopePaths,
                    isSuperUser = isSuperUser
                )
                val datasetsDto = repo.loadDatasets()

                if (districtsDto.isEmpty()) {
                    setStatus("No districts found at level=$districtLevel. Adjust District Level.", StatusType.DANGER)
                    uiState.value = uiState.value.copy(isBusy = false)
                    return@launch
                }
                if (datasetsDto.isEmpty()) {
                    setStatus("No datasets found.", StatusType.DANGER)
                    uiState.value = uiState.value.copy(isBusy = false)
                    return@launch
                }

                val districts = districtsDto.map { Ref(it.id, it.name) }
                val datasets = datasetsDto.map { Ref(it.id, it.name) }
                val districtScopeIds = districts.map { it.id }.toSet()

                val savedKeys = filterSavedKeysByDistrictScope(
                    repo.loadDataStoreKeys(NAMESPACE),
                    districtScopeIds
                )

                uiState.value = uiState.value.copy(
                    connected = true,
                    districts = districts,
                    datasets = datasets,
                    selectedDistrictId = districts.firstOrNull()?.id ?: "",
                    selectedDatasetId = datasets.firstOrNull()?.id ?: "",
                    savedKeys = savedKeys,
                    isBusy = false
                )

                try {
                    savePrefs(
                        context,
                        AppPrefs(
                            baseUrl = s.baseUrl,
                            authMode = s.authMode,
                            username = s.username,
                            password = s.password,
                            districtLevel = districtLevel,
                            hfLevel = hfLevel,
                            dataElementCount = s.dataElementCount.toIntOrNull() ?: DEFAULT_DATA_ELEMENT_COUNT,
                            periodType = s.periodType,
                            connected = true,
                            sessionCookie = s.sessionCookie
                        )
                    )
                } catch (_: Throwable) {
                }

                setStatus("Ready.")
            } catch (ex: Throwable) {
                repository = null
                val fallback = ex.message ?: "Connection failed."
                val message = when (ex) {
                    is HttpException -> {
                        when (ex.code) {
                            401, 403 -> "Invalid username or password."
                            else -> "Connection failed (HTTP ${ex.code})."
                        }
                    }
                    else -> {
                        // Common with DHIS2 login HTML responses when auth is invalid.
                        if (fallback.contains("Unexpected JSON token", ignoreCase = true)) {
                            "Invalid username or password."
                        } else {
                            fallback
                        }
                    }
                }

                uiState.value = uiState.value.copy(
                    connected = false,
                    showSessionLogin = false
                )
                scopePaths = emptyList()
                isSuperUser = false
                setStatus(message, StatusType.DANGER)

                try {
                    savePrefs(
                        context,
                        AppPrefs(
                            baseUrl = s.baseUrl,
                            authMode = s.authMode,
                            username = s.username,
                            password = s.password,
                            districtLevel = districtLevel,
                            hfLevel = hfLevel,
                            dataElementCount = s.dataElementCount.toIntOrNull() ?: DEFAULT_DATA_ELEMENT_COUNT,
                            periodType = s.periodType,
                            connected = false,
                            sessionCookie = ""
                        )
                    )
                } catch (_: Throwable) {
                }
            } finally {
                uiState.value = uiState.value.copy(isBusy = false)
            }
        }
    }

    fun onSessionCookie(cookie: String) {
        uiState.value = uiState.value.copy(sessionCookie = cookie, showSessionLogin = false)
        connect()
    }

    fun cancelSessionLogin() {
        uiState.value = uiState.value.copy(showSessionLogin = false)
    }

    fun logout() {
        viewModelScope.launch {
            clearPrefs(context)
            repository = null
            scopePaths = emptyList()
            isSuperUser = false
            uiState.value = UiState()
            val now = LocalDate.now()
            uiState.value = uiState.value.copy(
                periodMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                periodDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
            setStatus("Logged out.")
        }
    }

    fun refreshSaved(showStatus: Boolean = true) {
        val repo = repository ?: return
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isBusy = true)
            try {
                val districtScopeIds = uiState.value.districts.map { it.id }.toSet()
                val keys = filterSavedKeysByDistrictScope(
                    repo.loadDataStoreKeys(NAMESPACE),
                    districtScopeIds
                )
                uiState.value = uiState.value.copy(savedKeys = keys)
                if (showStatus) {
                    setStatus("Saved records refreshed.", StatusType.SUCCESS)
                }
            } catch (ex: Exception) {
                setStatus(ex.message ?: "Failed to load saved records.", StatusType.DANGER)
            } finally {
                uiState.value = uiState.value.copy(isBusy = false)
            }
        }
    }

    fun loadSaved() {
        val repo = repository ?: return
        val key = uiState.value.selectedSavedKey
        if (key.isBlank()) {
            setStatus("Select a saved record.", StatusType.DANGER)
            return
        }
        if (!uiState.value.savedKeys.contains(key)) {
            setStatus("Selected record is outside your scope.", StatusType.DANGER)
            return
        }

        viewModelScope.launch {
            uiState.value = uiState.value.copy(isBusy = true)
            try {
                val existing = repo.loadDataStoreState(NAMESPACE, key)
                if (existing == null) {
                    setStatus("Saved record not found.", StatusType.DANGER)
                    return@launch
                }
                if (uiState.value.districts.none { it.id == existing.meta.district.id }) {
                    setStatus("Saved record is outside your scope.", StatusType.DANGER)
                    return@launch
                }

                val s = uiState.value
                val period = existing.meta.period
                val updatedUi = s.copy(
                    currentState = existing,
                    selectedDistrictId = existing.meta.district.id,
                    selectedDatasetId = existing.meta.dataset.id,
                    periodMonth = if (s.periodType == PeriodType.MONTHLY && period.length == 6) {
                        "${period.substring(0, 4)}-${period.substring(4, 6)}"
                    } else s.periodMonth,
                    periodDay = if (s.periodType == PeriodType.DAILY && period.length == 8) {
                        "${period.substring(0, 4)}-${period.substring(4, 6)}-${period.substring(6, 8)}"
                    } else s.periodDay,
                    periodCustom = if (s.periodType == PeriodType.CUSTOM) period else s.periodCustom
                )

                uiState.value = updatedUi
                setStatus("Loaded saved record.", StatusType.SUCCESS)
            } catch (ex: Exception) {
                setStatus(ex.message ?: "Failed to load saved record.", StatusType.DANGER)
            } finally {
                uiState.value = uiState.value.copy(isBusy = false)
            }
        }
    }

    private fun periodToId(): String? {
        val s = uiState.value
        return when (s.periodType) {
            PeriodType.MONTHLY -> {
                val v = s.periodMonth.trim()
                if (Regex("^\\d{4}-\\d{2}$").matches(v)) v.replace("-", "") else null
            }
            PeriodType.DAILY -> {
                val v = s.periodDay.trim()
                if (Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(v)) v.replace("-", "") else null
            }
            PeriodType.CUSTOM -> {
                val v = s.periodCustom.trim()
                if (v.isNotEmpty()) v else null
            }
        }
    }

    private fun makeKey(districtId: String, period: String, datasetId: String): String {
        return "${districtId}_${period}_${datasetId}"
    }

    fun loadOrGenerate() {
        val repo = repository ?: return
        val s = uiState.value
        val districtId = s.selectedDistrictId
        val datasetId = s.selectedDatasetId
        val period = periodToId()
        val benchmark = s.benchmark.toIntOrNull() ?: 95
        val dataElementCount = s.dataElementCount.toIntOrNull()

        if (districtId.isBlank() || datasetId.isBlank() || period == null) {
            setStatus("Please select District, Period, and Dataset.", StatusType.DANGER)
            return
        }
        if (s.districts.none { it.id == districtId }) {
            setStatus("Selected district is outside your scope.", StatusType.DANGER)
            return
        }
        if (dataElementCount == null || dataElementCount < 1) {
            setStatus("Data elements count must be a positive number.", StatusType.DANGER)
            return
        }

        val key = makeKey(districtId, period, datasetId)

        viewModelScope.launch {
            uiState.value = uiState.value.copy(isBusy = true)
            try {
                setStatus("Loading facilities and dataset elements...")

                val districtName = s.districts.find { it.id == districtId }?.name ?: districtId
                val datasetName = s.datasets.find { it.id == datasetId }?.name ?: datasetId

                val districtRef = Ref(districtId, districtName)
                val datasetRef = Ref(datasetId, datasetName)

                val facilitiesAll = repo.loadFacilitiesUnderDistrict(
                    districtId = districtId,
                    scopePaths = scopePaths,
                    isSuperUser = isSuperUser
                )
                if (facilitiesAll.isEmpty()) {
                    setStatus("No health facilities found in your scope for this district/level.", StatusType.DANGER)
                    return@launch
                }

                val desAll = repo.loadDataElementsFromDataset(datasetId)
                if (desAll.isEmpty()) {
                    setStatus("No data elements found in selected dataset.", StatusType.DANGER)
                    return@launch
                }

                val takeCount = minOf(dataElementCount, desAll.size)
                val facilities = facilitiesAll.map { Ref(it.id, it.name, it.parent?.name) }
                val dataElements = desAll.shuffled().take(takeCount).map { Ref(it.id, it.name) }
                val includeInOverall = facilities.associate { it.id to false }

                setStatus("Checking saved record in dataStore...")
                val existing = repo.loadDataStoreState(NAMESPACE, key)
                if (existing != null) {
                    val existingFacilityIds = existing.sample.facilities.map { it.id }.sorted()
                    val currentFacilityIds = facilities.map { it.id }.sorted()
                    val existingDeCount = existing.sample.dataElements.size
                    if (existing.meta.district.id == districtId &&
                        existingFacilityIds == currentFacilityIds &&
                        existingDeCount == takeCount
                    ) {
                        uiState.value = s.copy(
                            currentState = existing,
                            selectedDistrictId = existing.meta.district.id,
                            selectedDatasetId = existing.meta.dataset.id
                        )
                        setStatus("Loaded saved sample and results from dataStore.", StatusType.SUCCESS)
                        return@launch
                    }
                }

                val now = java.time.Instant.now().toString()
                val entries = facilities.associate { it.id to emptyMap<String, String>() }

                val state = LqasState(
                    meta = Meta(
                        district = districtRef,
                        period = period,
                        dataset = datasetRef,
                        benchmark = benchmark,
                        createdAt = now,
                        updatedAt = now
                    ),
                    sample = Sample(facilities = facilities, dataElements = dataElements),
                    entries = entries,
                    includeInOverall = includeInOverall
                )

                uiState.value = s.copy(currentState = state)
                val deNote = if (takeCount < dataElementCount) {
                    " Requested $dataElementCount, available ${desAll.size}."
                } else {
                    ""
                }
                setStatus(
                    "Loaded ${facilities.size} facilities with $takeCount random data elements.$deNote",
                    StatusType.SUCCESS
                )
            } catch (ex: Exception) {
                setStatus(ex.message ?: "Failed to load sample.", StatusType.DANGER)
            } finally {
                uiState.value = uiState.value.copy(isBusy = false)
            }
        }
    }

    fun calculate() {
        val s = uiState.value
        val state = s.currentState ?: run {
            setStatus("Load/Generate first.", StatusType.DANGER)
            return
        }

        val computed = calculateState(state)
        uiState.value = s.copy(currentState = computed)
        setStatus("Calculated results (not saved yet).", StatusType.SUCCESS)
    }

    fun save() {
        val repo = repository ?: return
        val s = uiState.value
        val state = s.currentState ?: run {
            setStatus("Load/Generate first.", StatusType.DANGER)
            return
        }

        viewModelScope.launch {
            uiState.value = uiState.value.copy(isBusy = true)
            try {
                val computed = calculateState(state)
                val districtId = computed.meta.district.id
                val period = computed.meta.period
                val datasetId = computed.meta.dataset.id
                val key = makeKey(districtId, period, datasetId)

                repo.saveDataStoreState(NAMESPACE, key, computed)

                uiState.value = s.copy(currentState = computed)
                setStatus("Saved to dataStore.", StatusType.SUCCESS)
                refreshSaved(showStatus = false)
            } catch (ex: Exception) {
                setStatus(ex.message ?: "Save failed.", StatusType.DANGER)
            } finally {
                uiState.value = uiState.value.copy(isBusy = false)
            }
        }
    }

    fun updateEntry(ouId: String, deId: String, value: String) {
        val s = uiState.value
        val state = s.currentState ?: return

        val newEntries = state.entries.toMutableMap()
        val row = newEntries[ouId]?.toMutableMap() ?: mutableMapOf()

        if (value.isBlank()) {
            row.remove(deId)
        } else {
            row[deId] = value
        }

        newEntries[ouId] = row

        val updated = state.copy(
            entries = newEntries,
            meta = state.meta.copy(updatedAt = java.time.Instant.now().toString())
        )

        uiState.value = s.copy(currentState = updated)
    }

    fun updateFacilityIncluded(ouId: String, included: Boolean) {
        val s = uiState.value
        val state = s.currentState ?: return
        val map = state.includeInOverall.toMutableMap()
        map[ouId] = included
        val updated = state.copy(
            includeInOverall = map,
            meta = state.meta.copy(updatedAt = java.time.Instant.now().toString())
        )
        uiState.value = s.copy(currentState = updated)
    }

    private fun calculateState(state: LqasState): LqasState {
        val benchmark = state.meta.benchmark
        var sumCoverage = 0
        var countCoverage = 0
        val perOu = mutableMapOf<String, PerOu>()

        for (ou in state.sample.facilities) {
            var correct = 0
            var sampleSize = 0

            for (de in state.sample.dataElements) {
                val v = state.entries[ou.id]?.get(de.id) ?: ""
                if (v == "X" || v.isBlank()) continue
                sampleSize += 1
                if (v == "Y") correct += 1
            }

            val coverage = if (sampleSize == 0) 0 else (correct.toDouble() / sampleSize.toDouble() * 100.0).roundToInt()
            val pass = coverage >= benchmark
            perOu[ou.id] = PerOu(correct, sampleSize, coverage, pass)

            val include = state.includeInOverall[ou.id] ?: false
            if (include) {
                sumCoverage += coverage
                countCoverage += 1
            }
        }

        val overallAvg = if (countCoverage == 0) null else (sumCoverage.toDouble() / countCoverage.toDouble()).roundToInt()
        val computed = state.copy(
            computed = state.computed.copy(
                perOu = perOu,
                overall = Overall(overallAvg)
            ),
            meta = state.meta.copy(updatedAt = java.time.Instant.now().toString())
        )

        return computed
    }

    fun formatSavedKey(key: String): String {
        val parts = key.split("_")
        if (parts.size < 3) return key
        val districtId = parts[0]
        val period = parts[1]
        val datasetId = parts.drop(2).joinToString("_")
        val districtName = uiState.value.districts.find { it.id == districtId }?.name ?: districtId
        val datasetName = uiState.value.datasets.find { it.id == datasetId }?.name ?: datasetId
        return "${districtName} | ${period} | ${datasetName}"
    }

    fun buildReportHtml(): String? {
        val state = uiState.value.currentState ?: return null
        val facilities = state.sample.facilities
        val des = state.sample.dataElements
        if (facilities.isEmpty() || des.isEmpty()) return null

        val benchmark = state.meta.benchmark
        val periodLabel = formatPeriodReadable(state.meta.period)
        val updated = state.meta.updatedAt
        val overall = state.computed.overall.averageCoveragePct

        fun esc(s: String): String {
            return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;")
        }

        val header = StringBuilder()
        header.append("<div class='report-header'>")
        header.append("<div>")
        header.append("<div class='report-title'>LQAS Data Accuracy Report</div>")
        header.append("<div class='report-subtitle'>District: <b>${esc(state.meta.district.name)}</b></div>")
        header.append("<div class='report-subtitle'>Dataset: <b>${esc(state.meta.dataset.name)}</b></div>")
        header.append("</div>")
        header.append("<div class='report-badges'>")
        header.append("<span class='badge'>Period: ${esc(periodLabel)}</span>")
        header.append("<span class='badge'>Benchmark: ${benchmark}%</span>")
        header.append("<span class='badge'>Updated: ${esc(updated)}</span>")
        if (overall != null) {
            header.append("<span class='badge accent'>Average: ${overall}%</span>")
        }
        header.append("</div>")
        header.append("</div>")

        val table = StringBuilder()
        table.append("<table><thead><tr>")
        table.append("<th class='left'>#</th>")
        table.append("<th class='left'>UC / Parent</th>")
        table.append("<th class='left'>Health Facility</th>")
        table.append("<th>In Average</th>")
        des.forEachIndexed { idx, de ->
            table.append("<th>${idx + 1}. ${esc(de.name)}</th>")
        }
        table.append("<th>Total Correct (Y)</th>")
        table.append("<th>Sample Size</th>")
        table.append("<th>Coverage %</th>")
        table.append("<th>>= ${benchmark}%</th>")
        table.append("</tr></thead><tbody>")

        facilities.forEachIndexed { index, ou ->
            table.append("<tr>")
            table.append("<td class='left'>${index + 1}</td>")
            table.append("<td class='left'>${esc(ou.parentName ?: "")}</td>")
            table.append("<td class='left'>${esc(ou.name)}</td>")
            val include = state.includeInOverall[ou.id] ?: false
            table.append("<td>${if (include) "Yes" else "No"}</td>")
            des.forEach { de ->
                val value = state.entries[ou.id]?.get(de.id) ?: ""
                table.append("<td>${esc(value)}</td>")
            }
            val c = state.computed.perOu[ou.id]
            table.append("<td>${c?.correct ?: ""}</td>")
            table.append("<td>${c?.sampleSize ?: ""}</td>")
            table.append("<td>${c?.coveragePct?.let { "${it}%" } ?: ""}</td>")
            table.append("<td>${if (c == null) "" else if (c.benchPass) "Yes" else "No"}</td>")
            table.append("</tr>")
        }
        table.append("</tbody></table>")
        table.append("<div style='margin-top:10px' class='report-subtitle'>Overall Average Coverage: <b>${overall?.let { it.toString() + "%" } ?: "N/A"}</b></div>")

        val css = """
            html, body { margin: 0; padding: 10px; }
            body { font-family: sans-serif; color: #0b1526; }
            .report-header { display:flex; gap:16px; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; margin-bottom: 10px; }
            .report-title { font-size: 18px; font-weight: 700; }
            .report-subtitle { color: #5c6b82; font-size: 13px; margin-top: 3px; }
            .report-badges { display:flex; gap:8px; flex-wrap: wrap; }
            .badge { padding: 4px 8px; border-radius: 999px; background: #eef2ff; color: #1e3a8a; font-size: 12px; }
            .badge.accent { background: #e6fffb; color: #115e59; }
            table { border-collapse: collapse; width: 100%; margin-top: 8px; }
            th, td { border: 1px solid #d9e2f0; padding: 6px; text-align: center; font-size: 11px; vertical-align: top; }
            th { background: #f3f6fb; }
            td.left, th.left { text-align: left; }
            tr:nth-child(even) td { background: #f8fafc; }
        """.trimIndent()

        return """
            <!doctype html>
            <html>
            <head>
              <meta charset='utf-8'/>
              <title>LQAS Data Accuracy Report</title>
              <style>$css</style>
            </head>
            <body>
              ${header}
              ${table}
            </body>
            </html>
        """.trimIndent()
    }

    fun buildReportCsv(): String? {
        val state = uiState.value.currentState ?: return null
        val facilities = state.sample.facilities
        val des = state.sample.dataElements
        if (facilities.isEmpty() || des.isEmpty()) return null

        fun csv(value: String): String {
            val escaped = value.replace("\"", "\"\"")
            return "\"$escaped\""
        }

        val lines = mutableListOf<String>()
        val periodLabel = formatPeriodReadable(state.meta.period)
        val overall = state.computed.overall.averageCoveragePct
        lines += listOf(csv("District"), csv(state.meta.district.name)).joinToString(",")
        lines += listOf(csv("Dataset"), csv(state.meta.dataset.name)).joinToString(",")
        lines += listOf(csv("Period"), csv(periodLabel)).joinToString(",")
        lines += listOf(csv("Benchmark"), csv("${state.meta.benchmark}%")).joinToString(",")
        lines += listOf(csv("Updated"), csv(state.meta.updatedAt)).joinToString(",")
        lines += listOf(csv("Overall Average"), csv(overall?.let { "$it%" } ?: "N/A")).joinToString(",")
        lines += ""

        val header = mutableListOf<String>()
        header += csv("#")
        header += csv("UC / Parent")
        header += csv("Health Facility")
        header += csv("In Average")
        des.forEachIndexed { idx, de -> header += csv("${idx + 1}. ${de.name}") }
        header += csv("Total Correct (Y)")
        header += csv("Sample Size")
        header += csv("Coverage %")
        header += csv(">= ${state.meta.benchmark}%")
        lines += header.joinToString(",")

        facilities.forEachIndexed { index, ou ->
            val row = mutableListOf<String>()
            row += csv((index + 1).toString())
            row += csv(ou.parentName ?: "")
            row += csv(ou.name)
            val include = state.includeInOverall[ou.id] ?: false
            row += csv(if (include) "Yes" else "No")
            des.forEach { de ->
                row += csv(state.entries[ou.id]?.get(de.id) ?: "")
            }
            val c = state.computed.perOu[ou.id]
            row += csv(c?.correct?.toString() ?: "")
            row += csv(c?.sampleSize?.toString() ?: "")
            row += csv(c?.coveragePct?.let { "$it%" } ?: "")
            row += csv(if (c == null) "" else if (c.benchPass) "Yes" else "No")
            lines += row.joinToString(",")
        }

        return lines.joinToString("\n")
    }
}
