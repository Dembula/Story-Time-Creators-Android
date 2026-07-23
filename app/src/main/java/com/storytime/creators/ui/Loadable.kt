package com.storytime.creators.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.storytime.creators.core.network.ApiException

sealed class LoadState<out T> {
    data object Loading : LoadState<Nothing>()
    data class Error(val message: String) : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
}

class LoadableController {
    var reloadKey by mutableIntStateOf(0)
        private set
    fun reload() { reloadKey++ }
}

@Composable
fun rememberLoadable(): LoadableController = remember { LoadableController() }

/**
 * Runs [loader] on composition (and whenever [controller] reloads / [key] changes),
 * rendering loading / error / content states.
 */
@Composable
fun <T> Loadable(
    controller: LoadableController,
    key: Any? = Unit,
    emptyIcon: String = "tray",
    loader: suspend () -> T,
    content: @Composable (T) -> Unit,
) {
    var state by remember(controller, key) { mutableStateOf<LoadState<T>>(LoadState.Loading) }

    LaunchedEffect(controller.reloadKey, key) {
        state = LoadState.Loading
        state = try {
            LoadState.Success(loader())
        } catch (e: ApiException) {
            LoadState.Error(e.message ?: "Something went wrong.")
        } catch (e: Exception) {
            LoadState.Error(e.message ?: "Something went wrong.")
        }
    }

    when (val s = state) {
        is LoadState.Loading -> LoadingStateView()
        is LoadState.Error -> ErrorStateView(s.message) { controller.reload() }
        is LoadState.Success -> content(s.data)
    }
}
