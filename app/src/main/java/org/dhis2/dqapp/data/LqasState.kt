package org.dhis2.dqapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Ref(
    val id: String,
    val name: String,
    val parentName: String? = null
)

@Serializable
data class Meta(
    val district: Ref,
    val period: String,
    val dataset: Ref,
    val benchmark: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Sample(
    val facilities: List<Ref> = emptyList(),
    val dataElements: List<Ref> = emptyList()
)

@Serializable
data class PerOu(
    val correct: Int,
    val sampleSize: Int,
    val coveragePct: Int,
    val benchPass: Boolean
)

@Serializable
data class Overall(
    val averageCoveragePct: Int? = null
)

@Serializable
data class Computed(
    val perOu: Map<String, PerOu> = emptyMap(),
    val overall: Overall = Overall()
)

@Serializable
data class LqasState(
    val meta: Meta,
    val sample: Sample,
    val entries: Map<String, Map<String, String>> = emptyMap(),
    val includeInOverall: Map<String, Boolean> = emptyMap(),
    val computed: Computed = Computed()
)
