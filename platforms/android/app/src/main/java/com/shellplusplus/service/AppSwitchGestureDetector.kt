package com.shellplusplus.service

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSwitchGestureDetector @Inject constructor() {
    private var isDetecting = false

    fun startDetection() {
        isDetecting = true
    }

    fun stopDetection() {
        isDetecting = false
    }

    fun isDetecting(): Boolean = isDetecting
}
