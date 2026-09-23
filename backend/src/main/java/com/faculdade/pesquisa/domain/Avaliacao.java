package com.faculdade.pesquisa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Pesquisa de satisfacao gerada automaticamente quando um chamado e encerrado.
 * O cliente responde pelo link publico que carrega o token.
 */
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false, unique = true)
    private Chamado chamado;

    @Column(nullable = false, unique = true, length = 60)
    private String token;

    /** Nota de 1 a 5. Fica nulo enquanto a pesquisa nao for respondida. */
    @Column
    private Integer nota;

    @Column(length = 1000)
    private String comentario;

    @Column(name = "criada_em", nullable = false)
    private OffsetDateTime criadaEm = OffsetDateTime.now();

    @Column(name = "respondida_em")
    private OffsetDateTime respondidaEm;

    /** Momento em que o e-mail da pesquisa deve ser disparado (encerramento + atraso configurado). */
    @Column(name = "enviar_em", nullable = false)
    private OffsetDateTime enviarEm = OffsetDateTime.now();

    /** Fica nulo ate o job de envio disparar o e-mail; usado para nao reenviar. */
    @Column(name = "email_enviado_em")
    private OffsetDateTime emailEnviadoEm;

    public boolean isRespondida() {
        return respondidaEm != null;
    }

    public boolean isEmailEnviado() {
        return emailEnviadoEm != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public OffsetDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(OffsetDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public OffsetDateTime getRespondidaEm() {
        return respondidaEm;
    }

    public void setRespondidaEm(OffsetDateTime respondidaEm) {
        this.respondidaEm = respondidaEm;
    }

    public OffsetDateTime getEnviarEm() {
        return enviarEm;
    }

    public void setEnviarEm(OffsetDateTime enviarEm) {
        this.enviarEm = enviarEm;
    }

    public OffsetDateTime getEmailEnviadoEm() {
        return emailEnviadoEm;
    }

    public void setEmailEnviadoEm(OffsetDateTime emailEnviadoEm) {
        this.emailEnviadoEm = emailEnviadoEm;
    }
}
