package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.repository.AvaliacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Job periodico que dispara por e-mail as pesquisas cujo atraso configurado
 * (app.email.atraso-minutos) ja passou desde o encerramento do chamado.
 */
@Component
public class EnvioPesquisaScheduler {

    private static final Logger log = LoggerFactory.getLogger(EnvioPesquisaScheduler.class);

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmailNotificacaoService emailNotificacaoService;

    public EnvioPesquisaScheduler(AvaliacaoRepository avaliacaoRepository, EmailNotificacaoService emailNotificacaoService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.emailNotificacaoService = emailNotificacaoService;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    @Transactional
    public void enviarPendentes() {
        List<Avaliacao> pendentes = avaliacaoRepository.findByEmailEnviadoEmIsNullAndEnviarEmLessThanEqual(OffsetDateTime.now());
        for (Avaliacao avaliacao : pendentes) {
            if (emailNotificacaoService.enviarPesquisa(avaliacao)) {
                avaliacao.setEmailEnviadoEm(OffsetDateTime.now());
            }
        }
        if (!pendentes.isEmpty()) {
            log.info("Job de envio da pesquisa processou {} pesquisa(s) pendente(s).", pendentes.size());
        }
    }
}
