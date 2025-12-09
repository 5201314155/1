package studio.xiaoyun.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Chip
import androidx.compose.material3.ChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.xiaoyun.ui.theme.XiaoYunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XiaoYunTheme {
                StudioHomeScreen()
            }
        }
    }
}

data class CanvasComponent(
    val id: Int,
    val name: String,
    val visible: Boolean = true,
    val locked: Boolean = false
)

data class DeviceProfile(
    val name: String,
    val logicalSize: String,
    val note: String
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StudioHomeScreen() {
    var selectedComponent by remember { mutableStateOf("按钮") }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var nextId by remember { mutableStateOf(1) }
    var previewMode by remember { mutableStateOf(false) }
    val deviceProfiles = listOf(
        DeviceProfile(name = "小屏 720p", logicalSize = "360x720dp", note = "入门机型"),
        DeviceProfile(name = "主流 1080p", logicalSize = "411x891dp", note = "大多数机型"),
        DeviceProfile(name = "大屏 2K", logicalSize = "480x960dp", note = "平板/大屏")
    )
    var selectedDevice by remember { mutableStateOf(deviceProfiles[1]) }
    val canvasComponents = remember { mutableStateListOf<CanvasComponent>() }

    fun addComponentToCanvas(name: String) {
        canvasComponents.add(CanvasComponent(id = nextId, name = name))
        nextId += 1
    }

    fun toggleVisibility(id: Int) {
        val index = canvasComponents.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = canvasComponents[index]
            canvasComponents[index] = current.copy(visible = !current.visible)
        }
    }

    fun toggleLock(id: Int) {
        val index = canvasComponents.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = canvasComponents[index]
            canvasComponents[index] = current.copy(locked = !current.locked)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "小云 UI Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "布局参考剪映 · 预览即进入应用",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "进入预览")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusRibbon(selectedComponent = selectedComponent, zoom = zoom)
            Row(modifier = Modifier.weight(1f)) {
                ComponentLibraryPanel(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight(),
                    selectedComponent = selectedComponent,
                    onComponentSelected = { selectedComponent = it },
                    onAddToCanvas = { addComponentToCanvas(it) }
                )
                CanvasPreview(
                    modifier = Modifier.weight(1f),
                    selectedComponent = selectedComponent,
                    zoom = zoom,
                    previewMode = previewMode,
                    selectedDevice = selectedDevice,
                    availableDevices = deviceProfiles,
                    onTogglePreview = { previewMode = !previewMode },
                    onZoomChange = { zoom = it },
                    onDeviceChange = { selectedDevice = it },
                    components = canvasComponents,
                    onSelectComponent = { selectedComponent = it }
                )
                PropertyPanel(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight(),
                    selectedComponent = selectedComponent,
                    zoom = zoom,
                    components = canvasComponents,
                    onSelectComponent = { selectedComponent = it },
                    onToggleVisible = { toggleVisibility(it) },
                    onToggleLock = { toggleLock(it) }
                )
            }
            BottomToolBar()
        }
    }
}

@Composable
fun ComponentLibraryPanel(
    modifier: Modifier = Modifier,
    selectedComponent: String,
    onComponentSelected: (String) -> Unit,
    onAddToCanvas: (String) -> Unit
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "组件库", style = MaterialTheme.typography.titleMedium)
            val sections = listOf(
                "基础组件" to listOf("按钮", "文本", "图片", "矩形"),
                "容器布局" to listOf("Flex行", "Flex列", "网格"),
                "高级组件" to listOf("卡片", "底部导航", "弹窗")
            )
            sections.forEach { (title, items) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = title, fontWeight = FontWeight.SemiBold)
                        items.forEach { item ->
                            val isSelected = item == selectedComponent
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onComponentSelected(item) },
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onAddToCanvas(selectedComponent) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("添加到画布")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CanvasPreview(
    modifier: Modifier = Modifier,
    selectedComponent: String,
    zoom: Float,
    previewMode: Boolean,
    selectedDevice: DeviceProfile,
    availableDevices: List<DeviceProfile>,
    onTogglePreview: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onDeviceChange: (DeviceProfile) -> Unit,
    components: List<CanvasComponent>,
    onSelectComponent: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "画布（手机视图）", style = MaterialTheme.typography.titleMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            PhoneFramePreview(
                selectedComponent = selectedComponent,
                zoom = zoom,
                previewMode = previewMode,
                device = selectedDevice,
                components = components,
                onSelectComponent = onSelectComponent
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onTogglePreview) {
                Text(if (previewMode) "返回编辑" else "实时预览")
            }
            Button(onClick = { }) {
                Text("生成代码")
            }
        }
        DeviceSizeSelector(
            availableDevices = availableDevices,
            selectedDevice = selectedDevice,
            onDeviceChange = onDeviceChange
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "缩放比例：${(zoom * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = zoom,
                    onValueChange = onZoomChange,
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        thumbColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedButton(
                    onClick = { onZoomChange(1f) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("还原")
                }
            }
        }
    }
}

@Composable
fun PhoneFramePreview(
    selectedComponent: String,
    zoom: Float,
    previewMode: Boolean,
    device: DeviceProfile,
    components: List<CanvasComponent>,
    onSelectComponent: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth(0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FakeStatusBar(device)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (previewMode) "预览 = 直接进入假手机" else "画布 = 假手机",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "组件属性只基于画布尺寸，不依赖真机参数",
                        color = Color.Black.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "当前选中：$selectedComponent",
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "缩放：${(zoom * 100).toInt()}% · ${device.name}",
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    LayerPreviewStack(
                        components = components,
                        onSelectComponent = onSelectComponent,
                        previewMode = previewMode
                    )
                    Text(
                        text = "预览按钮会切换到假手机屏幕，模拟进入应用",
                        color = Color.Black.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            FakeBottomBar(previewMode = previewMode, device = device)
        }
    }
}

@Composable
fun PropertyPanel(
    modifier: Modifier = Modifier,
    selectedComponent: String,
    zoom: Float,
    components: List<CanvasComponent>,
    onSelectComponent: (String) -> Unit,
    onToggleVisible: (Int) -> Unit,
    onToggleLock: (Int) -> Unit
) {
    Surface(
        modifier = modifier,
        tonalElevation = 2.dp
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "属性面板", style = MaterialTheme.typography.titleMedium)
            PropertyGroup(
                title = "已选组件",
                items = listOf("名称：$selectedComponent", "缩放：${(zoom * 100).toInt()}%", "状态：预览等同真机")
            )
            LayerList(
                components = components,
                onSelectComponent = onSelectComponent,
                onToggleVisible = onToggleVisible,
                onToggleLock = onToggleLock
            )
            PropertyGroup(title = "尺寸与对齐", items = listOf("宽度：匹配画布", "高度：自适应", "对齐：居中"))
            PropertyGroup(title = "样式", items = listOf("圆角：12dp", "背景：主题色", "阴影：柔和"))
            PropertyGroup(title = "交互", items = listOf("点击：预览模式", "双指：缩放", "长按：显示菜单"))
            PropertyGroup(title = "适配", items = listOf("独立于真机参数", "兼容所有尺寸", "预览即真实效果"))
        }
    }
}

@Composable
fun LayerList(
    components: List<CanvasComponent>,
    onSelectComponent: (String) -> Unit,
    onToggleVisible: (Int) -> Unit,
    onToggleLock: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "画布图层", fontWeight = FontWeight.SemiBold)
            if (components.isEmpty()) {
                Text(
                    text = "暂无组件，先从左侧添加", 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                components.forEach { component ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (component.visible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = component.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "状态：${if (component.visible) "可见" else "隐藏"} · ${if (component.locked) "已锁定" else "可编辑"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(onClick = { onSelectComponent(component.name) }) {
                                    Text("聚焦")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onToggleVisible(component.id) }) {
                                    Text(if (component.visible) "隐藏" else "显示")
                                }
                                OutlinedButton(onClick = { onToggleLock(component.id) }) {
                                    Text(if (component.locked) "解锁" else "锁定")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyGroup(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSizeSelector(
    availableDevices: List<DeviceProfile>,
    selectedDevice: DeviceProfile,
    onDeviceChange: (DeviceProfile) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "假手机尺寸",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            availableDevices.forEach { device ->
                Chip(
                    onClick = { onDeviceChange(device) },
                    colors = ChipDefaults.chipColors(
                        containerColor = if (device == selectedDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(device.name, fontWeight = FontWeight.Medium)
                        Text(device.logicalSize, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Text(
            text = "组件属性仅基于假手机尺寸计算，默认兼容多设备",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FakeStatusBar(device: DeviceProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Outlined.Smartphone, contentDescription = null, tint = Color.White)
            Column {
                Text(text = device.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(text = device.logicalSize, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "17:30", color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "Wi-Fi", color = Color.White.copy(alpha = 0.8f))
            Text(text = "电量 90%", color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun FakeBottomBar(previewMode: Boolean, device: DeviceProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = if (previewMode) "预览中 · 假手机全屏" else "编辑中 · 画布模式", color = Color.White)
            Text(text = "多机型模拟：${device.note}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Text(text = "预览=进入应用", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayerPreviewStack(
    components: List<CanvasComponent>,
    onSelectComponent: (String) -> Unit,
    previewMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(Color(0xFFFFA25C).copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "假手机顶部状态区", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFA25C).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (previewMode) "预览画布区域" else "画布区域", color = Color(0xFFE1691B))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFFFFA25C).copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "假手机底部操作栏", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
        if (components.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                components.take(3).forEach { component ->
                    Chip(onClick = { onSelectComponent(component.name) }) {
                        Text(text = component.name)
                    }
                }
                if (components.size > 3) {
                    Text(
                        text = "…${components.size - 3} 个图层",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BottomToolBar() {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Save, contentDescription = "保存")
            Text(
                text = "一键保存 · 自动同步画布状态",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { }) {
                Text("导出布局代码")
            }
            Button(onClick = { }) {
                Text("立即预览")
            }
        }
    }
}

@Composable
fun StatusRibbon(selectedComponent: String, zoom: Float) {
    Surface(
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选中组件：$selectedComponent",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "画布缩放：${(zoom * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun StudioHomePreview() {
    XiaoYunTheme {
        StudioHomeScreen()
    }
}
