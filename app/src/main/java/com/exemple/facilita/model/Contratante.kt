package com.exemple.facilita.model
import java.io.Serializable
data class Contratante(
    val id: Int,
    val necessidade: String,
    val id_usuario: Int,
    val id_localizacao: Int,
    val cpf: String,
    val usuario: Usuario?
): Serializable