package com.faculdade.pesquisa.service;

import org.springframework.http.HttpStatus;

/** Erro previsto de regra de negocio, com o status HTTP que deve ser devolvido. */
public class RegraDeNegocioException extends RuntimeException {

    private final HttpStatus status;

    public RegraDeNegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public static RegraDeNegocioException naoEncontrado(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.NOT_FOUND, mensagem);
    }

    public static RegraDeNegocioException conflito(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.CONFLICT, mensagem);
    }

    public static RegraDeNegocioException invalido(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public static RegraDeNegocioException semPermissao(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.FORBIDDEN, mensagem);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
