package com.github.mikeandv.pingwatch.ui.utils

sealed class ParallelismInputResult {
    data class Empty(val value: Int = 0) : ParallelismInputResult()
    data class Valid(val value: Int) : ParallelismInputResult()
    data class Error(val message: String) : ParallelismInputResult()
}

