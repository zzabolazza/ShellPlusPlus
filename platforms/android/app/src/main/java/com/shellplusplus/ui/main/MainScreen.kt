package com.shellplusplus.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import coil.compose.AsyncImage
import com.shellplusplus.data.AppInfo
import com.shellplusplus.ui.components.AppWebView
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import coil.request.ImageRequest
import kotlin.math.hypot
import kotlin.math.roundToInt

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
    var revealedPackage by remember { mutableStateOf<String?>(null) }
    
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

    val drawerWidth = 300.dp
    val addAccent = rememberLogoColor(selectedApp?.url.orEmpty())
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val doubleTapTimeout = viewConfiguration.doubleTapTimeoutMillis
    val doubleTapMinTime = viewConfiguration.doubleTapMinTimeMillis
    val doubleTapSlop = viewConfiguration.touchSlop * 2

    Box(modifier = Modifier.fillMaxSize()) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) { 
                // 标题栏，包含标题和关闭按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "应用列表",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                                state = rememberLazyListState()
                            ) {
                                items(
                                    items = apps,
                                    key = { it.packageName }
                                ) { app ->
                                    val revealed = revealedPackage == app.packageName
                                    SwipeRevealAppRow(
                                        revealed = revealed,
                                        onRevealedChange = { open ->
                                            revealedPackage = if (open) app.packageName else null
                                        },
                                        onClick = {
                                            if (revealed) {
                                                revealedPackage = null
                                            } else {
                                                viewModel.selectApp(app)
                                                scope.launch { drawerState.close() }
                                            }
                                        },
                                        onEdit = {
                                            revealedPackage = null
                                            viewModel.showEditDialog(app)
                                        },
                                        onDelete = {
                                            revealedPackage = null
                                            viewModel.showDeleteConfirmation(app)
                                        }
                                    ) {
                                        AppItem(
                                            app = app,
                                            isSelected = app.packageName == selectedApp?.packageName
                                        )
                                    }
                                }
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(lerp(MaterialTheme.colorScheme.surface, addAccent, 0.22f))
                                .clickable { viewModel.showAddDialog() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加应用",
                                tint = addAccent
                            )
                        }
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
                    .pointerInput(doubleTapTimeout, doubleTapMinTime, doubleTapSlop) {
                        var pointerCount = 0
                        var lastTapUptime = 0L
                        var lastTapPosition = Offset.Zero
                        var tapDownPosition = Offset.Zero

                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        pointerCount = event.changes.size
                                        if (pointerCount >= 2) {
                                            // 双指触屏，显示顶部菜单栏
                                            showTopBar()
                                        } else if (pointerCount == 1) {
                                            tapDownPosition = event.changes[0].position
                                        }
                                    }
                                    PointerEventType.Release -> {
                                        val change = event.changes.firstOrNull()
                                        if (pointerCount == 1 && change != null &&
                                            hypot(
                                                change.position.x - tapDownPosition.x,
                                                change.position.y - tapDownPosition.y
                                            ) <= doubleTapSlop
                                        ) {
                                            val now = change.uptimeMillis
                                            val sinceLast = now - lastTapUptime
                                            val fromLast = hypot(
                                                change.position.x - lastTapPosition.x,
                                                change.position.y - lastTapPosition.y
                                            )
                                            if (lastTapUptime != 0L &&
                                                sinceLast in doubleTapMinTime..doubleTapTimeout &&
                                                fromLast <= doubleTapSlop
                                            ) {
                                                scope.launch { drawerState.open() }
                                                lastTapUptime = 0L
                                            } else {
                                                lastTapUptime = now
                                                lastTapPosition = change.position
                                            }
                                        }
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
                        },
                        onDoubleTap = {
                            scope.launch { drawerState.open() }
                        },
                        onTwoFingerSwipe = { delta ->
                            val drawerOpen = drawerState.currentValue == DrawerValue.Open ||
                                drawerState.targetValue == DrawerValue.Open
                            if (!drawerOpen && viewModel.selectRelativeApp(delta)) {
                                showTopBar()
                            }
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

        // 添加或编辑应用
        val showAddDialog by viewModel.showAddDialog.collectAsState()
        val editingApp by viewModel.editingApp.collectAsState()
        if (showAddDialog || editingApp != null) {
            val editing = editingApp
            var name by remember(editing?.packageName) { mutableStateOf(editing?.name.orEmpty()) }
            var url by remember(editing?.packageName) { mutableStateOf(editing?.url.orEmpty()) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val dismissForm = {
                if (editing != null) viewModel.hideEditDialog() else viewModel.hideAddDialog()
            }

            ModalBottomSheet(
                onDismissRequest = dismissForm,
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .imePadding()
                ) {
                    Text(
                        text = if (editing != null) "编辑应用" else "添加应用",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = dismissForm) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                if (name.isNotBlank() && url.isNotBlank()) {
                                    if (editing != null) {
                                        viewModel.updateApp(name, url)
                                    } else {
                                        viewModel.addApp(name, url)
                                        viewModel.hideAddDialog()
                                    }
                                }
                            },
                            enabled = name.isNotBlank() && url.isNotBlank()
                        ) {
                            Text(if (editing != null) "保存" else "添加")
                        }
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

    val drawerOpen = drawerState.currentValue == DrawerValue.Open ||
        drawerState.targetValue == DrawerValue.Open
    if (drawerOpen) {
        val drawerWidthPx = with(density) { drawerWidth.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    val raw = drawerState.offset.value
                    val right = if (raw.isNaN()) drawerWidthPx else raw + drawerWidthPx
                    IntOffset(right.roundToInt().coerceAtLeast(0), 0)
                }
                .pointerInput(Unit) {
                    detectTapGestures {
                        scope.launch { drawerState.close() }
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
    isSelected: Boolean
) {
    val context = LocalContext.current
    val faviconUrl = faviconUrlFor(app.url)
    val logoColor = rememberLogoColor(app.url)
    val selectedBackground = lerp(MaterialTheme.colorScheme.surface, logoColor, 0.16f)
    val checkColor = logoColor.forCheckCircle()
    val imageRequest = remember(faviconUrl) {
        ImageRequest.Builder(context)
            .data(faviconUrl)
            .allowHardware(false)
            .build()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) selectedBackground else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = "${app.name} 图标",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(checkColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "当前应用",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private val RevealActionWidth = 72.dp

@Composable
private fun SwipeRevealAppRow(
    revealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val revealPx = with(density) { (RevealActionWidth * 2).toPx() }
    val revealPxState = rememberUpdatedState(revealPx)
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val dragState = rememberDraggableState { delta ->
        scope.launch {
            offset.snapTo((offset.value + delta).coerceIn(-revealPxState.value, 0f))
        }
    }

    LaunchedEffect(revealed) {
        offset.animateTo(if (revealed) -revealPx else 0f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End
        ) {
            RevealActionButton(
                label = "编辑",
                icon = Icons.Default.Edit,
                background = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onEdit
            )
            RevealActionButton(
                label = "删除",
                icon = Icons.Default.Delete,
                background = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                onClick = onDelete
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = dragState,
                    onDragStarted = {
                        scope.launch { offset.stop() }
                    },
                    onDragStopped = {
                        val open = offset.value < -revealPxState.value / 2f
                        if (open == revealed) {
                            scope.launch {
                                offset.animateTo(if (open) -revealPxState.value else 0f)
                            }
                        } else {
                            onRevealedChange(open)
                        }
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
private fun RevealActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(RevealActionWidth)
            .fillMaxHeight()
            .background(background)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}