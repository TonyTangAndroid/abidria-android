package com.abidria.presentation.common

import android.app.Application
import com.abidria.data.common.injection.DataModule
import com.abidria.presentation.common.injection.ApplicationComponent
import com.abidria.presentation.common.injection.DaggerApplicationComponent
import com.facebook.stetho.Stetho
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

class AbidriaApplication : Application() {

    companion object {
        val injector: ApplicationComponent = DaggerApplicationComponent.builder().dataModule(DataModule()).build()
    }

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this);

        val picassoBuilder = Picasso.Builder(this)
        picassoBuilder.downloader(OkHttp3Downloader(this, Long.MAX_VALUE))
        picassoBuilder.indicatorsEnabled(true)
        Picasso.setSingletonInstance(picassoBuilder.build())
    }
}
