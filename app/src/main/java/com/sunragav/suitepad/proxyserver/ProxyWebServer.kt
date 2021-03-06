package com.sunragav.suitepad.proxyserver

import android.content.ClipData
import android.content.Intent
import android.os.*
import com.sunragav.suitepad.data.Repository
import com.sunragav.suitepad.nativelib.KeyStoreHelper
import com.sunragav.suitepad.proxyserver.BuildConfig.GET_URI_ACTION
import dagger.android.DaggerService
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.security.KeyStore
import javax.inject.Inject
import javax.net.ssl.KeyManagerFactory


class ProxyWebServer : DaggerService() {
    @Inject
    lateinit var intents: Map<String, @JvmSuppressWildcards Intent>
    @Inject
    lateinit var repository: Repository

    private val background = Schedulers.io()
    private val foreground: Scheduler = AndroidSchedulers.mainThread()

    private var isBound = false
    private val messengerToReceiveMsgFromRemoteActivity =
        Messenger(IncomingHandler(WeakReference(this)))
    private lateinit var messengerToSendMsgToRemoteActivity: Messenger

    private lateinit var server: SuitePadHttpServer

    private val disposable = CompositeDisposable()

    override fun onBind(intent: Intent): IBinder? {
        return messengerToReceiveMsgFromRemoteActivity.binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
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
        }
    }


    private fun initHttpServerAndInformBoundClient(clipData: ClipData) {
        Completable.fromCallable {
            if (::server.isInitialized.not()) {
                val keyStoreStream = resources.openRawResource(R.raw.localhost)
                val pass = KeyStoreHelper().getPass().toCharArray()
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(keyStoreStream, pass)
                val keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                keyManagerFactory.init(keyStore, pass)
                server = SuitePadHttpServer(
                    port = PORT,
                    keyStore = keyStore,
                    keyStoreManagerFactory = keyManagerFactory,
                    dataSource = repository,
                    htmlUri = clipData.getItemAt(0).uri,
                    jsonUri = clipData.getItemAt(1).uri
                )
            }
            if (server.isAlive.not())
                server.start(SOCKET_READ_TIMEOUT, false)
        }.doOnSubscribe {
            disposable.add(it)
        }.subscribeOn(background)
            .observeOn(foreground)
            .andThen {
                messengerToSendMsgToRemoteActivity.send(
                    Message.obtain(
                        null,
                        MSG_HTTP_SERVER_STARTED
                    ).also {
                        it.data = Bundle().apply {
                            putInt("port", server.listeningPort)
                        }
                    })
            }.subscribe()
    }


    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    private fun stopServer() {
        Completable.fromCallable {
            if (::server.isInitialized)
                server.stop()
        }.doOnSubscribe {
            disposable.add(it)
        }.subscribeOn(background)
            .observeOn(foreground)
            .andThen {
                disposable.dispose()
            }.subscribe()
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
        private const val FILE_PROVIDER = "FileProvider"
        private const val MSG_HTTP_SERVER_STARTED = 2
        private const val NOTIFY_WEBVIEW_WHEN_HTTP_SERVER_STARTS = 1

    }
}


