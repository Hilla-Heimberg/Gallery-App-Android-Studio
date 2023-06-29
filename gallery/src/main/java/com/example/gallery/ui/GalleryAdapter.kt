package com.example.gallery.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gallery.R
import com.example.gallery.data.models.ImageItem

/**
 * Gallery Adapter for the gallery recycler view.
 */
class GalleryAdapter(
    imageItemsList: List<ImageItem>
) : RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>(){

    var imageItemsList: List<ImageItem>

    init {
        this.imageItemsList = imageItemsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
        return ImageViewHolder(view)    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageItemsList.get(position).uri
        Glide
            .with(holder.itemView.context)
            .load(uri)
            .error(R.drawable.placeholder_image)
            .into(holder.galleryImageView)

        holder.itemView.setOnClickListener {
            val directions = GalleryFragmentDirections.actionGalleryFragmentToGalleryImageFragment(uri.toString())
            Navigation.findNavController(it).navigate(directions)
        }
    }

    override fun getItemCount(): Int {
        return imageItemsList.size
    }

    /**
     * View holder for gallery image.
     */
    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val galleryImageView: ImageView = itemView.findViewById(R.id.galleryItemImageView)
    }
}