package com.github.mikeandv.pingwatch.entity

data class LaunchValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
