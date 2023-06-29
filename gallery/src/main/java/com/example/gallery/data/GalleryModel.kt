package com.example.gallery.data
import android.net.Uri
import androidx.lifecycle.MediatorLiveData
import com.example.gallery.data.models.ImageItem

/**
 * Model class that represents the gallery gallery.
 */
data class GalleryModel(
    val galleryItemsProvider: ImagesProvider,
    val unsplashItemsProvider: ImagesProvider
)
{
    var curProvider: ImagesProvider = galleryItemsProvider
    var placeholderImage = listOf<ImageItem>(ImageItem((Uri.parse(PLACEHOLDER_IMAGE))))
    val errorsLiveData = MediatorLiveData<ImagesProviderError>().apply {
        addSource(galleryItemsProvider.errorsLiveData) { value ->
            this.value = value
        }

        addSource(unsplashItemsProvider.errorsLiveData) { value ->
            this.value = value
        }
    }

    suspend fun getImagesFromProvider() : List<ImageItem> {
        return curProvider.getImageItems()
    }

    companion object {
        private const val DEFAULT_IMAGE_1 = "android.resource://com.example.gallery/drawable/windows_xp_1"
        private const val DEFAULT_IMAGE_2 = "android.resource://com.example.gallery/drawable/windows_xp_2"
        private const val DEFAULT_IMAGE_3 = "android.resource://com.example.gallery/drawable/windows_xp_3"
        private const val DEFAULT_IMAGE_4 = "android.resource://com.example.gallery/drawable/windows_xp_4"
        private const val PLACEHOLDER_IMAGE = "android.resource://com.example.gallery/drawable/placeholder_image"

        val initialImageItemLists: List<ImageItem> = listOf<ImageItem>(
            ImageItem(Uri.parse(DEFAULT_IMAGE_1)),
            ImageItem(Uri.parse(DEFAULT_IMAGE_2)),
            ImageItem(Uri.parse(DEFAULT_IMAGE_3)),
            ImageItem(Uri.parse(DEFAULT_IMAGE_4)),
        )
    }
}