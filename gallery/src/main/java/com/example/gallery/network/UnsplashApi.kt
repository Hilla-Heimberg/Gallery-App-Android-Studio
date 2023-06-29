package com.example.gallery.network

import com.example.gallery.network.entities.ImagesResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

typealias ClientID = String

interface UnsplashApi {

    @GET("/photos/")
    suspend fun getUnsplashImages(
        @Query("client_id") clientID: ClientID,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10
    ) : Response<List<ImagesResponse>>

    companion object {
        private const val BASE_URL = "https://api.unsplash.com/"

        val instance: UnsplashApi by lazy {
            val retrofit: Retrofit = createRetrofit()
            retrofit.create(UnsplashApi::class.java)
        }

        private fun createRetrofit() : Retrofit {
            // Create converter
            val moshi : Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            // Create Logger
            val logger = HttpLoggingInterceptor()
            logger.setLevel(HttpLoggingInterceptor.Level.BODY)

            // Create client
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            // Build retrofit
            return Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build()
        }
    }
}