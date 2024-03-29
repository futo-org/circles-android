package org.futo.circles.core.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import org.futo.circles.core.base.ErrorParser
import org.futo.circles.core.base.fragment.HasLoadingState

fun <T> LiveData<Response<T>>.observeResponse(
    fragment: Fragment,
    success: (T) -> Unit = {},
    error: ((String) -> Unit)? = null,
    onRequestInvoked: (() -> Unit)? = null
) {
    observe(fragment.viewLifecycleOwner) {
        it ?: return@observe
        onRequestInvoked?.invoke()
            ?: run { (fragment as? HasLoadingState)?.stopLoading() }
        when (it) {
            is Response.Success -> success(it.data)
            is Response.Error -> error?.invoke(it.message)
                ?: fragment.showError(it.message)
        }
    }
}

fun <T> LiveData<T>.observeData(fragment: Fragment, observer: (T) -> Unit) {
    val owner = fragment.viewLifecycleOwner
    observe(owner) {
        if (it != null) observer(it)
    }
}

suspend fun <T> createResult(block: suspend () -> T): Response<T> {
    return try {
        Response.Success(block())
    } catch (t: Throwable) {
        Response.Error(ErrorParser.getErrorMessage(t))
    }
}

sealed class Response<out T> {

    data class Success<out T>(val data: T) : Response<T>()

    data class Error(val message: String) : Response<Nothing>()
}