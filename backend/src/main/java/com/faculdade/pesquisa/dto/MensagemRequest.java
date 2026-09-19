package com.faculdade.pesquisa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemRequest(@NotBlank @Size(max = 2000) String texto) {
}
