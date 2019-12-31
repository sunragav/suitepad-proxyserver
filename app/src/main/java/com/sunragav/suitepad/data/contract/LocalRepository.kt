package com.sunragav.suitepad.data.contract

import android.net.Uri
import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import io.reactivex.Single

interface LocalRepository {
    fun getString(
        uri: Uri
    ): Single<String>

    fun putSampleJson(
        jsonUri: Uri,
        suitepadRemoteDataList: List<SuitepadRemoteData>
    ): Single<String>

    fun putSampleHtml(htmlUri: Uri, input: String): Single<String>
}

