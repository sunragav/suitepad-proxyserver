package com.sunragav.suitepad.proxyserver

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sunragav.suitepad.proxyserver.WebServerApplication.Companion.CHANNEL_ID
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class ProxyWebServer : Service() {

    companion object {
        private const val PORT = 8091
        private const val WEBVIEW_APPLICATION_ID = "com.sunragav.suitepad.webview"
        private const val WEBVIEW_CLASSNAME = "com.sunragav.suitepad.webview.MainActivity"
        private const val BROADCAST_ACTION_PROXY_STARTED =
            "com.sunragav.suitepad.proxy.ProxyServerStarted"

        private const val FILEPROVIDER_APPLICATION_ID = "com.sunragav.suitepad.fileprovider"
        private const val FILEPROVIDER_CLASSNAME =
            "com.sunragav.suitepad.fileprovider.FileProviderActivity"
        private const val GET_URI_ACTION = "com.sunragav.suitepad.GetURIAction"
        private const val MOVE_TO_FOREGROUND_ACTION = "com.sunragav.suitepad.proxy.MoveToForeground"


    }

    private val webviewActivityIntent = Intent().apply {
        component = ComponentName(
            WEBVIEW_APPLICATION_ID,
            WEBVIEW_CLASSNAME
        )
    }

    private val fileProviderActivityIntent = Intent().also {
        it.component = ComponentName(
            FILEPROVIDER_APPLICATION_ID,
            FILEPROVIDER_CLASSNAME
        )
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        it.action = GET_URI_ACTION
    }


    private val broadCastProxyStartedIntent = Intent().apply {
        action =
            BROADCAST_ACTION_PROXY_STARTED
        addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
    }
    private val server =
        SuitePadHTTPServer(this, PORT)
    private val running = AtomicBoolean(true)
    private var htmlUri = AtomicReference<Uri>()
    private var jsonUri = AtomicReference<Uri>()
    private val runnable = Runnable {
        println("Sending broadcast that the Suitepad ProxyServer has started")
        sendBroadcast(broadCastProxyStartedIntent)
        while (running.get()) {
            if (server.wasStarted().not())
                server.start(SOCKET_READ_TIMEOUT, false)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        println("Suitepad Proxy web server Service created!!!!!!")


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let { proxyIntent ->
            when (proxyIntent.action) {
                MOVE_TO_FOREGROUND_ACTION -> {
                    handleStartWebServiceAction()
                    startActivity(fileProviderActivityIntent)
                }
                GET_URI_ACTION -> {
                    println("Got the result back from the Suitepad file provider")
                    val clipData = proxyIntent.clipData
                    if (clipData?.itemCount == 2) {
                        htmlUri.set(clipData.getItemAt(0).uri)
                        jsonUri.set(clipData.getItemAt(1).uri)
                        running.getAndSet(true)
                        Thread(runnable).start()
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStartWebServiceAction() {
        val pendingIntent = PendingIntent.getActivity(this, 0, webviewActivityIntent, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Web Proxy Server for the SuitePad")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        // android.os.Debug.waitForDebugger()
        println("Suitepad Proxy web server moved to foreground!!!!!!")
        startForeground(1234, notification)

    }

    override fun onDestroy() {
        super.onDestroy()
        running.getAndSet(false)
        server.stop()
    }

    inner class SuitePadHTTPServer(private val context: Context, port: Int) : NanoHTTPD(port) {

        override fun serve(session: IHTTPSession): Response {
            var result = "Hello Proxy"

            when (session.method) {
                Method.GET -> {
                    val urlString = session.uri.toString()
                    when {
                        urlString.endsWith("sample.html") -> {
                            println("sample.html requested from Suitepad webserver")
                            if (htmlUri.get() != null) result = readUri(htmlUri.get())
                        }
                        urlString.endsWith("sample.json") -> {
                            println("sample.json requested from Suitepad webserver")
                            if (jsonUri.get() != null) result = readUri(jsonUri.get())
                        }
                    }
                }
                else -> result = "Unknown methods"
            }

            return newFixedLengthResponse(result)
        }

        private fun readUri(uri: Uri): String {
            var inputStream: InputStream? = null
            try {
                inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val buffer = ByteArray(1024)
                    var result: Int
                    var content = ""
                    while (inputStream.read(buffer).also { result = it } != -1) {
                        content += String(buffer, 0, result)
                    }
                    return content
                }
            } catch (e: IOException) {
                println("IOException when reading uri")
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        println("IOException when closing stream")
                    }
                }
            }
            return "No result from file provider"
        }

    }
}


