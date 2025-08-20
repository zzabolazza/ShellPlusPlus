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

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private val _appToDelete = MutableStateFlow<AppInfo?>(null)
    val appToDelete: StateFlow<AppInfo?> = _appToDelete

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    private val _showConfigDialog = MutableStateFlow(false)
    val showConfigDialog: StateFlow<Boolean> = _showConfigDialog

    init {
        refreshApps()
    }

    fun showConfigDialog() {
        _showConfigDialog.value = true
    }

    fun hideConfigDialog() {
        _showConfigDialog.value = false
    }

    fun showDeleteConfirmation(app: AppInfo) {
        _appToDelete.value = app
        _showDeleteDialog.value = true
    }

    fun hideDeleteConfirmation() {
        _showDeleteDialog.value = false
        _appToDelete.value = null
    }

    fun deleteApp() {
        viewModelScope.launch {
            _appToDelete.value?.let { app ->
                appManager.removeApp(app.packageName)
                refreshApps()
            }
        }
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun addApp(name: String, url: String) {
        viewModelScope.launch {
            appManager.addApp(name, url)
            refreshApps()
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _apps.value = appManager.getApps()
                // 如果当前选中的应用不在列表中，清除选择
                if (_selectedApp.value != null && !_apps.value.any { it.packageName == _selectedApp.value?.packageName }) {
                    _selectedApp.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAppEnabled(packageName: String) {
        viewModelScope.launch {
            val app = _apps.value.find { it.packageName == packageName }
            app?.let {
                appManager.toggleAppEnabled(it)
                refreshApps()
            }
        }
    }

    fun selectApp(app: AppInfo) {
        _selectedApp.value = app
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