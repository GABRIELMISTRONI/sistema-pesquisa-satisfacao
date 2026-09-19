package com.faculdade.pesquisa.dto;

import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.ClassificacaoNota;

import java.time.OffsetDateTime;

public record ChamadoResponse(
        Long id,
        String titulo,
        String descricao,
        String status,
        String prioridade,
        String solicitante,
        String responsavel,
        OffsetDateTime criadoEm,
        OffsetDateTime encerradoEm,
        String tokenAvaliacao,
        Integer nota,
        String classificacao) {

    public static ChamadoResponse de(Chamado chamado, String tokenAvaliacao, Integer nota) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getStatus().name(),
                chamado.getPrioridade().name(),
                chamado.getSolicitante().getNome(),
                chamado.getResponsavel() == null ? null : chamado.getResponsavel().getNome(),
                chamado.getCriadoEm(),
                chamado.getEncerradoEm(),
                tokenAvaliacao,
                nota,
                nota == null ? null : ClassificacaoNota.deNota(nota).name());
    }
}
