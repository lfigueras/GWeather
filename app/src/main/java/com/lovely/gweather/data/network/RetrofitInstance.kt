package com.lovely.gweather.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * A singleton object to provide a configured instance of Retrofit.
 */
object RetrofitInstance {

    // Create a Moshi instance with a Kotlin adapter factory.
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Create a logging interceptor to view API requests/responses in Logcat.
    // This is extremely useful for debugging.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create an OkHttpClient and add the logging interceptor.
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Create the Retrofit instance using a lazy delegate.
    // This means the instance is only created the first time it's accessed.
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(httpClient) // Use our custom HttpClient with the logger
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Use Moshi for JSON parsing
            .build()
    }


    val api: OpenWeatherApiService by lazy {
        retrofit.create(OpenWeatherApiService::class.java)
    }
}
