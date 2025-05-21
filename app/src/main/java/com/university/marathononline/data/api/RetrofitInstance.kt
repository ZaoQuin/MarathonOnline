package com.university.marathononline.data.api

import com.intuit.sdp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://marathononlineapi.onrender.com"
//    private const val BASE_URL = "http://192.168.1.165:8080"

    fun <Api> buildApi(
        api: Class <Api>,
        token: String? = null
    ): Api {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                try {
                    chain.proceed(requestBuilder.build())
                } catch (e: IOException) {
                    throw IOException("Network error: Unable to connect to the server.", e)
                }
            }
            .also {
            client ->
                if(BuildConfig.DEBUG){
                    val logging = HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }
}