package com.shellplusplus.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppSwitchService : AccessibilityService() {
    @Inject
    lateinit var gestureDetector: AppSwitchGestureDetector

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Handle window state changes
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        gestureDetector.startDetection()
    }

    override fun onDestroy() {
        super.onDestroy()
        gestureDetector.stopDetection()
    }
}