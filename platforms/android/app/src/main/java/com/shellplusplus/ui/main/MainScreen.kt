package com.shellplusplus.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.shellplusplus.data.AppInfo
import com.shellplusplus.ui.components.AppWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "应用列表",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider()
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                            state = rememberLazyListState()
                        ) {
                            items(
                                items = apps,
                                key = { it.packageName }
                            ) { app ->
                                val dismissState = rememberDismissState(
                                    confirmValueChange = { dismissValue ->
                                        if (dismissValue == DismissValue.DismissedToStart) {
                                            viewModel.showDeleteConfirmation(app)
                                        }
                                        false
                                    }
                                )
                                SwipeToDismiss(
                                    state = dismissState,
                                    background = { },
                                    dismissContent = {
                                        AppItem(
                                            app = app,
                                            isSelected = app.packageName == selectedApp?.packageName,
                                            onSelect = {
                                                viewModel.selectApp(app)
                                                scope.launch { drawerState.close() }
                                            }
                                        )
                                    },
                                    directions = setOf(DismissDirection.EndToStart)
                                )
                            }
                        }
                        
                        // 添加按钮
                        FloatingActionButton(
                            onClick = { viewModel.showAddDialog() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "添加应用")
                        }
                    }

                    // 删除确认对话框
                    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.hideDeleteConfirmation() },
                            title = { Text("确认删除") },
                            text = {
                                val appToDelete by viewModel.appToDelete.collectAsState()
                                Text("确定要删除 ${appToDelete?.name} 吗？")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteApp()
                                        viewModel.hideDeleteConfirmation()
                                    }
                                ) {
                                    Text("删除")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                                    Text("取消")
                                }
                            }
                        )
                    }

                    // 添加应用对话框
                    val showAddDialog by viewModel.showAddDialog.collectAsState()
                    if (showAddDialog) {
                        var name by remember { mutableStateOf("") }
                        var url by remember { mutableStateOf("") }
                        
                        AlertDialog(
                            onDismissRequest = { viewModel.hideAddDialog() },
                            title = { Text("添加应用") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("应用名称") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = url,
                                        onValueChange = { url = it },
                                        label = { Text("应用网址") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (name.isNotBlank() && url.isNotBlank()) {
                                            viewModel.addApp(name, url)
                                            viewModel.hideAddDialog()
                                        }
                                    }
                                ) {
                                    Text("添加")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.hideAddDialog() }) {
                                    Text("取消")
                                }
                            }
                        )
                    }
                }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = false
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedApp?.name ?: "Shell++") },
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.showConfigDialog() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "配置"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                selectedApp?.let { app ->
                    AppWebView(
                        url = app.url,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("请从左侧菜单选择一个应用")
                    }
                }
            }
        }
        
        // 配置对话框
        val showConfigDialog by viewModel.showConfigDialog.collectAsState()
        if (showConfigDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideConfigDialog() },
                title = { Text("配置") },
                text = { Text("此功能正在开发中，敬请期待...") },
                confirmButton = {
                    TextButton(onClick = { viewModel.hideConfigDialog() }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItem(
    app: AppInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
        .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                    modifier = Modifier
                    .size(32.dp)
                        .clip(CircleShape)
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}