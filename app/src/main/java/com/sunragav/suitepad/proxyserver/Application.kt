package com.sunragav.suitepad.proxyserver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O

class WebServerApplication : Application(){

    companion object {
        const val CHANNEL_ID = "webserverChannel2"
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if(SDK_INT>=O){
           val notification = NotificationChannel(
               CHANNEL_ID,
                "ProxyServer channel",
                IMPORTANCE_DEFAULT)

            getSystemService(NotificationManager::class.java)?.apply {
                createNotificationChannel(notification)
            }
        }
    }

}