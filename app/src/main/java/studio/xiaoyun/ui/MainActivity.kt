package studio.xiaoyun.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableFloatStateOf
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

@Composable
fun StudioHomeScreen() {
    var selectedComponent by remember { mutableStateOf("按钮") }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var nextId by remember { mutableStateOf(1) }
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
                    onZoomChange = { zoom = it },
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
    onZoomChange: (Float) -> Unit,
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
                components = components,
                onSelectComponent = onSelectComponent
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { }) {
                Text("实时预览")
            }
            Button(onClick = { }) {
                Text("生成代码")
            }
        }
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
            Spacer(
                modifier = Modifier
                    .size(width = 60.dp, height = 6.dp)
                    .clip(RoundedCornerShape(100))
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "画布预览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CanvasPreviewContent(
                            selectedComponent = selectedComponent,
                            zoom = zoom,
                            components = components,
                            onSelectComponent = onSelectComponent
                        )
                    }
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                        Text("进入应用预览")
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

@Composable
fun CanvasPreviewContent(
    selectedComponent: String,
    zoom: Float,
    components: List<CanvasComponent>,
    onSelectComponent: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Smartphone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "预览等同真机 · ${(zoom * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "画布图层", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "缩放同步：${(zoom * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (components.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击左侧组件库并添加到画布",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    components.forEach { component ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectComponent(component.name) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (component.name == selectedComponent) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = component.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "状态：${if (component.visible) "可见" else "隐藏"} · ${if (component.locked) "已锁定" else "可编辑"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = if (component.name == selectedComponent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = { }) {
                Text("对齐参考线")
            }
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
