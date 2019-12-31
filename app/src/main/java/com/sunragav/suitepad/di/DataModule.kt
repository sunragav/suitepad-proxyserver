package com.sunragav.suitepad.di

import com.sunragav.suitepad.data.Repository
import com.sunragav.suitepad.data.RepositoryImpl
import dagger.Binds
import dagger.Module

@Module
abstract class DataModule {
    @Binds
    abstract fun bindsRepository(
        repoImpl: RepositoryImpl
    ): Repository


}