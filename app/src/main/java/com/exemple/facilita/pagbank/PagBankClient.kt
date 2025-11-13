package com.exemple.facilita.pagbank

import com.exemple.facilita.pagbank.api.PagBankService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit configurado para PagBank
 */
object PagBankClient {

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (PagBankConfig.IS_SANDBOX)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(PagBankConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(PagBankConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(PagBankConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("x-api-version", "4.0")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(PagBankConfig.getBaseUrl())
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val service: PagBankService by lazy {
        retrofit.create(PagBankService::class.java)
    }

    /**
     * Retorna Authorization header formatado
     */
    fun getAuthorizationHeader(token: String = PagBankConfig.TOKEN_SANDBOX): String {
        return "Bearer $token"
    }
}

