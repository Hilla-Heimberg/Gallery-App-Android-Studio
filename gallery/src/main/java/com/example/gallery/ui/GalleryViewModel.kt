package com.example.gallery.ui

import androidx.lifecycle.*
import com.example.gallery.R
import com.example.gallery.data.*
import com.example.gallery.data.models.ImageItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val galleryItemsProvider: ImagesProvider,
    private val unsplashItemsProvider: ImagesProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    companion object {
        internal const val GALLERY_IMAGES = 0
        internal const val UNSPLASH_IMAGES = 1
    }

    private val galleryModel = GalleryModel(galleryItemsProvider, unsplashItemsProvider)
    private val _imageItemsLiveData = MutableLiveData<List<ImageItem>>()
    private val _errorMessageLiveData = MutableLiveData<Int?>()
    private val imagesErrorObserver = Observer<ImagesProviderError> { error ->
        when (error) {
            ImagesProviderError.FAIL_NETWORK_CALL ->
                _errorMessageLiveData.postValue(R.string.network_call_error)
            ImagesProviderError.NO_INTERNET ->
                _errorMessageLiveData.postValue(R.string.no_internet_connection)
            else -> _errorMessageLiveData.postValue(R.string.general_error)
        }
    }

    val imageItemsLiveData: LiveData<List<ImageItem>> = _imageItemsLiveData
    val errorMessageLiveData: LiveData<Int?> = _errorMessageLiveData
    var didLoadContentOnce = false
    var currentSpinnerPosition: Int = 0

    init {
        galleryModel.errorsLiveData.observeForever(imagesErrorObserver)
    }

    fun postImages() {
        viewModelScope.launch(dispatcher) {
            val newImagesList = galleryModel.getImagesFromProvider()
            _imageItemsLiveData.postValue(newImagesList)
        }
    }

    fun onSpinnerItemSelected(position: Int) {
        currentSpinnerPosition = position
        when (position){
            GALLERY_IMAGES -> {
                galleryModel.curProvider = galleryItemsProvider
                if (didLoadContentOnce) {
                    postImages()
                }
            }
            UNSPLASH_IMAGES -> {
                galleryModel.curProvider = unsplashItemsProvider
                postImages()
            }
        }
    }

    /// This function is being called after the observer consumed the new value, to show the error
    /// message only once and not every time the fragment is being re-created.
    fun errorMessageDisplayed() {
        _errorMessageLiveData.value = null
    }

    override fun onCleared() {
        super.onCleared()
        galleryModel.errorsLiveData.removeObserver(imagesErrorObserver)
    }

    
    class Factory(
        private val galleryItemsProviderFactory: () -> GalleryItemsProvider,
        private val unsplashItemsProviderFactory: () -> UnsplashItemsProvider
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(
                galleryItemsProviderFactory(),
                unsplashItemsProviderFactory()
            ) as T
        }
    }
}
