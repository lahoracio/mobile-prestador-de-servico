package com.exemple.facilita.utils

/**
 * Utilitários para formatação de texto (CPF, Telefone, etc)
 */
object TextFormatUtils {

    /**
     * Aplica máscara de CPF: 000.000.000-00
     */
    fun formatCPF(cpf: String): String {
        val digitsOnly = cpf.filter { it.isDigit() }.take(11)

        return when (digitsOnly.length) {
            0 -> ""
            in 1..3 -> digitsOnly
            in 4..6 -> "${digitsOnly.substring(0, 3)}.${digitsOnly.substring(3)}"
            in 7..9 -> "${digitsOnly.substring(0, 3)}.${digitsOnly.substring(3, 6)}.${digitsOnly.substring(6)}"
            else -> "${digitsOnly.substring(0, 3)}.${digitsOnly.substring(3, 6)}.${digitsOnly.substring(6, 9)}-${digitsOnly.substring(9)}"
        }
    }

    /**
     * Remove máscara do CPF, retornando apenas dígitos
     */
    fun unformatCPF(cpf: String): String {
        return cpf.filter { it.isDigit() }
    }

    /**
     * Aplica máscara de telefone: (00) 00000-0000 ou (00) 0000-0000
     */
    fun formatPhone(phone: String): String {
        val digitsOnly = phone.filter { it.isDigit() }.take(11)

        return when (digitsOnly.length) {
            0 -> ""
            in 1..2 -> "(${digitsOnly}"
            in 3..6 -> "(${digitsOnly.substring(0, 2)}) ${digitsOnly.substring(2)}"
            in 7..10 -> "(${digitsOnly.substring(0, 2)}) ${digitsOnly.substring(2, 6)}-${digitsOnly.substring(6)}"
            else -> "(${digitsOnly.substring(0, 2)}) ${digitsOnly.substring(2, 7)}-${digitsOnly.substring(7)}"
        }
    }

    /**
     * Remove máscara do telefone, retornando apenas dígitos
     */
    fun unformatPhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }

    /**
     * Valida CPF
     */
    fun isValidCPF(cpf: String): Boolean {
        val digitsOnly = cpf.filter { it.isDigit() }

        if (digitsOnly.length != 11) return false
        if (digitsOnly.all { it == digitsOnly[0] }) return false

        // Validação dos dígitos verificadores
        fun calculateDigit(cpf: String, weight: Int): Int {
            var sum = 0
            for (i in 0 until weight - 1) {
                sum += cpf[i].digitToInt() * ((weight) - i)
            }
            val remainder = sum % 11
            return if (remainder < 2) 0 else 11 - remainder
        }

        val digit1 = calculateDigit(digitsOnly, 10)
        val digit2 = calculateDigit(digitsOnly, 11)

        return digitsOnly[9].digitToInt() == digit1 && digitsOnly[10].digitToInt() == digit2
    }

    /**
     * Valida telefone (10 ou 11 dígitos)
     */
    fun isValidPhone(phone: String): Boolean {
        val digitsOnly = phone.filter { it.isDigit() }
        return digitsOnly.length in 10..11
    }
}

