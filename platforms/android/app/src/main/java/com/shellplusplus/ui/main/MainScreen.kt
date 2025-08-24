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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import coil.compose.AsyncImage
import com.shellplusplus.data.AppInfo
import com.shellplusplus.ui.components.AppWebView
import java.net.URI
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Close

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
    
    // 当前页面标题状态
    var currentPageTitle by remember { mutableStateOf<String?>(null) }
    
    // 顶部菜单栏自动隐藏状态
    var isTopBarVisible by remember { mutableStateOf(true) }
    var topBarHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    // 自动隐藏顶部菜单栏的函数
    fun hideTopBarAfterDelay() {
        topBarHideJob?.cancel()
        topBarHideJob = scope.launch {
            kotlinx.coroutines.delay(3000) // 3秒后自动隐藏
            // 只有当侧边栏关闭时才隐藏顶部菜单栏
            if (drawerState.currentValue == DrawerValue.Closed) {
                isTopBarVisible = false
            }
        }
    }
    
    // 显示顶部菜单栏的函数
    fun showTopBar() {
        isTopBarVisible = true
        // 只有当侧边栏关闭时才开始自动隐藏计时
        if (drawerState.currentValue == DrawerValue.Closed) {
            hideTopBarAfterDelay()
        }
    }
    
    // 监听侧边栏状态变化
    LaunchedEffect(drawerState.currentValue) {
        when (drawerState.currentValue) {
            DrawerValue.Open -> {
                // 侧边栏打开时，显示顶部菜单栏并停止自动隐藏
                isTopBarVisible = true
                topBarHideJob?.cancel()
            }
            DrawerValue.Closed -> {
                // 侧边栏关闭时，开始自动隐藏计时
                hideTopBarAfterDelay()
            }
        }
    }
    
    // 当选择的应用程序改变时，重置页面标题
    LaunchedEffect(selectedApp) {
        currentPageTitle = null
    }
    
    // 初始化时显示顶部菜单栏并开始计时
    LaunchedEffect(Unit) {
        hideTopBarAfterDelay()
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(Modifier.height(1.5.dp))
                
                // 标题栏，包含标题和关闭按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "应用列表",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    // 关闭按钮
                    IconButton(
                        onClick = { 
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭应用列表",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
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
                        if (apps.isEmpty()) {
                            // 应用列表为空时的提示
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "正在加载默认应用...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
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
                        }
                        
                        // 添加按钮（底部悬浮按钮）
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .clickable { viewModel.showAddDialog() }
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
                if (isTopBarVisible) {
                    TopAppBar(
                        title = { 
                            Text(
                                text = currentPageTitle ?: selectedApp?.name ?: "Shell++",
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            ) 
                        },
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
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        var pointerCount = 0
                        
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        pointerCount = event.changes.size
                                        if (pointerCount >= 2) {
                                            // 双指触屏，显示顶部菜单栏
                                            showTopBar()
                                        }
                                    }
                                    PointerEventType.Release -> {
                                        pointerCount = 0
                                    }
                                }
                            }
                        }
                    }
            ) {
                // 网页内容区域
                selectedApp?.let { app ->
                    AppWebView(
                        url = app.url,
                        modifier = Modifier.fillMaxSize(),
                        onTitleChanged = { title ->
                            currentPageTitle = title
                        }
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
    // 从URL中提取域名
    val domain = try {
        val uri = URI(app.url)
        uri.host ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
    
    // 构建favicon URL
    val faviconUrl = "https://favicon.im/$domain"
    
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
                // 使用网络图标，如果加载失败则显示文字
                AsyncImage(
                    model = faviconUrl,
                    contentDescription = "${app.name} 图标",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
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