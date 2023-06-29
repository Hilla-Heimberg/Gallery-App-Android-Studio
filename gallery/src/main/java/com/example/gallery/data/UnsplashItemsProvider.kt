package com.example.gallery.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.*
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gallery.data.models.ImageItem
import com.example.gallery.network.ClientID
import com.example.gallery.network.UnsplashApi

class UnsplashItemsProvider constructor(
    private val context: Context,
    private val unsplashApi: UnsplashApi
) : ImagesProvider {

    private var unsplashItems: List<ImageItem> = emptyList()
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var _errorsLiveData = MutableLiveData<ImagesProviderError>()

    override val errorsLiveData: LiveData<ImagesProviderError> = _errorsLiveData

    companion object {
        private const val CLIENT_ID: ClientID = "JMUQXMCOLUBlf3ztrecgeyP5xDalc94D5MdCTm4Jd2Q"
        private const val PER_PAGE: Int = 30
        private val TRANSPORT = listOf<Int>(
            NetworkCapabilities.TRANSPORT_CELLULAR,
            NetworkCapabilities.TRANSPORT_WIFI,
            NetworkCapabilities.TRANSPORT_ETHERNET
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun getImageItems(): List<ImageItem> {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        /// There is internet connection
        if (capabilities != null && TRANSPORT.any { capabilities.hasTransport(it) }) {
            if (unsplashItems.isEmpty()) {
                val fetchResult = getUnsplashImages()
                unsplashItems = if (fetchResult.isSuccess){
                    fetchResult.getOrNull()!!
                } else {
                    _errorsLiveData.postValue(ImagesProviderError.FAIL_NETWORK_CALL)
                    emptyList()
                }
            }
            return unsplashItems
        }

        _errorsLiveData.postValue(ImagesProviderError.NO_INTERNET)
        return emptyList()
    }

    private suspend fun getUnsplashImages(): Result<List<ImageItem>> = withContext(Dispatchers.IO) {
        val deferredResponses = (1..4).map { pageNumber ->
            async {
                unsplashApi.getUnsplashImages(
                    clientID = CLIENT_ID,
                    page = pageNumber,
                    perPage = PER_PAGE
                )
            }
        }
        val responses = awaitAll(*deferredResponses.toTypedArray())

        val imageItemsList = mutableListOf<ImageItem>()
        for (response in responses) {
            when {
                response.isSuccessful -> {
                    val imagesResponse = response.body()!!

                    for (image in imagesResponse) {
                        val urlAsUri = Uri.parse(image.urls.imageUrl)
                        imageItemsList.add(ImageItem(urlAsUri))
                    }
                } else -> Result.failure<List<ImageItem>>(UnsplashError("Error"))
            }
        }
        Result.success(imageItemsList)
    }
}

internal class UnsplashError(error: String) : Exception(error)