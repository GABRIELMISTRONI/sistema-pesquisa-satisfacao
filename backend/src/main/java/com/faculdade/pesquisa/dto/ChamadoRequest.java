package com.faculdade.pesquisa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank @Size(max = 160) String titulo,
        @NotBlank @Size(max = 2000) String descricao,
        String prioridade) {
}
