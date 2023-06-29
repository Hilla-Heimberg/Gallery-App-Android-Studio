package com.example.gallery.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gallery.data.models.ImageItem

class GalleryItemsProvider(context: Context) : ImagesProvider {

    private var contentResolver: ContentResolver = context.contentResolver
    private var _imageItemsLiveData = MutableLiveData<ImagesProviderError>()

    override val errorsLiveData: LiveData<ImagesProviderError> = _imageItemsLiveData

    override suspend fun getImageItems(): List<ImageItem> {
        return getAllGalleryItems()
    }

    /**
     * This is the function to get the user gallery items.
     */
    private fun getAllGalleryItems(): List<ImageItem> {
        return contentResolver.query(
            MediaStore.Files.getContentUri(EXTERNAL_VOLUME_NAME),
            arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MIME_TYPE),
            SELECTION,
            null,
            DESCENDING_SORT_ORDER
        ).use { cursor ->
            if (cursor == null) {
                return@use listOf()
            }
            val idColumnIndex: Int =
                cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val userAssetsList = mutableListOf<ImageItem>()
            while (cursor.moveToNext()) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri(EXTERNAL_VOLUME_NAME),
                    cursor.getInt(idColumnIndex).toLong()
                )
                userAssetsList.add(ImageItem(uri))
            }
            return@use userAssetsList
        }
    }

    companion object {
        private const val SELECTION =
            "(lower(" + MediaStore.Files.FileColumns.MIME_TYPE + ") LIKE 'image/%' )"
        private const val EXTERNAL_VOLUME_NAME = "external"
        private const val DESCENDING_SORT_ORDER =
            MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
    }
}
