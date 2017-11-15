package com.abidria.data.scene

import android.content.Context
import android.net.Uri
import android.util.Log
import com.abidria.BuildConfig
import com.abidria.data.common.ParseNetworkResultTransformer
import com.abidria.data.common.Result
import com.abidria.data.picture.Picture
import com.google.gson.Gson
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import net.gotev.uploadservice.MultipartUploadRequest
import net.gotev.uploadservice.UploadNotificationConfig
import net.gotev.uploadservice.UploadStatusDelegate
import retrofit2.Retrofit
import javax.inject.Named
import net.gotev.uploadservice.UploadInfo
import net.gotev.uploadservice.ServerResponse
import org.json.JSONObject


class SceneApiRepository(retrofit: Retrofit, @Named("io") val scheduler: Scheduler, val context: Context) {

    private val sceneApi: SceneApi = retrofit.create(SceneApi::class.java)

    fun scenesFlowableAndRefreshObserver(experienceId: String):
            Pair<Flowable<Result<List<Scene>>>, Observer<Any>> {
        val refreshPublisher: PublishSubject<Any> = PublishSubject.create()
        return Pair(first = refreshPublisher.startWith(Any())
                            .flatMap { sceneApi.scenes(experienceId).subscribeOn(scheduler).toObservable() }
                            .toFlowable(BackpressureStrategy.LATEST)
                            .compose<Result<List<Scene>>>(ParseNetworkResultTransformer({ it.map { it.toDomain() } })),
                    second = refreshPublisher)
    }

    fun createScene(scene: Scene): Flowable<Result<Scene>> =
        sceneApi.createScene(title = scene.title, description = scene.description,
                             latitude = scene.latitude, longitude = scene.longitude,
                             experienceId = scene.experienceId)
                .compose(ParseNetworkResultTransformer({ it.toDomain() }))

    fun uploadScenePicture(sceneId: String, croppedImageUriString: String, delegate: (scene: Scene) -> Unit) {
        try {
            val uploadId = MultipartUploadRequest(context,
                    BuildConfig.API_URL + "/scenes/" + sceneId + "/picture/")
                    .addFileToUpload(Uri.parse(croppedImageUriString).path, "picture")
                    .setNotificationConfig(UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(object : UploadStatusDelegate {
                        override fun onProgress(context: Context, uploadInfo: UploadInfo) {}
                        override fun onError(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse,
                                             exception: Exception) {}
                        override fun onCancelled(context: Context, uploadInfo: UploadInfo) {}
                        override fun onCompleted(context: Context, uploadInfo: UploadInfo,
                                                 serverResponse: ServerResponse) {
                            val jsonScene = JSONObject(serverResponse.bodyAsString)
                            delegate(parseSceneJson(jsonScene))
                        }
                    })
                    .startUpload()
        } catch (exc: Exception) {
            Log.e("AndroidUploadService", exc.message, exc)
        }
    }

    private fun parseSceneJson(jsonScene: JSONObject): Scene {
        val id = jsonScene.getString("id")
        val title = jsonScene.getString("title")
        val description = jsonScene.getString("description")
        val latitude = jsonScene.getDouble("latitude")
        val longitude = jsonScene.getDouble("longitude")
        val experienceId = jsonScene.getString("experience_id")
        val pictureJson = jsonScene.getJSONObject("picture")
        val smallUrl = pictureJson.getString("small_url")
        val mediumUrl = pictureJson.getString("medium_url")
        val largeUrl = pictureJson.getString("large_url")
        val picture = Picture(smallUrl = smallUrl, mediumUrl = mediumUrl, largeUrl = largeUrl)

        return Scene(id = id, title = title, description = description, latitude = latitude,
                     longitude = longitude, experienceId = experienceId, picture = picture)
    }

}
