package com.github.mikeandv.pingwatch.entity

sealed class ParallelismInputResult {
    data class Valid(val value: Int) : ParallelismInputResult()
    data class Error(val message: String) : ParallelismInputResult()
}
