package com.aetherglass.util

/**
 * Compatibility aliases for the project-wide result type.
 *
 * All layers must share the same result implementation so repositories,
 * use cases, and Compose screens can exchange success/failure values safely.
 */
typealias Result<T> = com.aetherglass.core.util.Result<T>
typealias Error = com.aetherglass.core.util.Error

inline fun <T> runCatching(block: () -> T): Result<T> =
    com.aetherglass.core.util.runCatching(block)
