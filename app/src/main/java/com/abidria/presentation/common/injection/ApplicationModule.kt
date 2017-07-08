package com.abidria.presentation.common.injection

import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Provides
    @Singleton
    @Named("io")
    fun providerIOScheduler(): Scheduler = Schedulers.io()

    @Provides
    @Singleton
    @Named("main")
    fun providerMainScheduler(): Scheduler = AndroidSchedulers.mainThread()

    @Provides
    @Singleton
    fun provideSchedulerProvider(@Named("io") subscriberScheduler: Scheduler,
                                 @Named("main") observerScheduler: Scheduler): SchedulerProvider =
        SchedulerProvider(subscriberScheduler, observerScheduler)
}