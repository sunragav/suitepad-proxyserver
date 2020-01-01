package com.sunragav.suitepad.data

import android.net.Uri
import com.sunragav.suitepad.data.contract.LocalRepository
import com.sunragav.suitepad.data.contract.RemoteRepository
import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import com.sunragav.suitepad.data.remotedata.qualifiers.Background
import io.reactivex.Scheduler
import io.reactivex.Single
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
    ): Single<String> {
        return localRepository.getString(uri).flatMap { localResponse ->
            if (localResponse.isNullOrBlank()) {
                retrieveFromRemoteAndSave(uri, disposable).map { it }
            } else {
                Single.just(localResponse)
            }
        }.doOnSubscribe { disposable.add(it) }

    }

    private fun retrieveFromRemoteAndSave(
        uri: Uri,
        disposable: CompositeDisposable
    ): Single<String> {
        return when {
            uri.toString().endsWith("json") -> {
                remoteRepository.getSampleJson().flatMap { remoteResponse ->
                    saveToLocalRepo(uri, remoteResponse, disposable)
                }.doOnSubscribe { disposable.add(it) }

            }
            uri.toString().endsWith("html") -> {

                remoteRepository.getHtml().flatMap { remoteResponse ->
                    saveToLocalRepo(uri, remoteResponse, disposable)
                }.doOnSubscribe { disposable.add(it) }

            }
            else -> Single.just("Unknown request")
        }

    }

    private fun saveToLocalRepo(
        uri: Uri,
        remoteResponse: List<SuitepadRemoteData>,
        disposable: CompositeDisposable
    ): Single<String> {
        if (remoteResponse.isNotEmpty())
            return localRepository.putSampleJson(uri, remoteResponse)
                .doOnSubscribe { disposable.add(it) }
        return Single.just("No content to save")
    }

    private fun saveToLocalRepo(
        uri: Uri,
        remoteResponse: String,
        disposable: CompositeDisposable
    ): Single<String> {
        if (remoteResponse.isNotBlank())
            return localRepository.putSampleHtml(uri, remoteResponse)
                .doOnSubscribe { disposable.add(it) }
        return Single.just("No content to save")
    }
}