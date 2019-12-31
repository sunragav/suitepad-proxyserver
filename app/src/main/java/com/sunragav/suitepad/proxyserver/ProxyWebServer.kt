package com.sunragav.suitepad.proxyserver

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.WebServerApplication.Companion.CHANNEL_ID
import com.sunragav.suitepad.data.Repository
import com.sunragav.suitepad.proxyserver.BuildConfig.GET_URI_ACTION
import com.sunragav.suitepad.proxyserver.BuildConfig.MOVE_TO_FOREGROUND_ACTION
import dagger.android.DaggerService
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject


class ProxyWebServer : DaggerService() {

    @Inject
    lateinit var intents: Map<String, @JvmSuppressWildcards Intent>
    @Inject
    lateinit var repository: Repository

    private val server = SuitePadHTTPServer(this, PORT)
    private var htmlUri = AtomicReference<Uri>()
    private var jsonUri = AtomicReference<Uri>()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                MOVE_TO_FOREGROUND_ACTION -> {
                    handleMoveToForegroundAction()
                    startActivity(intents["FileProvider"])
                }
                GET_URI_ACTION -> {
                    handleGetUriAction(it)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun handleGetUriAction(proxyIntent: Intent) {
        val clipData = proxyIntent.clipData
        if (clipData?.itemCount == 2) {
            htmlUri.set(clipData.getItemAt(0).uri)
            jsonUri.set(clipData.getItemAt(1).uri)

            if (server.isAlive.not()) {
                server.start(SOCKET_READ_TIMEOUT, false)

                sendBroadcast(intents["Broadcast"].also {
                    it?.putExtra("port", server.listeningPort)
                })
            }

        }
    }

    private fun handleMoveToForegroundAction() {
        val pendingIntent = PendingIntent.getActivity(this, 0, intents["WebView"], 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Web Proxy Server for the SuitePad")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1234, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
    }

    inner class SuitePadHTTPServer(private val context: Context, port: Int) : NanoHTTPD(port) {
        private var result = ""
        override fun serve(session: IHTTPSession): Response {
            result = "Hello Proxy"

            when (session.method) {
                Method.GET -> {
                    val disposable = CompositeDisposable()

                    //android.os.Debug.waitForDebugger()
                    val urlString = session.uri.toString()
                    when {
                        urlString.endsWith("sample.html") -> {
                            if (htmlUri.get() != null)
                                result = repository.getString(htmlUri.get(), disposable)

                        }
                        urlString.endsWith("sample.json") -> {
                            if (jsonUri.get() != null)
                                result = repository.getString(jsonUri.get(), disposable)

                        }
                    }

                    disposable.dispose()

                }
                else -> result = "Unknown method"
            }
            return newFixedLengthResponse(result)
        }
    }

    companion object {
        private const val PORT = 8091
    }
}


