package com.power.baseproject.ktbase.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


open class BaseViewModel : ViewModel() {

    fun launch(
        block: suspend () -> Unit,
        error: (Throwable) -> Unit,
        complete: suspend () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                error(e)
            } finally {
                complete()
            }
        }
    }
}