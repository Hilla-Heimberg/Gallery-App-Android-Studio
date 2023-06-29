package com.example.gallery.ui

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.*
import com.example.gallery.data.GalleryItemsProvider
import com.example.gallery.data.GalleryModel
import com.example.gallery.data.UnsplashItemsProvider
import com.example.gallery.data.models.ImageItem
import com.example.gallery.network.UnsplashApi
import com.google.android.material.snackbar.Snackbar

/**
 * Gallery fragment. Will be the first fragment to be displayed on the screen.
 */
class GalleryFragment: Fragment(R.layout.gallery_fragment), AdapterView.OnItemSelectedListener {
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var recyclerView : RecyclerView
    private lateinit var progressBar : ProgressBar
    private lateinit var imageItemList: List<ImageItem>
    private lateinit var reloadButton: Button
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContext = requireContext().applicationContext
        // Here, use the given view model factory to create the view model.
        // Getting it by the ViewModelProvider allows to get an existing VM (if exists)
        // or create a new one if needed
        galleryViewModel = ViewModelProvider(
            this,
            GalleryViewModel.Factory(
                { GalleryItemsProvider(appContext) },
                { UnsplashItemsProvider(appContext, UnsplashApi.instance) }
            )
        ).get()
        imageItemList = GalleryModel.initialImageItemLists
    }

    override fun onResume() {
        super.onResume()
        if (galleryViewModel.didLoadContentOnce &&
            spinner.selectedItemId == GalleryViewModel.GALLERY_IMAGES.toLong()
        )
        {
            galleryViewModel.postImages()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner(view)

        reloadButton = view.findViewById<Button>(R.id.button)
        reloadButton.isVisible = false
        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                startProgressBar()
                if (isGranted) {
                    galleryViewModel.postImages()
                    reloadButton.isVisible = false
                    galleryViewModel.didLoadContentOnce = true
                } else {
                    Snackbar.make(
                        view.findViewById(R.id.mainLayout),
                        R.string.permission_explanation,
                        Snackbar.LENGTH_LONG
                    ).show()
                    progressBar.isVisible = false
                    reloadButton.isVisible = true
                }
            }
        reloadButton.setOnClickListener {
            activityResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        setupProgressBar(view)
        setupRecyclerView(view)
        setupImageItemsObserver()
        setupErrorMessageObserver()

        if (spinner.selectedItemId == GalleryViewModel.GALLERY_IMAGES.toLong()) {
            activityResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun setupProgressBar(view: View) {
        progressBar = view.findViewById<ProgressBar>(R.id.galleryProgressBar)
        startProgressBar()
    }

    private fun startProgressBar() {
        progressBar.isVisible = true
        progressBar.animate()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        if (recyclerView.adapter == null) {
            recyclerView.adapter = GalleryAdapter(imageItemList)
        }
    }

    private fun setupImageItemsObserver() {
        val imageItemsListObserver = Observer<List<ImageItem>> { newList ->
            imageItemList = newList
            (recyclerView.adapter as GalleryAdapter).imageItemsList = newList
            (recyclerView.adapter as GalleryAdapter).notifyDataSetChanged()
            progressBar.isVisible = false
        }

        galleryViewModel.imageItemsLiveData.observe(viewLifecycleOwner, imageItemsListObserver)
    }

    private fun setupErrorMessageObserver() {
        val errorMessageObserver = Observer<Int?> { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                galleryViewModel.errorMessageDisplayed()
            }
        }

        galleryViewModel.errorMessageLiveData.observe(viewLifecycleOwner, errorMessageObserver)
    }

    private fun setupSpinner(view: View) {
        spinner = view.findViewById<Spinner>(R.id.spinner)
        ArrayAdapter.createFromResource(
            view.context,
            R.array.images_source_array,
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = arrayAdapter
            spinner.setSelection(galleryViewModel.currentSpinnerPosition)
        }
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        progressBar.isVisible = true
        galleryViewModel.onSpinnerItemSelected(position)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        return
    }
}