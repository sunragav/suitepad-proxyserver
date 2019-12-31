package com.sunragav.suitepad.data

import android.net.Uri
import com.sunragav.suitepad.data.contract.LocalRepository
import com.sunragav.suitepad.data.contract.RemoteRepository
import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import com.sunragav.suitepad.data.remotedata.qualifiers.Background
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    @Background private val background: Scheduler
) : Repository {
    override fun getString(
        uri: Uri,
        disposable: CompositeDisposable
    ): String {
        return localRepository.getString(uri).map { localResponse ->
            if (localResponse.isNullOrBlank()) {
                retrieveFromRemoteAndSave(uri, disposable)
            } else {
                localResponse
            }
        }.doOnSubscribe { disposable.add(it) }
            .blockingGet()
    }

    private fun retrieveFromRemoteAndSave(
        uri: Uri,
        disposable: CompositeDisposable
    ): String {
        return when {

            uri.toString().endsWith("json") -> {
                remoteRepository.getSampleJson().map { remoteResponse ->
                    saveToLocalRepo(uri, remoteResponse, disposable)
                }.doOnSubscribe { disposable.add(it) }
                    .blockingGet()
            }
            uri.toString().endsWith("html") -> {

                remoteRepository.getHtml().map { remoteResponse ->
                    saveToLocalRepo(uri, remoteResponse, disposable)
                }.doOnSubscribe { disposable.add(it) }
                    .blockingGet()
            }
            else -> "Unknown request"
        }

    }

    private fun saveToLocalRepo(
        uri: Uri,
        remoteResponse: List<SuitepadRemoteData>,
        disposable: CompositeDisposable
    ): String {
        var result = "No content to save"
        if (remoteResponse.isNotEmpty())
            result = localRepository.putSampleJson(uri, remoteResponse)
                .doOnSubscribe { disposable.add(it) }
                .blockingGet()
        return result
    }

    private fun saveToLocalRepo(
        uri: Uri,
        remoteResponse: String,
        disposable: CompositeDisposable
    ): String {
        var result = "No content to save"
        if (remoteResponse.isNotBlank())
            result = localRepository.putSampleHtml(uri, remoteResponse)
                .doOnSubscribe { disposable.add(it) }
                .blockingGet()
        return result
    }
}