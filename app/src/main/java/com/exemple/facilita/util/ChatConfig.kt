package com.exemple.facilita.util

object ChatConfig {
    // Alternar entre AMBIENTE_LOCAL = true para desenvolvimento local, false para produção
    var AMBIENTE_LOCAL: Boolean = false

    private const val LOCAL_URL = "http://10.0.2.2:8080" // Android emulator localhost mapping
    private const val PROD_URL = "https://facilita-c6hhb9csgygudrdz.canadacentral-01.azurewebsites.net"

    val SOCKET_URL: String
        get() = if (AMBIENTE_LOCAL) LOCAL_URL else PROD_URL

    // Também expõe o ws(s) equivalente (para referências explicitas se necessário)
    val SOCKET_WS_URL: String
        get() = if (AMBIENTE_LOCAL) LOCAL_URL.replaceFirst("http", "ws") else PROD_URL.replaceFirst("https", "wss")
}

