package com.shellplusplus.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
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
    val openedApps by viewModel.openedApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var switcherIndex by remember { mutableStateOf<Int?>(null) }
    var displayedSwitcherIndex by remember { mutableIntStateOf(0) }
    
    // 当前页面标题状态
    var pageTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val currentPageTitle = selectedApp?.packageName?.let { pageTitles[it] }
    
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
    
    // 初始化时显示顶部菜单栏并开始计时
    LaunchedEffect(Unit) {
        hideTopBarAfterDelay()
    }

    val drawerWidth = 300.dp
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
                                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                                state = rememberLazyListState()
                            ) {
                                items(
                                    items = apps,
                                    key = { it.packageName }
                                ) { app ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectApp(app)
                                                scope.launch { drawerState.close() }
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
                if (openedApps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("请从左侧菜单选择一个应用")
                    }
                } else {
                    openedApps.forEach { app ->
                        key(app.packageName) {
                            val active = app.packageName == selectedApp?.packageName
                            AppWebView(
                                url = app.url,
                                active = active,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(if (active) 1f else 0f),
                                onTitleChanged = { title ->
                                    pageTitles = pageTitles + (app.packageName to title)
                                },
                                onDoubleTap = {
                                    scope.launch { drawerState.open() }
                                },
                                touchEnabled = switcherIndex == null,
                                onShowSwitcher = {
                                    val drawerOpen = drawerState.currentValue == DrawerValue.Open ||
                                        drawerState.targetValue == DrawerValue.Open
                                    if (!drawerOpen && openedApps.size > 1 && switcherIndex == null) {
                                        val current = openedApps.indexOfFirst {
                                            it.packageName == selectedApp?.packageName
                                        }.coerceAtLeast(0)
                                        switcherIndex = current
                                        displayedSwitcherIndex = current
                                    }
                                },
                                onTwoFingerSwipeDown = {
                                    val drawerOpen = drawerState.currentValue == DrawerValue.Open ||
                                        drawerState.targetValue == DrawerValue.Open
                                    if (!drawerOpen && selectedApp != null) {
                                        switcherIndex = null
                                        viewModel.closeSelectedApp()
                                        showTopBar()
                                    }
                                }
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = switcherIndex != null && openedApps.size > 1,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                ) {
                    OpenAppSwitcher(
                        apps = openedApps,
                        selectedIndex = displayedSwitcherIndex,
                        onSelect = { app ->
                            viewModel.selectApp(app)
                            showTopBar()
                            switcherIndex = null
                        },
                        onDismiss = { switcherIndex = null }
                    )
                }
            }
        }
        
        val showConfigDialog by viewModel.showConfigDialog.collectAsState()
        val sourceUrl by viewModel.sourceUrl.collectAsState()
        if (showConfigDialog) {
            var sourceText by remember(sourceUrl) { mutableStateOf(sourceUrl) }
            AlertDialog(
                onDismissRequest = { viewModel.hideConfigDialog() },
                title = { Text("应用列表源") },
                text = {
                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        label = { Text("源地址") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.saveSourceUrl(sourceText) },
                        enabled = sourceText.isNotBlank()
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideConfigDialog() }) {
                        Text("取消")
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

@Composable
private fun OpenAppSwitcher(
    apps: List<AppInfo>,
    selectedIndex: Int,
    onSelect: (AppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val dismissInteraction = remember { MutableInteractionSource() }
    val cardInteraction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable(
                interactionSource = dismissInteraction,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .clickable(
                    interactionSource = cardInteraction,
                    indication = null,
                    onClick = {}
                )
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                apps.forEachIndexed { index, app ->
                    SwitcherAppIcon(
                        app = app,
                        selected = index == selectedIndex,
                        onClick = { onSelect(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitcherAppIcon(
    app: AppInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val faviconUrl = faviconUrlFor(app.url)
    val imageRequest = remember(faviconUrl) {
        ImageRequest.Builder(context)
            .data(faviconUrl)
            .allowHardware(false)
            .build()
    }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            scale.snapTo(1f)
            scale.animateTo(
                1.15f,
                keyframes {
                    durationMillis = 300
                    1f at 0
                    1.2f at 150
                    1.15f at 300
                }
            )
        } else {
            scale.animateTo(1f, tween(300))
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.6f,
        animationSpec = tween(300),
        label = "switcherAlpha"
    )

    Column(
        modifier = Modifier
            .width(80.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = app.name,
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
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

