package org.futo.circles.core.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import okio.buffer
import okio.sink
import okio.source
import org.futo.circles.core.extensions.getUri
import org.futo.circles.core.model.MediaFileData
import org.futo.circles.core.model.MediaType
import org.futo.circles.core.provider.MatrixSessionProvider
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    @Throws(IOException::class)
    fun createVideoFile(context: Context): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile("VIDEO_${timeStamp}_", ".mp4", storageDir)
    }

    private suspend fun downloadEncryptedFile(
        contentData: MediaFileData,
        onError: (message: String) -> Unit = {}
    ): File? {
        val session = MatrixSessionProvider.currentSession ?: kotlin.run {
            onError("Session is not active")
            return null
        }
        return runCatching {
            session.fileService().downloadFile(
                fileName = contentData.fileName,
                mimeType = contentData.mimeType,
                url = contentData.fileUrl,
                elementToDecrypt = contentData.elementToDecrypt
            )
        }.fold({ it }, { onError(it.message ?: ""); null }
        )
    }

    suspend fun downloadEncryptedFileToContentUri(
        context: Context,
        contentData: MediaFileData
    ): Uri? = downloadEncryptedFile(contentData)?.getUri(context)

    suspend fun saveMediaFileToDevice(
        context: Context,
        mediaFileData: MediaFileData,
        mediaType: MediaType
    ) {
        val localFile = downloadEncryptedFile(mediaFileData) ?: return

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, mediaFileData.fileName)
            put(MediaStore.Images.Media.DISPLAY_NAME, mediaFileData.fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mediaFileData.mimeType)
        }

        val externalContentUri = when (mediaType) {
            MediaType.Image -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaType.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(externalContentUri, values) ?: return

        val source = localFile.inputStream().source().buffer()
        context.contentResolver.openOutputStream(uri)?.sink()?.buffer()?.let { sink ->
            source.use { input -> sink.use { output -> output.writeAll(input) } }
        }
    }

    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB", "PB", "EB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

}