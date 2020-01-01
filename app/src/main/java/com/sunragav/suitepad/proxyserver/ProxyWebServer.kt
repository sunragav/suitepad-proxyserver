package com.sunragav.suitepad.proxyserver

import android.app.PendingIntent
import android.content.ClipData
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import app.WebServerApplication.Companion.CHANNEL_ID
import com.sunragav.suitepad.data.Repository
import com.sunragav.suitepad.proxyserver.BuildConfig.GET_URI_ACTION
import com.sunragav.suitepad.proxyserver.BuildConfig.MOVE_TO_FOREGROUND_ACTION
import dagger.android.DaggerService
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import java.lang.ref.WeakReference
import javax.inject.Inject


class ProxyWebServer : DaggerService() {
    @Inject
    lateinit var intents: Map<String, @JvmSuppressWildcards Intent>
    @Inject
    lateinit var repository: Repository

    private var isBound = false
    private val messengerToReceiveMsgFromRemoteActivity =
        Messenger(IncomingHandler(WeakReference(this)))
    private lateinit var messengerToSendMsgToRemoteActivity: Messenger

    private lateinit var server: SuitePadHttpServer

    override fun onBind(intent: Intent): IBinder? {
        return messengerToReceiveMsgFromRemoteActivity.binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                MOVE_TO_FOREGROUND_ACTION -> {
                    handleMoveToForegroundAction()
                    startActivity(intents[FILE_PROVIDER])
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
            if (isBound)
                initHttpServerAndInformBoundClient(clipData)
            else
                initHttpServer(clipData)
        }
    }

    private fun initHttpServer(clipData: ClipData) {
        if (::server.isInitialized.not())
            server = SuitePadHttpServer(
                port = PORT,
                dataSource = repository,
                htmlUri = clipData.getItemAt(0).uri,
                jsonUri = clipData.getItemAt(1).uri
            )

        if (server.isAlive.not()) {
            server.start(SOCKET_READ_TIMEOUT, false)
            sendBroadcast(intents["Broadcast"].also {
                it?.putExtra("port", server.listeningPort)
            })
        }
    }


    private fun initHttpServerAndInformBoundClient(clipData: ClipData) {
        if (::server.isInitialized.not())
            server = SuitePadHttpServer(
                port = PORT,
                dataSource = repository,
                htmlUri = clipData.getItemAt(0).uri,
                jsonUri = clipData.getItemAt(1).uri
            )

        if (server.isAlive.not()) {
            server.start(SOCKET_READ_TIMEOUT, false)
            messengerToSendMsgToRemoteActivity.send(
                Message.obtain(
                    null,
                    MSG_HTTP_SERVER_STARTED
                ).also {
                    it.data = Bundle().apply {
                        putInt("port", server.listeningPort)
                    }
                })
        }
    }


    private fun handleMoveToForegroundAction() {
        val pendingIntent = PendingIntent.getActivity(this, 0, intents[WEB_VIEW], 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.pendingIntentTitle))
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1234, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
    }

    class IncomingHandler(private val serviceRef: WeakReference<ProxyWebServer>) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NOTIFY_WEBVIEW_WHEN_HTTP_SERVER_STARTS -> {
                    with(serviceRef.get()!!) {
                        isBound = true
                        messengerToSendMsgToRemoteActivity = msg.replyTo
                        startActivity(intents[FILE_PROVIDER])
                    }
                }
            }
        }
    }

    companion object {
        private const val PORT = 8091
        private const val WEB_VIEW = "WebView"
        private const val FILE_PROVIDER = "FileProvider"
        private const val MSG_HTTP_SERVER_STARTED = 2
        private const val NOTIFY_WEBVIEW_WHEN_HTTP_SERVER_STARTS = 1

    }
}


