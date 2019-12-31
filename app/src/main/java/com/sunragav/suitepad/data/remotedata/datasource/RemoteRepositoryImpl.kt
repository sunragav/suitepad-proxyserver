package com.sunragav.suitepad.data.remotedata.datasource

import com.sunragav.suitepad.data.contract.RemoteRepository
import com.sunragav.suitepad.data.remotedata.api.SuitePadService
import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import com.sunragav.suitepad.data.remotedata.qualifiers.Background
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject

class RemoteRepositoryImpl @Inject constructor(
    private val suitePadService: SuitePadService,
    @Background private val backgroundThread: Scheduler

) : RemoteRepository {
    override fun getSampleJson(
    ): Single<List<SuitepadRemoteData>> {
        return suitePadService.getSampleJson().subscribeOn(backgroundThread)
    }

    override fun getHtml(): Single<String> {
        return suitePadService.getSampleHtml().subscribeOn(backgroundThread)
    }
}