package com.sunragav.suitepad.data

import android.net.Uri
import com.sunragav.suitepad.data.contract.LocalRepository
import com.sunragav.suitepad.data.contract.RemoteRepository
import com.sunragav.suitepad.data.remotedata.models.SuitepadRemoteData
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository
) : Repository {
    override fun getString(
        uri: Uri
    ): Single<String> {
        return localRepository.getString(uri).flatMap { localResponse ->
            if (localResponse.isNullOrBlank()) {
                retrieveFromRemoteAndSave(uri).map { it }
            } else {
                Single.just(localResponse)
            }
        }

    }

    private fun retrieveFromRemoteAndSave(
        uri: Uri
    ): Single<String> {
        return when {
            uri.toString().endsWith("json") -> {
                remoteRepository.getSampleJson().flatMap { remoteResponse ->
                    saveToLocalRepo(uri, remoteResponse)
                }

            }
            uri.toString().endsWith("html") -> {

                remoteRepository.getHtml().flatMap { remoteResponse ->
                    saveToLocalRepo(uri, remoteResponse)
                }

            }
            else -> Single.just("Unknown request")
        }

    }

    private fun saveToLocalRepo(
        uri: Uri,
        remoteResponse: List<SuitepadRemoteData>
    ): Single<String> {
        if (remoteResponse.isNotEmpty())
            return localRepository.putSampleJson(uri, remoteResponse)
        return Single.just("No content to save")
    }

    private fun saveToLocalRepo(
        uri: Uri,
        remoteResponse: String
    ): Single<String> {
        if (remoteResponse.isNotBlank())
            return localRepository.putSampleHtml(uri, remoteResponse)
        return Single.just("No content to save")
    }
}