package org.futo.circles.core.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import org.futo.circles.extensions.getUri
import org.futo.circles.model.MediaContentData
import org.futo.circles.provider.MatrixSessionProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    @Throws(IOException::class)
    fun createVideoFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile("VIDEO_${timeStamp}_", ".mp4", storageDir)
    }

    private suspend fun downloadEncryptedFile(
        contentData: MediaContentData,
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
        contentData: MediaContentData
    ): Uri? = downloadEncryptedFile(contentData)?.getUri(context)
}