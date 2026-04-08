package org.dhis2.dqapp.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class UserScope(
    val isSuperUser: Boolean,
    val scopePaths: List<String>
)

class Dhis2Repository(private val client: Dhis2Client) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadUserScope(): UserScope {
        val path = "/api/me.json?fields=authorities,organisationUnits[id],dataViewOrganisationUnits[id]"
        val body = client.get(path)
        val me = json.decodeFromString<MeResponse>(body)
        // Prefer assigned org units for tighter restriction.
        val sourceScope = if (me.organisationUnits.isNotEmpty()) {
            me.organisationUnits
        } else if (me.dataViewOrganisationUnits.isNotEmpty()) {
            me.dataViewOrganisationUnits
        } else {
            emptyList()
        }
        val paths = sourceScope.mapNotNull { ou ->
            runCatching { loadOrgUnitPath(ou.id) }.getOrNull()?.takeIf { it.isNotBlank() }
        }.distinct()
        val isSuper = me.authorities.contains("ALL")
        return UserScope(isSuperUser = isSuper, scopePaths = paths)
    }

    private fun isParentOrInScope(unitPath: String?, scopePaths: List<String>): Boolean {
        if (unitPath.isNullOrBlank()) return false
        return scopePaths.any { scope ->
            unitPath.startsWith(scope) || scope.startsWith(unitPath)
        }
    }

    private fun isInScope(unitPath: String?, scopePaths: List<String>): Boolean {
        if (unitPath.isNullOrBlank()) return false
        return scopePaths.any { scope -> unitPath.startsWith(scope) }
    }

    suspend fun loadDistricts(level: Int, scopePaths: List<String>, isSuperUser: Boolean): List<OrgUnitDto> {
        val path = "/api/organisationUnits.json?paging=false&fields=id,name,level,path&filter=level:eq:${level}&order=name:asc"
        val body = client.get(path)
        val units = json.decodeFromString<OrgUnitResponse>(body).organisationUnits
        return if (isSuperUser || scopePaths.isEmpty()) {
            if (isSuperUser) units else emptyList()
        } else {
            units.filter { isParentOrInScope(it.path, scopePaths) }
        }
    }

    suspend fun loadDatasets(): List<DataSetDto> {
        val path = "/api/dataSets.json?paging=false&fields=id,name&order=name:asc"
        val body = client.get(path)
        return json.decodeFromString<DataSetResponse>(body).dataSets
    }

    suspend fun loadOrgUnitPath(id: String): String {
        val path = "/api/organisationUnits/${id}.json?fields=id,name,path"
        val body = client.get(path)
        val dto = json.decodeFromString<OrgUnitDto>(body)
        return dto.path ?: ""
    }

    suspend fun loadFacilitiesUnderDistrict(
        districtId: String,
        scopePaths: List<String>,
        isSuperUser: Boolean
    ): List<OrgUnitDto> {
        val path = loadOrgUnitPath(districtId)
        val query = "/api/organisationUnits.json?paging=false&fields=id,name,level,path,parent[id,name]&filter=path:like:${path}&filter=leaf:eq:true&order=name:asc"
        val body = client.get(query)
        val units = json.decodeFromString<OrgUnitResponse>(body).organisationUnits
        return if (isSuperUser || scopePaths.isEmpty()) {
            if (isSuperUser) units else emptyList()
        } else {
            units.filter { isInScope(it.path, scopePaths) }
        }
    }

    suspend fun loadDataElementsFromDataset(datasetId: String): List<DataElementDto> {
        val path = "/api/dataSets/${datasetId}.json?fields=id,name,dataSetElements[dataElement[id,name,valueType]]"
        val body = client.get(path)
        val dto = json.decodeFromString<DataSetDetailDto>(body)
        return dto.dataSetElements.mapNotNull { it.dataElement }
    }

    suspend fun loadDataStoreKeys(namespace: String): List<String> {
        val path = "/api/dataStore/${namespace}"
        val body = client.getMaybe(path) ?: return emptyList()
        return json.decodeFromString<List<String>>(body)
    }

    suspend fun loadDataStoreState(namespace: String, key: String): LqasState? {
        val path = "/api/dataStore/${namespace}/${key}"
        val body = client.getMaybe(path) ?: return null
        return json.decodeFromString(body)
    }

    suspend fun saveDataStoreState(namespace: String, key: String, state: LqasState) {
        val body = json.encodeToString(state)
        val path = "/api/dataStore/${namespace}/${key}"
        try {
            client.put(path, body)
        } catch (ex: HttpException) {
            if (ex.code == 404) {
                client.post(path, body)
            } else {
                throw ex
            }
        }
    }
}
