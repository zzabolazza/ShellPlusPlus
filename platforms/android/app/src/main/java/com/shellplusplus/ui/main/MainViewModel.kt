package com.shellplusplus.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shellplusplus.data.AppInfo
import com.shellplusplus.data.AppManager
import com.shellplusplus.service.AppSwitchGestureDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appManager: AppManager,
    private val gestureDetector: AppSwitchGestureDetector
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp

    private val _openedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val openedApps: StateFlow<List<AppInfo>> = _openedApps

    private val _showConfigDialog = MutableStateFlow(false)
    val showConfigDialog: StateFlow<Boolean> = _showConfigDialog

    private val _sourceUrl = MutableStateFlow(appManager.getSourceUrl())
    val sourceUrl: StateFlow<String> = _sourceUrl

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appManager.syncApps()
                publishApps()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showConfigDialog() {
        _showConfigDialog.value = true
    }

    fun hideConfigDialog() {
        _showConfigDialog.value = false
    }

    fun saveSourceUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            appManager.setSourceUrl(trimmed)
            _sourceUrl.value = trimmed
            _isLoading.value = true
            try {
                appManager.syncApps()
                publishApps()
            } finally {
                _isLoading.value = false
                _showConfigDialog.value = false
            }
        }
    }

    private suspend fun publishApps() {
        val fresh = appManager.getApps()
        _apps.value = fresh
        _openedApps.value = _openedApps.value.mapNotNull { opened ->
            fresh.find { it.packageName == opened.packageName }
        }
        val selectedPackage = _selectedApp.value?.packageName
        val refreshedSelection = _openedApps.value.find { it.packageName == selectedPackage }
        _selectedApp.value = when {
            selectedPackage == null -> null
            refreshedSelection != null -> refreshedSelection
            else -> _openedApps.value.firstOrNull()
        }
    }

    fun toggleAppEnabled(packageName: String) {
        viewModelScope.launch {
            val app = _apps.value.find { it.packageName == packageName }
            app?.let {
                appManager.toggleAppEnabled(it)
                publishApps()
            }
        }
    }

    fun selectApp(app: AppInfo) {
        val opened = _openedApps.value.toMutableList()
        val existing = opened.indexOfFirst { it.packageName == app.packageName }
        if (existing >= 0) {
            opened[existing] = app
        } else {
            opened.add(app)
        }
        _openedApps.value = opened
        _selectedApp.value = app
    }

    fun closeSelectedApp() {
        val current = _selectedApp.value ?: return
        val opened = _openedApps.value.toMutableList()
        val index = opened.indexOfFirst { it.packageName == current.packageName }
        if (index < 0) {
            _selectedApp.value = null
            return
        }
        opened.removeAt(index)
        _openedApps.value = opened
        _selectedApp.value = when {
            opened.isEmpty() -> null
            index <= opened.lastIndex -> opened[index]
            else -> opened.last()
        }
    }

    fun selectRelativeApp(delta: Int): Boolean {
        if (delta == 0) return false
        val list = _openedApps.value
        if (list.size < 2) return false
        val current = _selectedApp.value ?: return false
        val index = list.indexOfFirst { it.packageName == current.packageName }
        if (index < 0) return false
        val next = list[Math.floorMod(index + delta, list.size)]
        if (next.packageName == current.packageName) return false
        _selectedApp.value = next
        return true
    }

    fun startGestureDetection() {
        viewModelScope.launch {
            gestureDetector.startDetection()
        }
    }

    fun stopGestureDetection() {
        viewModelScope.launch {
            gestureDetector.stopDetection()
        }
    }
}