package com.example.ui.components

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    private var isScanning = true
    private var lastAnalyzedTimestamp = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // Throttle frame processing to avoid high CPU usage and log/audit rate limits
        if (!isScanning || currentTime - lastAnalyzedTimestamp < 250L) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            lastAnalyzedTimestamp = currentTime
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { code ->
                            if (code.isNotBlank() && isScanning) {
                                isScanning = false
                                onBarcodeDetected(code)
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // Ignore transient analysis errors
                }
                .addOnCompleteListener {
                    try {
                        imageProxy.close()
                    } catch (e: Exception) {
                        // ignore if closed already
                    }
                }
        } else {
            imageProxy.close()
        }
    }

    fun close() {
        isScanning = false
        try {
            scanner.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        isScanning = true
    }
}
