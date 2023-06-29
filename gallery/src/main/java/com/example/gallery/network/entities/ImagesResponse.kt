package com.example.gallery.network.entities

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias Url = String

@JsonClass(generateAdapter = true)
data class ImagesResponse(
    @Json(name = "urls")
    val urls: UnsplashURL
)

@JsonClass(generateAdapter = true)
data class UnsplashURL(
    @Json(name = "small")
    val imageUrl: Url
)
