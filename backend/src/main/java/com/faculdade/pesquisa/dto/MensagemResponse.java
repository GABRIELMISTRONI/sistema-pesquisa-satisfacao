package com.faculdade.pesquisa.dto;

import com.faculdade.pesquisa.domain.Mensagem;

import java.time.OffsetDateTime;

public record MensagemResponse(
        Long id,
        Long chamadoId,
        Long autorId,
        String autorNome,
        String autorPerfil,
        String texto,
        OffsetDateTime criadaEm) {

    public static MensagemResponse de(Mensagem mensagem) {
        return new MensagemResponse(
                mensagem.getId(),
                mensagem.getChamado().getId(),
                mensagem.getAutor().getId(),
                mensagem.getAutor().getNome(),
                mensagem.getAutor().getPerfil().name(),
                mensagem.getTexto(),
                mensagem.getCriadaEm());
    }
}
