package com.sunragav.suitepad.di

import android.content.Context
import com.sunragav.suitepad.data.contract.LocalRepository
import com.sunragav.suitepad.data.localdata.LocalRepositoryImpl
import com.sunragav.suitepad.data.remotedata.qualifiers.Background
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton


@Module(includes = [LocalDataModule.Binders::class])
class LocalDataModule {

    @Module
    interface Binders {

        @Binds
        fun bindsLocalDataSource(
            localDataSourceImpl: LocalRepositoryImpl
        ): LocalRepository

    }

    @Provides
    @Singleton
    @Background
    fun providesBackgroundScheduler() = Schedulers.io()

    @Provides
    @Singleton
    fun providesContentResolver(context: Context) = context.contentResolver
}
