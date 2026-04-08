package org.dhis2.dqapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrgUnitDto(
    val id: String,
    val name: String,
    val level: Int? = null,
    val path: String? = null,
    val parent: OrgUnitParentDto? = null
)

@Serializable
data class OrgUnitParentDto(
    val id: String,
    val name: String? = null
)

@Serializable
data class OrgUnitResponse(
    @SerialName("organisationUnits") val organisationUnits: List<OrgUnitDto> = emptyList()
)

@Serializable
data class DataSetDto(
    val id: String,
    val name: String
)

@Serializable
data class DataSetResponse(
    @SerialName("dataSets") val dataSets: List<DataSetDto> = emptyList()
)

@Serializable
data class DataSetDetailDto(
    val id: String,
    val name: String,
    @SerialName("dataSetElements") val dataSetElements: List<DataSetElementDto> = emptyList()
)

@Serializable
data class DataSetElementDto(
    val dataElement: DataElementDto? = null
)

@Serializable
data class DataElementDto(
    val id: String,
    val name: String,
    val valueType: String? = null
)

@Serializable
data class MeResponse(
    val authorities: List<String> = emptyList(),
    @SerialName("organisationUnits") val organisationUnits: List<MeOrgUnitDto> = emptyList(),
    @SerialName("dataViewOrganisationUnits") val dataViewOrganisationUnits: List<MeOrgUnitDto> = emptyList()
)

@Serializable
data class MeOrgUnitDto(
    val id: String,
    val name: String? = null,
    val path: String? = null,
    val level: Int? = null
)
