package com.eag.tflmeme.game.domain


sealed class StateHolder {
    data object Idle : StateHolder()
    data object Loading : StateHolder()
    data object Success : StateHolder()
    data class SuccessWithData(val data: Any?) : StateHolder()
    data class Error(val message: String) : StateHolder()
}