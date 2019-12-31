package com.sunragav.suitepad.data.localdata

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.Gson
import com.sunragav.suitepad.data.contract.LocalRepository
import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import com.sunragav.suitepad.data.remotedata.qualifiers.Background
import io.reactivex.Scheduler
import io.reactivex.Single
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import javax.inject.Inject

class LocalRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    @Background private val background: Scheduler
) : LocalRepository {
    override fun getString(uri: Uri): Single<String> {
        return Single.fromCallable {
            readUri(uri)
        }.subscribeOn(background)
    }

    override fun putSampleJson(
        jsonUri: Uri,
        suitepadRemoteDataList: List<SuitepadRemoteData>
    ): Single<String> {
        return Single.fromCallable {
            writeToUri(jsonUri, Gson().toJson(suitepadRemoteDataList))
        }
    }

    override fun putSampleHtml(htmlUri: Uri, input: String): Single<String> {
        return Single.fromCallable {
            writeToUri(htmlUri, input)
        }
    }

    private fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String {
        return this.bufferedReader(charset).use { it.readText() }
    }

    private fun OutputStream.writeTextAndClose(str: String, charset: Charset = Charsets.UTF_8) {
        return this.bufferedWriter(charset).use { it.write(str) }
    }

    private fun readUri(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.readTextAndClose() ?: ""
    }


    private fun writeToUri(uri: Uri, input: String): String {
        contentResolver.openOutputStream(uri)?.writeTextAndClose(input)
        return input
    }


}