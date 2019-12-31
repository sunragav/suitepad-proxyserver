package com.sunragav.suitepad.data.contract

import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import io.reactivex.Single

interface RemoteRepository {
    fun getSampleJson(

    ): Single<List<SuitepadRemoteData>>

    fun getHtml(

    ): Single<String>
}
