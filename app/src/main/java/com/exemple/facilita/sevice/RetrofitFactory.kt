package com.exemple.facilita.service

import com.exemple.facilita.sevice.UserService
import com.google.firebase.appdistribution.gradle.ApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitFactory {

   private val gson = GsonBuilder()
        .serializeNulls()
        .setLenient()
        .create()

    // Configurar logging interceptor para debug
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configurar OkHttpClient com timeouts aumentados
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Timeout de conexão: 60 segundos
        .readTimeout(60, TimeUnit.SECONDS)    // Timeout de leitura: 60 segundos
        .writeTimeout(60, TimeUnit.SECONDS)   // Timeout de escrita: 60 segundos
        .addInterceptor(loggingInterceptor)   // Adicionar logging
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
       .baseUrl("https://servidor-facilita.onrender.com/")
       .client(okHttpClient) // Adicionar o client configurado
       .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Mantém o UserService existente para não quebrar outras partes do app
    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }
    //fun getUserService(): UserService = retrofitUser.create(UserService::class.java)
    fun getServicoService(): ServicoService = retrofit.create(ServicoService::class.java)

    fun getCNHService(): CNHService = retrofit.create(CNHService::class.java)

    fun getDocumentoService(): DocumentoService = retrofit.create(DocumentoService::class.java)

    fun getModalidadeService(): ModalidadeService = retrofit.create(ModalidadeService::class.java)
}

