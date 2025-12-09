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
import androidx.compose.material3.Divider
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

@Composable
fun StudioHomeScreen() {
    var selectedComponent by remember { mutableStateOf("按钮") }
    var zoom by remember { mutableFloatStateOf(1.0f) }

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
                    onComponentSelected = { selectedComponent = it }
                )
                CanvasPreview(
                    modifier = Modifier.weight(1f),
                    selectedComponent = selectedComponent,
                    zoom = zoom,
                    onZoomChange = { zoom = it }
                )
                PropertyPanel(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight(),
                    selectedComponent = selectedComponent,
                    zoom = zoom
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
    onComponentSelected: (String) -> Unit
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
    onZoomChange: (Float) -> Unit
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
            PhoneFramePreview(selectedComponent = selectedComponent, zoom = zoom)
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
            Slider(
                value = zoom,
                onValueChange = onZoomChange,
                valueRange = 0.5f..2.5f,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun PhoneFramePreview(selectedComponent: String, zoom: Float) {
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
                        CanvasPreviewContent(selectedComponent = selectedComponent, zoom = zoom)
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
    zoom: Float
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
            PropertyGroup(title = "尺寸与对齐", items = listOf("宽度：匹配画布", "高度：自适应", "对齐：居中"))
            PropertyGroup(title = "样式", items = listOf("圆角：12dp", "背景：主题色", "阴影：柔和"))
            PropertyGroup(title = "交互", items = listOf("点击：预览模式", "双指：缩放", "长按：显示菜单"))
            PropertyGroup(title = "适配", items = listOf("独立于真机参数", "兼容所有尺寸", "预览即真实效果"))
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
fun CanvasPreviewContent(selectedComponent: String, zoom: Float) {
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
                Text(text = "已选组件：$selectedComponent", fontWeight = FontWeight.SemiBold)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$selectedComponent 将以手机适配呈现",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { }) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("对齐参考线")
                    }
                    Text(
                        text = "缩放同步：${(zoom * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
