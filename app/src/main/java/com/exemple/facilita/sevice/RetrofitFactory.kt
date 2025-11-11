package com.exemple.facilita.service

import com.exemple.facilita.sevice.UserService
import com.google.firebase.appdistribution.gradle.ApiService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitFactory {

   private val gson = GsonBuilder()
        .serializeNulls()
        .setLenient()
        .create()

    private val retrofit: Retrofit = Retrofit.Builder()
       .baseUrl("https://servidor-facilita.onrender.com/")
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

