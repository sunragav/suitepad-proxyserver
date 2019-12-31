package app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import com.sunragav.suitepad.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class WebServerApplication : DaggerApplication() {

    companion object {
        const val CHANNEL_ID = "webserverChannel2"
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        createNotificationChannel()

        return DaggerAppComponent.builder().application(this).build()
    }

    private fun createNotificationChannel() {
        if (SDK_INT >= O) {
            val notification = NotificationChannel(
                CHANNEL_ID,
                "ProxyServer channel",
                IMPORTANCE_DEFAULT
            )

            getSystemService(NotificationManager::class.java)?.apply {
                createNotificationChannel(notification)
            }
        }
    }

}