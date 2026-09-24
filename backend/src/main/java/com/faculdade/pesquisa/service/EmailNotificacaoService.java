package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.repository.AvaliacaoRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * Envia a pesquisa de satisfacao por e-mail (SMTP do Resend) quando o atraso
 * configurado pelo atendente se esgota apos o encerramento do chamado.
 *
 * Substitui o antigo envio via Teams: o cliente nao ve mais o link na hora,
 * ele chega por e-mail no momento agendado (ver {@link EnvioPesquisaScheduler}).
 */
@Service
public class EmailNotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificacaoService.class);
    private static final String TEMPLATE_CLASSPATH = "templates/pesquisa-email.html";

    private final JavaMailSender mailSender;
    private final AvaliacaoRepository avaliacaoRepository;
    private final String remetente;
    private final String assunto;
    private final String urlBaseFrontend;
    private final String template;

    public EmailNotificacaoService(
            JavaMailSender mailSender,
            AvaliacaoRepository avaliacaoRepository,
            @Value("${app.email.remetente}") String remetente,
            @Value("${app.email.assunto}") String assunto,
            @Value("${app.frontend.url}") String urlBaseFrontend) {
        this.mailSender = mailSender;
        this.avaliacaoRepository = avaliacaoRepository;
        this.remetente = remetente;
        this.assunto = assunto;
        this.urlBaseFrontend = urlBaseFrontend;
        this.template = carregarTemplate();
    }

    /**
     * Dispara o envio em outra thread e retorna na hora - usado no encerramento
     * do chamado para o pedido HTTP nao ficar esperando o handshake SMTP (que
     * pode levar alguns segundos). Recarrega a avaliacao pelo id porque uma
     * entidade JPA carregada na thread da requisicao nao pode ser acessada
     * (lazy loading) fora daquela transacao.
     */
    @Async
    @Transactional
    public void enviarEmSegundoPlano(Long avaliacaoId) {
        avaliacaoRepository.findById(avaliacaoId).ifPresent(avaliacao -> {
            if (enviarPesquisa(avaliacao)) {
                avaliacao.setEmailEnviadoEm(OffsetDateTime.now());
            }
        });
    }

    /** @return true se o e-mail foi enviado com sucesso (a chamada nunca lanca excecao). */
    public boolean enviarPesquisa(Avaliacao avaliacao) {
        String destinatario = avaliacao.getChamado().getSolicitante().getEmail();
        try {
            String html = template
                    .replace("${CHAMADO}", String.valueOf(avaliacao.getChamado().getId()))
                    .replace("${ASSUNTO}", avaliacao.getChamado().getTitulo())
                    .replace("${LINK}", montarLink(avaliacao.getToken()));

            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, StandardCharsets.UTF_8.name());
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(html, true);

            mailSender.send(mensagem);
            log.info("Pesquisa de satisfacao do chamado {} enviada por e-mail para {}.",
                    avaliacao.getChamado().getId(), destinatario);
            return true;
        } catch (MailException | jakarta.mail.MessagingException e) {
            // Falha no envio do e-mail nao pode derrubar o job de envio nem o encerramento do chamado.
            // Fica pendente e o job tenta de novo na proxima execucao.
            log.error("Falha ao enviar pesquisa do chamado {} por e-mail para {}: {}",
                    avaliacao.getChamado().getId(), destinatario, e.getMessage());
            return false;
        }
    }

    private String montarLink(String token) {
        return urlBaseFrontend + "/pesquisa?token=" + token;
    }

    private String carregarTemplate() {
        try {
            byte[] bytes = StreamUtils.copyToByteArray(new ClassPathResource(TEMPLATE_CLASSPATH).getInputStream());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel carregar " + TEMPLATE_CLASSPATH, e);
        }
    }
}
