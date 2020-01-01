package com.sunragav.suitepad.proxyserver

import android.net.Uri
import com.sunragav.suitepad.data.Repository
import fi.iki.elonen.NanoHTTPD
import io.reactivex.disposables.CompositeDisposable

class SuitePadHttpServer(
    port: Int,
    private val htmlUri: Uri,
    private val jsonUri: Uri,
    private var dataSource: Repository

) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        var result = "No proper response"

        when (session.method) {
            Method.GET -> {
                val disposable = CompositeDisposable()

                val urlString = session.uri.toString()
                when {
                    urlString.endsWith("sample.html") -> {
                        result = dataSource.getString(htmlUri)
                            .doOnSubscribe { disposable.add(it) }
                            .blockingGet()

                    }
                    urlString.endsWith("sample.json") -> {
                        result = dataSource.getString(jsonUri)
                            .doOnSubscribe { disposable.add(it) }
                            .blockingGet()

                    }
                }

                disposable.dispose()

            }
            else -> result = "Unknown method"
        }
        return newFixedLengthResponse(result)
    }
}