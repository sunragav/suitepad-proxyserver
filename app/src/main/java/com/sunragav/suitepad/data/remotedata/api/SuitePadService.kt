package com.sunragav.suitepad.data.remotedata.api

import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import io.reactivex.Single
import retrofit2.http.GET


interface SuitePadService {

    @GET("sample.json")
    fun getSampleJson(): Single<List<SuitepadRemoteData>>

    @GET("sample.html")
    fun getSampleHtml(): Single<String>

}