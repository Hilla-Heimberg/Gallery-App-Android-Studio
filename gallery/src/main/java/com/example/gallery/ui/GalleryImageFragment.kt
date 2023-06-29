package com.example.gallery.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.gallery.R

/**
 * Gallery Image fragment. Will be the second fragment to be displayed on the screen
 * from the gallery fragment.
 */
class GalleryImageFragment: Fragment(R.layout.gallery_image_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageView = view.findViewById<ImageView>(R.id.galleryImageFullScreen)

        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        if (bundle == null) {
            Glide
                .with(view.context)
                .load(R.drawable.placeholder_image)
                .into(imageView)
            return
        }

        val args = GalleryImageFragmentArgs.fromBundle(bundle)
        val uri = Uri.parse(args.galleryItem)

        Glide
            .with(view.context)
            .load(uri)
            .error(R.drawable.placeholder_image)
            .into(imageView)
    }
}
