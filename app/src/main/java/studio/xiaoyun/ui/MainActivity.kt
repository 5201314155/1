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
import androidx.compose.foundation.layout.align
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.foundation.layout.weight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Composable
fun SelectedComponentSummary(
    selectedComponentId: Int?,
    selectedComponent: String,
    zoom: Float,
    components: List<CanvasComponent>,
    device: DeviceProfile,
    onUpdateRegion: (Int, CanvasRegion) -> Unit,
    onUpdateSize: (Int, Int?, Int?) -> Unit,
    onUpdateOpacity: (Int, Float) -> Unit,
    onUpdateAlignment: (Int, CanvasAlignment) -> Unit,
    onUpdatePadding: (Int, Int) -> Unit
) {
    val target = components.firstOrNull { it.id == selectedComponentId }
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
            Text(text = "已选组件", fontWeight = FontWeight.SemiBold)
            if (target == null) {
                Text(
                    text = "暂无选中组件，点击画布或图层列表聚焦",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "名称：${target.name} · ID #${target.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "基于假手机：${device.name}（${device.logicalSize}）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "缩放：${(zoom * 100).toInt()}% · 区域：${target.region.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RegionSelector(target = target, onUpdateRegion = onUpdateRegion)
                    AlignmentAndPaddingEditor(
                        target = target,
                        onUpdateAlignment = onUpdateAlignment,
                        onUpdatePadding = onUpdatePadding
                    )
                    SizeAndOpacityEditor(
                        target = target,
                        device = device,
                        onUpdateSize = onUpdateSize,
                        onUpdateOpacity = onUpdateOpacity
                    )
                }
            }
        }
    }
}

@Composable
fun RegionSelector(target: CanvasComponent, onUpdateRegion: (Int, CanvasRegion) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("区域放置", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasRegion.values().forEach { region ->
                OutlinedButton(
                    onClick = { onUpdateRegion(target.id, region) },
                    enabled = !target.locked,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (target.region == region) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                    )
                ) {
                    Text(region.label)
                }
            }
        }
    }
}

@Composable
fun AlignmentAndPaddingEditor(
    target: CanvasComponent,
    onUpdateAlignment: (Int, CanvasAlignment) -> Unit,
    onUpdatePadding: (Int, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("对齐与内边距", fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasAlignment.values().forEach { alignment ->
                OutlinedButton(
                    onClick = { onUpdateAlignment(target.id, alignment) },
                    enabled = !target.locked,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (target.alignment == alignment) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent
                    )
                ) {
                    Text(alignment.label)
                }
            }
        }
        Text("内边距：${target.paddingDp} dp（按假手机宽度预留空间）")
        Slider(
            value = target.paddingDp.toFloat(),
            onValueChange = { onUpdatePadding(target.id, it.toInt()) },
            valueRange = 0f..48f,
            enabled = !target.locked
        )
    }
}

@Composable
fun SizeAndOpacityEditor(
    target: CanvasComponent,
    device: DeviceProfile,
    onUpdateSize: (Int, Int?, Int?) -> Unit,
    onUpdateOpacity: (Int, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("尺寸与透明度", fontWeight = FontWeight.Medium)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val maxWidth = (device.widthDp - target.paddingDp * 2).coerceAtLeast(120)
            val regionHeightFactor = when (target.region) {
                CanvasRegion.Top -> 0.28f
                CanvasRegion.Middle -> 0.46f
                CanvasRegion.Bottom -> 0.26f
            }
            val maxHeight = (device.heightDp * regionHeightFactor).toInt().coerceAtLeast(96)
            Text("宽度：${target.widthDp} dp / 限制 ${maxWidth}dp（按手机宽度）")
            Slider(
                value = target.widthDp.toFloat(),
                onValueChange = { onUpdateSize(target.id, it.toInt().coerceAtMost(maxWidth), null) },
                valueRange = 96f..maxWidth.toFloat(),
                enabled = !target.locked
            )
            Text("高度：${target.heightDp} dp / 区域上限 ${maxHeight}dp")
            Slider(
                value = target.heightDp.toFloat(),
                onValueChange = { onUpdateSize(target.id, null, it.toInt().coerceAtMost(maxHeight)) },
                valueRange = 64f..maxHeight.toFloat(),
                enabled = !target.locked
            )
            Text("透明度：${(target.opacity * 100).toInt()}%")
            Slider(
                value = target.opacity,
                onValueChange = { onUpdateOpacity(target.id, it) },
                valueRange = 0.2f..1f,
                enabled = !target.locked,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = if (target.locked) "已锁定：解锁后才能修改" else "同步应用：预览即真实效果",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class CanvasRegion(val label: String) {
    Top("顶部"),
    Middle("中部"),
    Bottom("底部")
}

data class CanvasComponent(
    val id: Int,
    val name: String,
    val region: CanvasRegion = CanvasRegion.Middle,
    val widthDp: Int = 240,
    val heightDp: Int = 96,
    val opacity: Float = 1f,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val alignment: CanvasAlignment = CanvasAlignment.Center,
    val paddingDp: Int = 16
)

enum class CanvasAlignment(val label: String, val alignment: Alignment) {
    Start("左对齐", Alignment.CenterStart),
    Center("居中", Alignment.Center),
    End("右对齐", Alignment.CenterEnd)
}

data class DeviceProfile(
    val name: String,
    val logicalSize: String,
    val note: String,
    val widthDp: Int,
    val heightDp: Int
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StudioHomeScreen() {
    var paletteSelection by remember { mutableStateOf("按钮") }
    var focusedComponentId by remember { mutableStateOf<Int?>(null) }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var nextId by remember { mutableStateOf(1) }
    var previewMode by remember { mutableStateOf(false) }
    val deviceProfiles = listOf(
        DeviceProfile(name = "小屏 720p", logicalSize = "360x720dp", note = "入门机型", widthDp = 360, heightDp = 720),
        DeviceProfile(name = "主流 1080p", logicalSize = "411x891dp", note = "大多数机型", widthDp = 411, heightDp = 891),
        DeviceProfile(name = "大屏 2K", logicalSize = "480x960dp", note = "平板/大屏", widthDp = 480, heightDp = 960)
    )
    var selectedDevice by remember { mutableStateOf(deviceProfiles[1]) }
    val canvasComponents = remember { mutableStateListOf<CanvasComponent>() }

    fun addComponentToCanvas(name: String) {
        canvasComponents.add(
            CanvasComponent(
                id = nextId,
                name = name,
                region = CanvasRegion.Middle
            )
        )
        focusedComponentId = nextId
        nextId += 1
    }

    fun updateComponent(id: Int, transform: (CanvasComponent) -> CanvasComponent) {
        val index = canvasComponents.indexOfFirst { it.id == id }
        if (index >= 0) {
            canvasComponents[index] = transform(canvasComponents[index])
        }
    }

    fun toggleVisibility(id: Int) {
        updateComponent(id) { it.copy(visible = !it.visible) }
    }

    fun toggleLock(id: Int) {
        updateComponent(id) { it.copy(locked = !it.locked) }
    }

    fun updateRegion(id: Int, region: CanvasRegion) {
        updateComponent(id) { it.copy(region = region) }
    }

    fun updateSize(id: Int, width: Int? = null, height: Int? = null) {
        updateComponent(id) {
            it.copy(
                widthDp = width ?: it.widthDp,
                heightDp = height ?: it.heightDp
            )
        }
    }

    fun updateOpacity(id: Int, alpha: Float) {
        updateComponent(id) { it.copy(opacity = alpha.coerceIn(0.2f, 1f)) }
    }

    fun updateAlignment(id: Int, alignment: CanvasAlignment) {
        updateComponent(id) { it.copy(alignment = alignment) }
    }

    fun updatePadding(id: Int, padding: Int) {
        updateComponent(id) {
            val safePadding = padding.coerceIn(0, 64)
            val widthLimit = (selectedDevice.widthDp - safePadding * 2).coerceAtLeast(96)
            it.copy(
                paddingDp = safePadding,
                widthDp = it.widthDp.coerceAtMost(widthLimit)
            )
        }
    }

    val focusedComponentName =
        canvasComponents.firstOrNull { it.id == focusedComponentId }?.name ?: paletteSelection

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
            StatusRibbon(selectedComponent = focusedComponentName, zoom = zoom)
            Row(modifier = Modifier.weight(1f)) {
                ComponentLibraryPanel(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight(),
                    selectedComponent = paletteSelection,
                    onComponentSelected = { paletteSelection = it },
                    onAddToCanvas = { addComponentToCanvas(it) }
                )
                CanvasPreview(
                    modifier = Modifier.weight(1f),
                    selectedComponentName = focusedComponentName,
                    selectedComponentId = focusedComponentId,
                    zoom = zoom,
                    previewMode = previewMode,
                    selectedDevice = selectedDevice,
                    availableDevices = deviceProfiles,
                    onTogglePreview = { previewMode = !previewMode },
                    onZoomChange = { zoom = it },
                    onDeviceChange = { selectedDevice = it },
                    components = canvasComponents,
                    onSelectComponent = { id ->
                        focusedComponentId = id
                        paletteSelection = canvasComponents.firstOrNull { it.id == id }?.name ?: paletteSelection
                    }
                )
                PropertyPanel(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight(),
                    selectedComponent = focusedComponentName,
                    selectedComponentId = focusedComponentId,
                    zoom = zoom,
                    device = selectedDevice,
                    components = canvasComponents,
                    onSelectComponent = { id ->
                        focusedComponentId = id
                        paletteSelection = canvasComponents.firstOrNull { it.id == id }?.name ?: paletteSelection
                    },
                    onToggleVisible = { toggleVisibility(it) },
                    onToggleLock = { toggleLock(it) },
                    onUpdateRegion = { id, region -> updateRegion(id, region) },
                    onUpdateSize = { id, width, height -> updateSize(id, width, height) },
                    onUpdateOpacity = { id, alpha -> updateOpacity(id, alpha) },
                    onUpdateAlignment = { id, alignment -> updateAlignment(id, alignment) },
                    onUpdatePadding = { id, padding -> updatePadding(id, padding) }
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
    selectedComponentName: String,
    selectedComponentId: Int?,
    zoom: Float,
    previewMode: Boolean,
    selectedDevice: DeviceProfile,
    availableDevices: List<DeviceProfile>,
    onTogglePreview: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onDeviceChange: (DeviceProfile) -> Unit,
    components: List<CanvasComponent>,
    onSelectComponent: (Int) -> Unit
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
                selectedComponent = selectedComponentName,
                selectedComponentId = selectedComponentId,
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
    selectedComponentId: Int?,
    zoom: Float,
    previewMode: Boolean,
    device: DeviceProfile,
    components: List<CanvasComponent>,
    onSelectComponent: (Int) -> Unit
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
            PhoneCanvasArea(
                modifier = Modifier.weight(1f),
                zoom = zoom,
                device = device,
                previewMode = previewMode,
                selectedComponent = selectedComponent,
                selectedComponentId = selectedComponentId,
                components = components,
                onSelectComponent = onSelectComponent
            )
            FakeBottomBar(previewMode = previewMode, device = device)
        }
    }
}

@Composable
fun PhoneCanvasArea(
    modifier: Modifier = Modifier,
    zoom: Float,
    device: DeviceProfile,
    previewMode: Boolean,
    selectedComponent: String,
    selectedComponentId: Int?,
    components: List<CanvasComponent>,
    onSelectComponent: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (previewMode) "预览中：进入假手机屏幕" else "编辑中：画布即假手机",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "当前选中：$selectedComponent · 缩放 ${(zoom * 100).toInt()}% · ${device.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f)
            )
            Text(
                text = "上/中/下三区分，组件属性仅基于假手机尺寸计算",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.7f)
            )
        }
        CanvasRegionBlock(
            region = CanvasRegion.Top,
            zoom = zoom,
            device = device,
            components = components,
            selectedComponentId = selectedComponentId,
            previewMode = previewMode,
            onSelectComponent = onSelectComponent
        )
        CanvasRegionBlock(
            region = CanvasRegion.Middle,
            zoom = zoom,
            device = device,
            components = components,
            selectedComponentId = selectedComponentId,
            previewMode = previewMode,
            onSelectComponent = onSelectComponent
        )
        CanvasRegionBlock(
            region = CanvasRegion.Bottom,
            zoom = zoom,
            device = device,
            components = components,
            selectedComponentId = selectedComponentId,
            previewMode = previewMode,
            onSelectComponent = onSelectComponent
        )
    }
}

@Composable
fun CanvasRegionBlock(
    region: CanvasRegion,
    zoom: Float,
    device: DeviceProfile,
    components: List<CanvasComponent>,
    selectedComponentId: Int?,
    previewMode: Boolean,
    onSelectComponent: (Int) -> Unit
) {
    val regionComponents = components.filter { it.region == region && it.visible }
    val regionHeight = when (region) {
        CanvasRegion.Top -> (device.heightDp * 0.24f).toInt()
        CanvasRegion.Middle -> (device.heightDp * 0.52f).toInt()
        CanvasRegion.Bottom -> (device.heightDp * 0.24f).toInt()
    }
    val availableWidth = device.widthDp
    val bgColor = when (region) {
        CanvasRegion.Top -> Color(0xFFEEF2FF)
        CanvasRegion.Middle -> Color(0xFFF7F2EA)
        CanvasRegion.Bottom -> Color(0xFFE7F5F0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor.copy(alpha = if (previewMode) 0.8f else 1f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${region.label}区域 · ${regionComponents.size} 个", fontWeight = FontWeight.Medium)
                Text(
                    text = "宽 ${availableWidth}dp · 高 ${regionHeight}dp · ${if (previewMode) "预览=真机效果" else "编辑=假手机布局"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (regionComponents.isEmpty()) {
                Text(
                    text = "暂未放置组件",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (regionComponents.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "移动端适配：宽度<=${availableWidth}dp · 区域高度<=${regionHeight}dp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                regionComponents.forEach { component ->
                    val isSelected = component.id == selectedComponentId
                    val maxRegionWidth = (availableWidth - component.paddingDp * 2).coerceAtLeast(96)
                    val scaledWidth = (component.widthDp.coerceAtMost(maxRegionWidth) * zoom).dp
                    val scaledHeight = (component.heightDp * zoom).dp
                    val alignment = when (component.alignment) {
                        CanvasAlignment.Start -> Alignment.CenterStart
                        CanvasAlignment.Center -> Alignment.Center
                        CanvasAlignment.End -> Alignment.CenterEnd
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else Color.White
                            )
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(component.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${component.widthDp}x${component.heightDp} dp · 透明度 ${(component.opacity * 100).toInt()}% · ${component.alignment.label} · 内边距 ${component.paddingDp}dp",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(onClick = { onSelectComponent(component.id) }) {
                                    Text(if (isSelected) "已聚焦" else "聚焦")
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(scaledHeight)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = component.opacity * 0.4f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (previewMode) "预览视图" else "编辑视图",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(alignment)
                                        .padding(horizontal = component.paddingDp.dp)
                                        .size(width = scaledWidth, height = scaledHeight)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = component.opacity * 0.6f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyPanel(
    modifier: Modifier = Modifier,
    selectedComponent: String,
    selectedComponentId: Int?,
    zoom: Float,
    device: DeviceProfile,
    components: List<CanvasComponent>,
    onSelectComponent: (Int) -> Unit,
    onToggleVisible: (Int) -> Unit,
    onToggleLock: (Int) -> Unit,
    onUpdateRegion: (Int, CanvasRegion) -> Unit,
    onUpdateSize: (Int, Int?, Int?) -> Unit,
    onUpdateOpacity: (Int, Float) -> Unit,
    onUpdateAlignment: (Int, CanvasAlignment) -> Unit,
    onUpdatePadding: (Int, Int) -> Unit
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
            SelectedComponentSummary(
                selectedComponentId = selectedComponentId,
                selectedComponent = selectedComponent,
                zoom = zoom,
                components = components,
                device = device,
                onUpdateRegion = onUpdateRegion,
                onUpdateSize = onUpdateSize,
                onUpdateOpacity = onUpdateOpacity,
                onUpdateAlignment = onUpdateAlignment,
                onUpdatePadding = onUpdatePadding
            )
            LayerList(
                components = components,
                onSelectComponent = onSelectComponent,
                onToggleVisible = onToggleVisible,
                onToggleLock = onToggleLock
            )
            PropertyGroup(title = "交互", items = listOf("点击：预览模式", "双指：缩放", "长按：显示菜单"))
            PropertyGroup(title = "适配", items = listOf("独立于真机参数", "兼容所有尺寸", "预览即真实效果"))
        }
    }
}

@Composable
fun LayerList(
    components: List<CanvasComponent>,
    onSelectComponent: (Int) -> Unit,
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
                                Button(onClick = { onSelectComponent(component.id) }) {
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
                SuggestionChip(
                    onClick = { onDeviceChange(device) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (device == selectedDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    label = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(device.name, fontWeight = FontWeight.Medium)
                            Text(device.logicalSize, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            }
        }
        Text(
            text = "仅按手机端尺寸计算属性，默认兼容多机型",
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
    onSelectComponent: (Int) -> Unit,
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
                    SuggestionChip(
                        onClick = { onSelectComponent(component.id) },
                        label = { Text(text = component.name) }
                    )
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
