package com.faculdade.pesquisa.dto;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.domain.ClassificacaoNota;

import java.time.OffsetDateTime;

public record AvaliacaoResponse(
        String token,
        Long chamadoId,
        String tituloChamado,
        String cliente,
        boolean respondida,
        Integer nota,
        String classificacao,
        String comentario,
        OffsetDateTime respondidaEm) {

    public static AvaliacaoResponse de(Avaliacao avaliacao) {
        Integer nota = avaliacao.getNota();
        return new AvaliacaoResponse(
                avaliacao.getToken(),
                avaliacao.getChamado().getId(),
                avaliacao.getChamado().getTitulo(),
                avaliacao.getChamado().getSolicitante().getNome(),
                avaliacao.isRespondida(),
                nota,
                nota == null ? null : ClassificacaoNota.deNota(nota).name(),
                avaliacao.getComentario(),
                avaliacao.getRespondidaEm());
    }
}
