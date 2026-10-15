package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var manualCodeInput by remember { mutableStateOf("") }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        BarcodeAnalyzer { barcode ->
            onBarcodeScanned(barcode)
        }
    }

    // Clean up camera on disposal to prevent buffer queue abandonment
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                camera?.cameraControl?.enableTorch(false)
                cameraProviderRef?.unbindAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            analyzer.close()
            cameraExecutor.shutdown()
        }
    }

    val demoCodes = listOf(
        "Творог 5%" to "4605432109876",
        "Геркулес" to "4601234567890",
        "Молоко 2.5%" to "4609876543210",
        "Гречка" to "4607001234567",
        "Авокадо" to "4606667778889",
        "Хлеб ц/з" to "4603334445556"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera Preview using TextureView (COMPATIBLE mode) to avoid SurfaceView buffer queue abandonment
                if (hasCameraPermission && cameraError == null) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                // COMPATIBLE mode uses TextureView instead of SurfaceView,
                                // which avoids "BufferQueue has been abandoned" on compose dialog transitions
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    cameraProviderRef = cameraProvider

                                    // Pick available camera
                                    val cameraSelector = when {
                                        cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                                        else -> null
                                    }

                                    if (cameraSelector == null) {
                                        cameraError = "Камера не найдена на устройстве"
                                        return@addListener
                                    }

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(cameraExecutor, analyzer)
                                        }

                                    cameraProvider.unbindAll()
                                    val boundCamera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    camera = boundCamera

                                    // Monitor camera state for errors
                                    boundCamera.cameraInfo.cameraState.observe(lifecycleOwner) { state ->
                                        if (state.type == CameraState.Type.CLOSED && state.error != null) {
                                            cameraError = "Камера эмулятора недоступна"
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    cameraError = "Не удалось инициализировать камеру"
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback visual background when camera is disabled, in emulator or error
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = ElectricLime,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (!hasCameraPermission) "Камера отключена" else "Режим ручного сканирования",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (!hasCameraPermission)
                                    "Предоставьте доступ к камере или используйте ввод штрих-кода ниже"
                                else
                                    "В эмуляторе камера эмулируется. Вы можете быстро выбрать тестовый штрих-код или ввести его вручную.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (!hasCameraPermission) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricLime,
                                        contentColor = Color(0xFF101014)
                                    )
                                ) {
                                    Text("Разрешить камеру", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Laser Targeting Overlay & Viewfinder
                ViewfinderOverlay(modifier = Modifier.fillMaxSize())

                // Top Controls Bar (Close, Title, Torch)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkCard.copy(alpha = 0.85f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = "Сканер штрих-кодов",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkCard.copy(alpha = 0.85f))
                            .clickable {
                                camera?.let { cam ->
                                    if (cam.cameraInfo.hasFlashUnit()) {
                                        isTorchOn = !isTorchOn
                                        cam.cameraControl.enableTorch(isTorchOn)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Фонарик",
                            tint = if (isTorchOn) ElectricLime else TextSecondary
                        )
                    }
                }

                // Bottom Panel: Manual Entry & Demo Barcode Chips
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 24.dp)
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(22.dp))
                        .background(DarkSurface.copy(alpha = 0.98f))
                        .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Наведите камеру на штрих-код (UPC / EAN)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Или выберите пример для быстрой проверки:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Demo Barcode Quick Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(demoCodes) { (name, code) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkCardElevated)
                                    .border(1.dp, WildWatermelon.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable { onBarcodeScanned(code) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Column {
                                    Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = code, fontSize = 10.sp, color = WildWatermelon)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Manual Barcode Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualCodeInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) manualCodeInput = input
                            },
                            placeholder = { Text("Ввести код вручную...", fontSize = 12.sp, color = TextTertiary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_barcode_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WildWatermelon,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkCard,
                                unfocusedContainerColor = DarkCard
                            )
                        )

                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank()) {
                                    onBarcodeScanned(manualCodeInput.trim())
                                }
                            },
                            enabled = manualCodeInput.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WildWatermelon,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("manual_barcode_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Найти", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewfinderOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Reticle box dimensions in upper portion of screen
        val boxWidth = (w * 0.72f).coerceAtMost(300.dp.toPx())
        val boxHeight = (boxWidth * 0.65f).coerceAtMost(200.dp.toPx())
        val left = (w - boxWidth) / 2f
        val top = (h - boxHeight) / 3.4f
        val right = left + boxWidth
        val bottom = top + boxHeight

        val cornerLength = 26.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerColor = WildWatermelon

        // Draw 4 corners
        // Top-Left
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), cornerStroke, cap = StrokeCap.Round)

        // Top-Right
        drawLine(cornerColor, Offset(right, top), Offset(right - cornerLength, top), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), cornerStroke, cap = StrokeCap.Round)

        // Bottom-Left
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLength), cornerStroke, cap = StrokeCap.Round)

        // Bottom-Right
        drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLength, bottom), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), cornerStroke, cap = StrokeCap.Round)

        // Animated WildWatermelon laser line across reticle
        val laserY = top + (boxHeight * laserYRatio)
        drawLine(
            color = WildWatermelon,
            start = Offset(left + 8.dp.toPx(), laserY),
            end = Offset(right - 8.dp.toPx(), laserY),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}
