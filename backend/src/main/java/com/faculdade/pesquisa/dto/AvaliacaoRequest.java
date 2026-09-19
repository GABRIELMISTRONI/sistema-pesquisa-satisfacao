package com.faculdade.pesquisa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequest(
        @NotNull @Min(1) @Max(10) Integer nota,
        @Size(max = 1000) String comentario) {
}
