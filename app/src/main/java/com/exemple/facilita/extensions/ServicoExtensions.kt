package com.exemple.facilita.extensions

import com.exemple.facilita.model.*

/**
 * Extensão para converter Servico em ServicoDetalhe
 * Usado quando aceitamos um serviço da API
 */
fun Servico.toServicoDetalhe(): ServicoDetalhe {
    return ServicoDetalhe(
        id = this.id,
        id_contratante = this.id_contratante,
        id_prestador = this.id_prestador,
        id_categoria = this.id_categoria,
        descricao = this.descricao,
        status = this.status,
        data_solicitacao = this.data_solicitacao,
        data_conclusao = null,
        data_confirmacao = null,
        id_localizacao = this.localizacao?.id,
        valor = this.valor,
        tempo_estimado = this.categoria.tempo_medio,
        data_inicio = null,
        contratante = ContratanteDetalhe(
            id = this.contratante.id,
            necessidade = this.contratante.necessidade,
            id_usuario = this.contratante.usuario.id,
            id_localizacao = this.localizacao?.id,
            cpf = "",
            usuario = UsuarioDetalhe(
                id = this.contratante.usuario.id,
                nome = this.contratante.usuario.nome,
                senha_hash = null,
                foto_perfil = this.contratante.usuario.foto_perfil,
                email = this.contratante.usuario.email,
                telefone = this.contratante.usuario.telefone,
                tipo_conta = "CONTRATANTE",
                criado_em = this.data_solicitacao
            )
        ),
        prestador = null,
        categoria = CategoriaDetalhe(
            id = this.categoria.id,
            nome = this.categoria.nome,
            descricao = this.categoria.descricao,
            icone = this.categoria.icone,
            preco_base = this.categoria.preco_base,
            tempo_medio = this.categoria.tempo_medio
        ),
        localizacao = this.localizacao?.let { loc ->
            LocalizacaoDetalhe(
                id = loc.id,
                endereco = loc.logradouro ?: "",
                bairro = loc.bairro ?: "",
                cidade = loc.cidade ?: "",
                estado = "SP", // Valor padrão, pode ser ajustado
                cep = loc.cep ?: "",
                numero = loc.numero ?: "",
                complemento = null,
                latitude = loc.latitude?.toDoubleOrNull() ?: 0.0,
                longitude = loc.longitude?.toDoubleOrNull() ?: 0.0
            )
        }
    )
}

