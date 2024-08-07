package org.futo.circles.gallery.feature.backup.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.ContentObserver
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.futo.circles.gallery.feature.backup.MediaBackupDataSource
import javax.inject.Inject

@AndroidEntryPoint
class MediaBackupService : Service() {

    private val binder = MediaBackupServiceBinder()
    private val job = SupervisorJob()
    private val backupScope = CoroutineScope(Dispatchers.IO + job)
    @Inject
    lateinit var mediaBackupDataSource: MediaBackupDataSource
    private var backupJob: Job? = null

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (backupJob != null || selfChange) return
            val path = uri?.path ?: return
            backupJob = backupScope.launch {
                mediaBackupDataSource.startBackupByFilePath(path)
                backupJob = null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver
        )
        startBackup()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        contentResolver.unregisterContentObserver(contentObserver);
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun onBackupSettingsUpdated() {
        backupJob?.cancel()
        startBackup()
    }

    private fun startBackup() {
        backupJob = backupScope.launch {
            mediaBackupDataSource.startMediaBackup()
            backupJob = null
        }
    }

    inner class MediaBackupServiceBinder : Binder() {
        fun getService(): MediaBackupService = this@MediaBackupService
    }

    companion object {
        fun bindService(context: Context, connection: ServiceConnection) {
            Intent(context, MediaBackupService::class.java).also { intent ->
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }
}
