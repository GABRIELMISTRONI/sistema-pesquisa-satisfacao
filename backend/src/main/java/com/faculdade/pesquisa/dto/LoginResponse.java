package com.faculdade.pesquisa.dto;

public record LoginResponse(
        String token,
        Long usuarioId,
        String nome,
        String email,
        String perfil) {
}
