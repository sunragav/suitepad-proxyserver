package com.sunragav.suitepad.di

import com.sunragav.suitepad.data.contract.RemoteRepository
import com.sunragav.suitepad.data.remotedata.api.FakeSuitePadService
import com.sunragav.suitepad.data.remotedata.api.SuitePadService
import com.sunragav.suitepad.data.remotedata.datasource.RemoteRepositoryImpl
import com.sunragav.suitepad.proxyserver.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [RemoteDataModule.Binders::class])
class RemoteDataModule {

    @Module
    interface Binders {

        @Binds
        fun bindsRemoteRepository(
            remoteDataSourceImpl: RemoteRepositoryImpl
        ): RemoteRepository
    }


    @Provides
    @Singleton
    fun providesFakeSuitepadService(retrofit: Retrofit): SuitePadService =
        FakeSuitePadService() //<-- This is a fake service to simulate the experience. Actual retrofit service is --> retrofit.create(SuitePadService::class.java)


    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BuildConfig.BASE_URL)
            .build()

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        val level = getInterceptorLevel()
        httpLoggingInterceptor.level = level
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .cache(null)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build()
    }


    private fun getInterceptorLevel(): HttpLoggingInterceptor.Level? {
        return if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

}