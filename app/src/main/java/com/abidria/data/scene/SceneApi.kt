package com.abidria.data.scene

import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Query

interface SceneApi {

    @GET("/scenes/")
    fun scenes(@Query("experience") experienceId: String) : Flowable<List<SceneMapper>>
}