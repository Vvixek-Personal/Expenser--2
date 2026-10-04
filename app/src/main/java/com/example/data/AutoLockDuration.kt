package com.example.data

enum class AutoLockDuration(val label: String, val millis: Long) {
    IMMEDIATE("Immediately", 0L),
    FIVE_SECONDS("5 seconds", 5_000L),
    THIRTY_SECONDS("30 seconds", 30_000L),
    ONE_MINUTE("1 minute", 60_000L),
    FIVE_MINUTES("5 minutes", 300_000L),
    TEN_MINUTES("10 minutes", 600_000L),
    NEVER("Never", -1L);

    val shouldLock: Boolean get() = this != NEVER

    companion object {
        fun fromString(name: String?): AutoLockDuration {
            return values().firstOrNull { it.name == name } ?: NEVER
        }
    }
}
