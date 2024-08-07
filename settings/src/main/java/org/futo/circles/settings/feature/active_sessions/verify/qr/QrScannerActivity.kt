package org.futo.circles.settings.feature.active_sessions.verify.qr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import org.futo.circles.core.base.BaseActivity
import org.futo.circles.settings.R
import org.futo.circles.settings.databinding.ActivityQrScannerBinding

class QrScannerActivity : BaseActivity() {

    private lateinit var binding: ActivityQrScannerBinding
    private var codeScanner: CodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        codeScanner = CodeScanner(this, binding.qrScannerView).apply {
            decodeCallback = DecodeCallback {
                runOnUiThread { handleQrResult(it) }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    showFailedToReadQr()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner?.startPreview()
    }

    override fun onPause() {
        codeScanner?.releaseResources()
        super.onPause()
    }

    private fun getRawBytes(result: Result): ByteArray? {
        val metadata = result.resultMetadata ?: return null
        val segments = metadata[ResultMetadataType.BYTE_SEGMENTS] ?: return null
        var bytes = ByteArray(0)
        @Suppress("UNCHECKED_CAST")
        for (seg in segments as Iterable<ByteArray>) {
            bytes += seg
        }
        return if (bytes.size >= result.text.length) bytes else null
    }

    private fun handleQrResult(rawResult: Result) {
        val rawBytes = getRawBytes(rawResult)
        val rawBytesStr = rawBytes?.toString(Charsets.ISO_8859_1)
        val result = rawBytesStr ?: rawResult.text
        val isQrCode = rawResult.barcodeFormat == BarcodeFormat.QR_CODE
        if (isQrCode) {
            setResult(RESULT_OK, Intent().apply { putExtra(SCANNED_TEXT, result) })
            finish()
        } else {
            showFailedToReadQr()
        }
    }

    private fun showFailedToReadQr() {
        Toast.makeText(
            this@QrScannerActivity,
            getString(R.string.failed_to_read_qr_code),
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val SCANNED_TEXT = "SCANNED_TEXT"

        fun startForResult(
            activity: Activity,
            activityResultLauncher: ActivityResultLauncher<Intent>
        ) {
            activityResultLauncher.launch(Intent(activity, QrScannerActivity::class.java))
        }

        fun getResultText(data: Intent?): String {
            return data?.getStringExtra(SCANNED_TEXT) ?: ""
        }
    }
}