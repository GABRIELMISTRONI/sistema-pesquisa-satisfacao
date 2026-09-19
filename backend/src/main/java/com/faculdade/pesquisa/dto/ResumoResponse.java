package com.faculdade.pesquisa.dto;

import java.util.Map;

public record ResumoResponse(
        long chamadosAbertos,
        long chamadosEmAndamento,
        long chamadosEncerrados,
        long pesquisasEnviadas,
        long pesquisasRespondidas,
        double taxaResposta,
        Double notaMedia,
        Map<Integer, Long> distribuicaoNotas,
        Map<String, Long> distribuicaoClassificacao) {
}
