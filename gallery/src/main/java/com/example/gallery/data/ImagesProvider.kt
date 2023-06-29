package com.example.gallery.data

import androidx.lifecycle.LiveData
import com.example.gallery.data.models.ImageItem

enum class ImagesProviderError {
    FAIL_NETWORK_CALL,
    NO_INTERNET,
}

interface ImagesProvider {
    val errorsLiveData: LiveData<ImagesProviderError>

    suspend fun getImageItems(): List<ImageItem>
}