package com.aetherglass.core.util

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Result as KotlinResult
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.functions.Function0
import kotlin.jvm.functions.Function1
import timber.log.Timber

/**
 * Result type for operations that can succeed or fail.
 * Used instead of exceptions for expected error conditions.
 */
@kotlinx.serialization.Serializable
sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val error: Error) : Result<Nothing>()

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun <T> failure(error: Error): Result<T> = Failure(error)
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    val value: T?
        get() = if (this is Success) value else null

    val error: Error?
        get() = if (this is Failure) error else null

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(value)
        return this
    }

    fun onFailure(action: (Error) -> Unit): Result<T> {
        if (this is Failure) action(error!!)
        return this
    }

    fun getOrDefault(default: T): T = value ?: default

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error!!.toException()
    }
}

data class Error(
    val code: String,
    val message: String,
    val cause: Throwable? = null,
    val recoverable: Boolean = true
) {
    fun toException(): Exception = cause ?: Exception(message)
}

/**
 * Run a block and catch exceptions as Result.
 */
inline fun <T> runCatching(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(Error("EXCEPTION", e.message ?: e.javaClass.simpleName, e))
    }
}

/**
 * Safely launch a coroutine with exception handling.
 */
fun CoroutineScope.safeLaunch(
    context: CoroutineContext = kotlinx.coroutines.Dispatchers.Default,
    onError: (Throwable) -> Unit = { Timber.tag("Coroutine").e(it, "Uncaught coroutine exception") }
): Job {
    return launch(context) {
        try {
            // Override in subclass
        } catch (e: Exception) {
            onError(e)
        }
    }
}

/**
 * Main-safe coroutine launch for UI operations.
 */
fun CoroutineScope.launchUi(
    onError: (Throwable) -> Unit = { Timber.tag("UI").e(it, "UI coroutine exception") }
): Job {
    return launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
        try {
            // Override in subclass
        } catch (e: Exception) {
            onError(e)
        }
    }
}

/**
 * Remember a mutable state with a custom equality check.
 */
@Composable
fun <T> rememberMutableState(
    initialValue: T,
    areEqual: (T, T) -> Boolean = { a, b -> a == b }
): androidx.compose.runtime.MutableState<T> {
    var value by remember { mutableStateOf(initialValue) }
    return androidx.compose.runtime.mutableStateOf(value, areEqual)
}

/**
 * Debounced state - only updates after delay without new values.
 */
@Composable
fun <T> rememberDebouncedState(
    initialValue: T,
    delayMs: Long = 300
): androidx.compose.runtime.MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }
    val pendingValue = remember { mutableStateOf<T?>(null) }
    val job = remember { SupervisorJob() }
    val scope = remember { CoroutineScope(job + kotlinx.coroutines.Dispatchers.Main) }

    // Cleanup on composition end
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { job.cancel() }
    }

    return object : androidx.compose.runtime.MutableState<T> {
        override var value: T = initialValue
            set(value) {
                field = value
                pendingValue.value = null
                job.cancel()
                scope.launch {
                    kotlinx.coroutines.delay(delayMs)
                    if (pendingValue.value != null) {
                        field = pendingValue.value!!
                        pendingValue.value = null
                    }
                }
            }

        override fun component1(): T = value

        override fun component2(): (T) -> Unit = { value = it }
    }
}

/**
 * Format bytes as human-readable string.
 */
fun Long.formatBytes(): String {
    if (this < 1024) return "$this B"
    val exp = (kotlin.math.log(this.toDouble()) / kotlin.math.log(1024.0)).toInt()
    val unit = "KMGTPE".charAt(exp - 1)
    val value = this / 1024.0.pow(exp)
    return "%.1f %ciB".format(value, unit)
}

/**
 * Format duration as human-readable string.
 */
fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

/**
 * Format percentage with consistent decimal places.
 */
fun Double.formatPercent(decimals: Int = 1): String {
    return "%.${decimals}f%%".format(this)
}

/**
 * Format timestamp as relative time.
 */
fun Long.formatRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(this))
    }
}

/**
 * Format timestamp as absolute time.
 */
fun Long.formatAbsoluteTime(): String {
    return java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(this))
}

/**
 * Format timestamp as absolute date.
 */
fun Long.formatAbsoluteDate(): String {
    return java.text.SimpleDateFormat("MMM d, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(this))
}

/**
 * Clamp a value between min and max.
 */
fun <T : Comparable<T>> T.clamp(min: T, max: T): T {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

/**
 * Convert dp to px.
 */
fun Int.dpToPx(density: Float = androidx.core.content.res.ResourcesCompat.getFont(androidx.core.content.res.ResourcesCompat.getSystem(), 0)?.let { 1f } ?: 1f): Int {
    return (this * density).toInt()
}

/**
 * Safe nullable string operations.
 */
fun String?.orEmpty(): String = this ?: ""
fun String?.isNullOrBlank(): Boolean = this == null || this.isBlank()
fun String?.ifNotBlank(block: (String) -> Unit): String? {
    if (!isNullOrBlank()) block(this!!)
    return this
}

/**
 * List extensions.
 */
fun <T> List<T>.firstOrNullOrEmpty(predicate: (T) -> Boolean): T? {
    return firstOrNull { predicate(it) }
}

fun <T> List<T>.isNullOrEmpty(): Boolean = this.isNullOrEmpty()

/**
 * Map extensions.
 */
fun <K, V> Map<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    return getOrPut(key, defaultValue)
}

/**
 * Coroutine scope for ViewModels.
 */
class ViewModelScope(private val job: SupervisorJob = SupervisorJob()) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = job + kotlinx.coroutines.Dispatchers.Main.immediate

    fun cancel() = job.cancel()
}

/**
 * Network state helper.
 */
sealed class NetworkState {
    data class Connected(val timestamp: Long = System.currentTimeMillis()) : NetworkState()
    data class Disconnected(val timestamp: Long = System.currentTimeMillis()) : NetworkState()
    data class Connecting(val timestamp: Long = System.currentTimeMillis()) : NetworkState()
}

/**
 * Paging state for lists.
 */
data class PagingState<T>(
    val items: List<T> = emptyList(),
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val error: Error? = null
) {
    fun copyWith(
        items: List<T>? = null,
        hasMore: Boolean? = null,
        isLoading: Boolean? = null,
        error: Error? = null
    ): PagingState<T> {
        return PagingState(
            items = items ?: this.items,
            hasMore = hasMore ?: this.hasMore,
            isLoading = isLoading ?: this.isLoading,
            error = error
        )
    }
}

/**
 * Resource state for async operations - combines loading, success, error, empty.
 */
sealed class Resource<T> {
    data class Loading<T>(val data: T? = null) : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: Error, val data: T? = null) : Resource<T>()
    object Empty : Resource<Nothing>()

    companion object {
        fun <T> loading(data: T? = null): Resource<T> = Loading(data)
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> error(error: Error, data: T? = null): Resource<T> = Error(error, data)
        fun <T> empty(): Resource<T> = Empty as Resource<T>
    }

    val isLoading: Boolean get() = this is Loading<*>
    val isSuccess: Boolean get() = this is Success<*>
    val isError: Boolean get() = this is Error<*>
    val isEmpty: Boolean get() = this == Empty

    val data: T?
        get() = when (this) {
            is Loading -> data
            is Success -> data
            is Error -> data
            is Empty -> null
        }

    val error: Error?
        get() = if (this is Error<*>) error else null

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Loading -> Resource.Loading(data?.let(transform))
        is Success -> Resource.Success(transform(data))
        is Error -> Resource.Error(error!!, data?.let(transform))
        is Empty -> Resource.Empty
    }
}

/**
 * Timber tree for debug logging with enhanced formatting.
 */
class TimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.DEBUG) return

        val tagStr = tag ?: "AetherGlass"
        val msg = if (t != null) "$message\n${Log.getStackTraceString(t)}" else message

        when (priority) {
            Log.DEBUG -> Log.d(tagStr, msg)
            Log.INFO -> Log.i(tagStr, msg)
            Log.WARN -> Log.w(tagStr, msg)
            Log.ERROR -> Log.e(tagStr, msg)
            else -> Log.v(tagStr, msg)
        }
    }
}