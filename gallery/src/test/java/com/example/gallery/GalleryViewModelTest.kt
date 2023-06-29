package com.example.gallery

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gallery.data.ImagesProvider
import com.example.gallery.data.ImagesProviderError
import com.example.gallery.data.models.ImageItem
import com.example.gallery.ui.GalleryViewModel
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*
import org.junit.Before

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {
    companion object {
        private const val DEFAULT_IMAGE_1 =
            "android.resource://com.example.gallery/drawable/windows_xp_1"
        private const val DEFAULT_IMAGE_2 =
            "android.resource://com.example.gallery/drawable/windows_xp_2"

        private val galleryImagesList = listOf<ImageItem>(ImageItem(Uri.parse(DEFAULT_IMAGE_1)))
        private val unsplashImagesList = listOf<ImageItem>(
            ImageItem(Uri.parse(DEFAULT_IMAGE_1)),
            ImageItem(Uri.parse(DEFAULT_IMAGE_2))
        )

        internal const val GALLERY_IMAGES = 0
        internal const val UNSPLASH_IMAGES = 1

        private class FakeGalleryItemsProvider() : ImagesProvider {
            private val _errorsLiveData = MutableLiveData<ImagesProviderError>()
            override var errorsLiveData: LiveData<ImagesProviderError> = _errorsLiveData

            override suspend fun getImageItems(): List<ImageItem> {
                return galleryImagesList
            }
        }

        private class FakeUnsplashItemsProvider() : ImagesProvider {
            var isInternetConnection: Boolean = true

            private val _errorsLiveData = MutableLiveData<ImagesProviderError>()
            override var errorsLiveData: LiveData<ImagesProviderError> = _errorsLiveData

            override suspend fun getImageItems(): List<ImageItem> {
                if (isInternetConnection) {
                    return unsplashImagesList
                } else {
                    _errorsLiveData.postValue(ImagesProviderError.NO_INTERNET)
                    return emptyList()
                }
            }
        }
    }

    @get:Rule
    val instantLiveData = InstantTaskExecutorRule()

    @Before
    fun before() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    // unsplashWithInternet -----------------------------------------------------------------

    @Test
    fun unsplashButton_imageItemsLiveDataUpdatedWithInternet() = runTest {
        val viewModel = GalleryViewModel(
            FakeGalleryItemsProvider(),
            FakeUnsplashItemsProvider(),
            UnconfinedTestDispatcher()
        )
        viewModel.imageItemsLiveData.observeForever {}

        // Action for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        assertEquals(viewModel.imageItemsLiveData.value, unsplashImagesList)
    }

    @Test
    fun unsplashButton_spinnerPositionChangedWithInternet() = runTest {
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), FakeUnsplashItemsProvider())

        val spinnerPositionBefore = viewModel.currentSpinnerPosition

        // Action for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.currentSpinnerPosition).isNotEqualTo(spinnerPositionBefore)
    }

    @Test
    fun unsplashButton_imageItemsLiveDataValueRemainsUnchangedWhenReClickingWithInternet() = runTest {
        val viewModel = GalleryViewModel(
            FakeGalleryItemsProvider(),
            FakeUnsplashItemsProvider(),
            UnconfinedTestDispatcher()
        )

        var counter = 0
        var lastImageItems: List<ImageItem> = emptyList()

        // Test
        viewModel.imageItemsLiveData.observeForever { imageItems ->
            if (counter == 0) {
                Truth.assertThat(imageItems).isEqualTo(unsplashImagesList)
                lastImageItems = imageItems
                counter++
            } else {
                Truth.assertThat(imageItems).isEqualTo(lastImageItems)
            }
        }

        // Actions for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Make sure that the LiveData object has been tested correctly
        Truth.assertThat(viewModel.imageItemsLiveData.value).isNotNull()
    }

    @Test
    fun unsplashButton_spinnerPositionRemainsUnchangedWhenReClickingWithInternet() = runTest {
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), FakeUnsplashItemsProvider())

        val spinnerPositionBefore = viewModel.currentSpinnerPosition

        // Actions for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)
        val lastSpinnerPosition = viewModel.currentSpinnerPosition
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.currentSpinnerPosition).isNotEqualTo(spinnerPositionBefore)
        Truth.assertThat(viewModel.currentSpinnerPosition).isEqualTo(lastSpinnerPosition)
    }

    // unsplashWithoutInternet --------------------------------------------------------------

    @Test
    fun unsplashButton_imageItemsLiveDataIsEmptyWhenNoInternet() = runTest {
        val unsplashProvider = FakeUnsplashItemsProvider()
        unsplashProvider.isInternetConnection = false
        val viewModel = GalleryViewModel(
            FakeGalleryItemsProvider(),
            unsplashProvider,
            UnconfinedTestDispatcher()
        )

        viewModel.imageItemsLiveData.observeForever {}

        // Action for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.imageItemsLiveData.value).isEqualTo(emptyList<ImageItem>())
    }

    @Test
    fun unsplashButton_errorMessageLiveDataUpdatedWithNoInternet() = runTest {
        val unsplashProvider = FakeUnsplashItemsProvider()
        unsplashProvider.isInternetConnection = false
        val viewModel = GalleryViewModel(
            FakeGalleryItemsProvider(),
            unsplashProvider,
            UnconfinedTestDispatcher()
        )

        viewModel.errorMessageLiveData.observeForever {}

        // Action for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.errorMessageLiveData.value).isEqualTo(R.string.no_internet_connection)
    }

    @Test
    fun unsplashButton_spinnerPositionChangedWithNoInternet() = runTest {
        val unsplashProvider = FakeUnsplashItemsProvider()
        unsplashProvider.isInternetConnection = false
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), unsplashProvider)

        val spinnerPositionBefore = viewModel.currentSpinnerPosition

        // Action for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.currentSpinnerPosition).isNotEqualTo(spinnerPositionBefore)
    }

    @Test
    fun unsplashButton_spinnerPositionRemainsUnchangedWhenReClickingWithNoInternet() = runTest {
        val unsplashProvider = FakeUnsplashItemsProvider()
        unsplashProvider.isInternetConnection = false
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), unsplashProvider)

        val spinnerPositionBefore = viewModel.currentSpinnerPosition

        // Actions for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)
        val lastSpinnerPosition = viewModel.currentSpinnerPosition
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.currentSpinnerPosition).isNotEqualTo(spinnerPositionBefore)
        Truth.assertThat(viewModel.currentSpinnerPosition).isEqualTo(lastSpinnerPosition)
    }

    @Test
    fun unsplashButton_imageItemsLiveDataUpdatesWhenInternetIsBack() = runTest {
        val unsplashProvider = FakeUnsplashItemsProvider()
        unsplashProvider.isInternetConnection = false
        val viewModel = GalleryViewModel(
            FakeGalleryItemsProvider(),
            unsplashProvider,
            UnconfinedTestDispatcher()
        )
        viewModel.imageItemsLiveData.observeForever {}

        // Actions for test
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)
        unsplashProvider.isInternetConnection = true
        viewModel.onSpinnerItemSelected(GALLERY_IMAGES)
        viewModel.onSpinnerItemSelected(UNSPLASH_IMAGES)

        // Test
        Truth.assertThat(viewModel.imageItemsLiveData.value).isEqualTo(unsplashImagesList)
    }

    // gallery ----------------------------------------------------------------------------

    @Test
    fun galleryButton_imageItemsLiveDataUpdated() = runTest {
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), FakeUnsplashItemsProvider())

        // Test
        viewModel.imageItemsLiveData.observeForever { imageItems ->
            Truth.assertThat(imageItems).isEqualTo(galleryImagesList)
        }

        // Action for test
        viewModel.onSpinnerItemSelected(GALLERY_IMAGES)
    }

    @Test
    fun galleryButton_spinnerPositionChanged() = runTest {
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), FakeUnsplashItemsProvider())

        val spinnerPositionBefore = viewModel.currentSpinnerPosition

        // Action for test
        viewModel.onSpinnerItemSelected(GALLERY_IMAGES)

        // Test
        Truth.assertThat(viewModel.currentSpinnerPosition).isEqualTo(spinnerPositionBefore)
    }

    @Test
    fun galleryButton_spinnerPositionRemainsUnchangedWhenReClicking() = runTest {
        val viewModel = GalleryViewModel(FakeGalleryItemsProvider(), FakeUnsplashItemsProvider())

        val spinnerPositionBefore = viewModel.currentSpinnerPosition

        // Actions for test
        viewModel.onSpinnerItemSelected(GALLERY_IMAGES)
        val lastSpinnerPosition = viewModel.currentSpinnerPosition
        viewModel.onSpinnerItemSelected(GALLERY_IMAGES)

        // Test
        Truth.assertThat(viewModel.currentSpinnerPosition).isEqualTo(spinnerPositionBefore)
        Truth.assertThat(viewModel.currentSpinnerPosition).isEqualTo(lastSpinnerPosition)
    }
}