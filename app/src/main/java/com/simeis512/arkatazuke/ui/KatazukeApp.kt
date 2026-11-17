package com.simeis512.arkatazuke.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import com.simeis512.arkatazuke.device.CameraProbe
import com.simeis512.arkatazuke.gemini.ClutterAnalysis
import com.simeis512.arkatazuke.gemini.GeminiClient
import com.simeis512.arkatazuke.settings.SettingsStore
import kotlinx.coroutines.launch

/**
 * ステップ1の画面。XR 環境では空間パネル1枚に、通常の Android では全画面に表示する。
 */
@Composable
fun KatazukeApp() {
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        Subspace {
            SpatialPanel(
                SubspaceModifier.width(900.dp).height(1100.dp).movable().resizable()
            ) {
                Surface { MainScreen(Modifier.fillMaxSize().padding(24.dp)) }
            }
        }
    } else {
        Surface { MainScreen(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) }
    }
}

@Composable
private fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf(store.apiKey) }
    var model by remember { mutableStateOf(store.model) }
    var image by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ClutterAnalysis?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cameraInfo by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            image = ImageUtil.loadScaled(context, uri)
            result = null
            error = null
        }
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) {
            image = ImageUtil.scaleDown(bmp)
            result = null
            error = null
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("AR片付け（ステップ1: Gemini 接続テスト）", style = MaterialTheme.typography.headlineSmall)

        // --- 設定 ---
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("Gemini APIキー") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("モデル名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = {
            store.apiKey = apiKey
            store.model = model
            model = store.model
        }) { Text("設定を保存") }

        HorizontalDivider()

        // --- 画像の取得 ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("写真を選ぶ") }
            OutlinedButton(onClick = {
                runCatching { takePicture.launch(null) }
                    .onFailure { error = "カメラアプリを起動できませんでした: ${it.message}" }
            }) { Text("撮影する") }
        }
        image?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "解析する写真",
                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
            )
        }

        Button(
            enabled = image != null && !loading,
            onClick = {
                val bmp = image ?: return@Button
                store.apiKey = apiKey
                store.model = model
                loading = true
                error = null
                result = null
                scope.launch {
                    try {
                        result = GeminiClient(store.apiKey, store.model)
                            .analyzeClutter(ImageUtil.toJpeg(bmp))
                    } catch (e: Exception) {
                        error = e.message ?: e.toString()
                    } finally {
                        loading = false
                    }
                }
            },
        ) { Text("Gemini で片付け対象を探す") }

        if (loading) CircularProgressIndicator()
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let { AnalysisView(it) }

        HorizontalDivider()

        // --- 実機確認 ---
        Text("実機の確認", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { cameraInfo = CameraProbe.describe(context) }) {
            Text("カメラ一覧を調べる")
        }
        cameraInfo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun AnalysisView(analysis: ClutterAnalysis) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(analysis.summary, style = MaterialTheme.typography.bodyLarge)
        if (analysis.items.isEmpty()) Text("片付け対象は見つかりませんでした")
        analysis.items.forEach { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "優先度${item.priority}  ${item.name}（${item.category}）",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text("場所: ${item.whereInImage}", style = MaterialTheme.typography.bodySmall)
                    Text("どうする: ${item.suggestedAction}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
