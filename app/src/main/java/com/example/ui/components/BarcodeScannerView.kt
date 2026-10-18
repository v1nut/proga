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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.ui.ScannerState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import java.util.concurrent.Executors

private fun isValidBarcode(raw: String): Boolean {
    val code = raw.trim()
    if (!code.all { it.isDigit() }) return false
    if (code.length !in listOf(8, 12, 13)) return false
    if (code.length == 13) {
        var sum = 0
        for (i in 0 until 12) {
            val digit = code[i].digitToInt()
            val weight = if (i % 2 == 0) 1 else 3
            sum += digit * weight
        }
        val remainder = sum % 10
        val checkDigit = if (remainder == 0) 0 else 10 - remainder
        return checkDigit == code[12].digitToInt()
    }
    if (code.length == 8) {
        var sum = 0
        for (i in 0 until 7) {
            val digit = code[i].digitToInt()
            val weight = if (i % 2 == 0) 3 else 1
            sum += digit * weight
        }
        val remainder = sum % 10
        val checkDigit = if (remainder == 0) 0 else 10 - remainder
        return checkDigit == code[7].digitToInt()
    }
    return true
}

@Composable
fun BarcodeScannerDialog(
    scannerState: ScannerState = ScannerState.Idle,
    onResetScannerState: () -> Unit = {},
    onOpenCreateProduct: (String) -> Unit = {},
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasCameraPermission && cameraError == null) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    cameraProviderRef = cameraProvider

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

                                    previewView.setOnTouchListener { view, event ->
                                        if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                                            val factory = previewView.meteringPointFactory
                                            val point = factory.createPoint(event.x, event.y)
                                            val action = androidx.camera.core.FocusMeteringAction.Builder(point, androidx.camera.core.FocusMeteringAction.FLAG_AF)
                                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                                .build()
                                            boundCamera.cameraControl.startFocusAndMetering(action)
                                            view.performClick()
                                        }
                                        true
                                    }

                                    previewView.post {
                                        try {
                                            val factory = previewView.meteringPointFactory
                                            val centerPoint = factory.createPoint(previewView.width / 2f, previewView.height / 2f)
                                            val action = androidx.camera.core.FocusMeteringAction.Builder(centerPoint, androidx.camera.core.FocusMeteringAction.FLAG_AF)
                                                .setAutoCancelDuration(2, java.util.concurrent.TimeUnit.SECONDS)
                                                .build()
                                            boundCamera.cameraControl.startFocusAndMetering(action)
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }

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
                                tint = WildWatermelon,
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
                                    "Введите штрих-код товара вручную.",
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
                                        containerColor = WildWatermelon,
                                        contentColor = Color(0xFF101014)
                                    )
                                ) {
                                    Text("Разрешить камеру", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Laser Targeting Overlay
                ViewfinderOverlay(modifier = Modifier.fillMaxSize())

                // Searching Card Overlay
                if (scannerState is ScannerState.Searching) {
                    val searchingCode = (scannerState as ScannerState.Searching).barcode
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkCard)
                                .border(1.dp, WildWatermelon, RoundedCornerShape(20.dp))
                                .padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = WildWatermelon,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Поиск штрих-кода...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = searchingCode,
                                fontSize = 13.sp,
                                color = WildWatermelon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Top Controls Bar
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
                            tint = if (isTorchOn) WildWatermelon else TextSecondary
                        )
                    }
                }

                // Bottom Panel: Manual Entry
                val isManualInputValid = manualCodeInput.isBlank() || isValidBarcode(manualCodeInput)

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
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Наведите камеру на штрих-код (EAN-13 / EAN-8 / UPC)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                            isError = !isManualInputValid,
                            supportingText = {
                                if (!isManualInputValid) {
                                    Text("Некорректный штрих-код (8, 12 или 13 цифр)", color = Color(0xFFF87171), fontSize = 11.sp)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_barcode_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WildWatermelon,
                                unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                                errorBorderColor = Color(0xFFF87171),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkCard,
                                unfocusedContainerColor = DarkCard
                            )
                        )

                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank() && isManualInputValid) {
                                    onBarcodeScanned(manualCodeInput.trim())
                                }
                            },
                            enabled = manualCodeInput.isNotBlank() && isManualInputValid,
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

                // In-Scanner "Not Found" Dialog
                if (scannerState is ScannerState.NotFound) {
                    val missingCode = (scannerState as ScannerState.NotFound).barcode
                    AlertDialog(
                        onDismissRequest = {
                            onResetScannerState()
                            analyzer.resume()
                        },
                        containerColor = DarkSurface,
                        shape = RoundedCornerShape(20.dp),
                        title = {
                            Text("Штрих-код не найден", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        },
                        text = {
                            Text(
                                "Штрих-код $missingCode не найден в локальной базе и на сервере Open Food Facts.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onResetScannerState()
                                    onDismiss()
                                    onOpenCreateProduct(missingCode)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WildWatermelon,
                                    contentColor = Color(0xFF101014)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Создать продукт", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    onResetScannerState()
                                    analyzer.resume()
                                }
                            ) {
                                Text("Сканировать ещё", color = TextSecondary)
                            }
                        }
                    )
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

        val boxWidth = (w * 0.72f).coerceAtMost(300.dp.toPx())
        val boxHeight = (boxWidth * 0.65f).coerceAtMost(200.dp.toPx())
        val left = (w - boxWidth) / 2f
        val top = (h - boxHeight) / 3.4f
        val right = left + boxWidth
        val bottom = top + boxHeight

        val cornerLength = 26.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerColor = WildWatermelon

        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), cornerStroke, cap = StrokeCap.Round)

        drawLine(cornerColor, Offset(right, top), Offset(right - cornerLength, top), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), cornerStroke, cap = StrokeCap.Round)

        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLength), cornerStroke, cap = StrokeCap.Round)

        drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLength, bottom), cornerStroke, cap = StrokeCap.Round)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), cornerStroke, cap = StrokeCap.Round)

        val laserY = top + (boxHeight * laserYRatio)
        drawLine(
            color = WildWatermelon,
            start = Offset(left + 8.dp.toPx(), laserY),
            end = Offset(right - 8.dp.toPx(), laserY),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}
