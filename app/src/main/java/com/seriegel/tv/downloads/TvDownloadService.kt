package com.seriegel.tv.downloads

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.media3.common.util.NotificationUtil
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.R as Media3ExoPlayerR

class TvDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    0,
) {
    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
    }

    override fun getDownloadManager(): DownloadManager {
        return DownloadInfrastructure.get(this).downloadManager
    }

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification {
        val helper = androidx.media3.exoplayer.offline.DownloadNotificationHelper(this, CHANNEL_ID)
        return helper.buildProgressNotification(
            this,
            Media3ExoPlayerR.drawable.exo_notification_small_icon,
            null,
            null,
            downloads,
            notMetRequirements,
        )
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "seriesgal_downloads"

        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                    manager.createNotificationChannel(
                        NotificationChannel(
                            CHANNEL_ID,
                            "SeriesGal Descargas",
                            NotificationManager.IMPORTANCE_LOW,
                        ),
                    )
                }
            } else {
                NotificationUtil.createNotificationChannel(
                    context,
                    CHANNEL_ID.hashCode(),
                    android.R.string.dialog_alert_title,
                    android.R.string.dialog_alert_title,
                    NotificationUtil.IMPORTANCE_LOW,
                )
            }
        }
    }
}
