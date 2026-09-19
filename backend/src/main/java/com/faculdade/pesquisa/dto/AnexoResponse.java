package com.faculdade.pesquisa.dto;

import java.time.OffsetDateTime;

/** Metadados do anexo (sem o conteudo binario, que so vai no download). */
public record AnexoResponse(
        Long id,
        Long chamadoId,
        Long autorId,
        String autorNome,
        String nomeArquivo,
        String tipoConteudo,
        long tamanhoBytes,
        OffsetDateTime criadoEm) {
}
