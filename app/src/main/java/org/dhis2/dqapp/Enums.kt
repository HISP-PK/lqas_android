package org.dhis2.dqapp

enum class AuthMode {
    SESSION,
    BASIC
}

enum class PeriodType {
    MONTHLY,
    DAILY,
    CUSTOM
}

enum class StatusType {
    MUTED,
    SUCCESS,
    DANGER
}

data class UiStatus(
    val message: String = "",
    val type: StatusType = StatusType.MUTED
)
