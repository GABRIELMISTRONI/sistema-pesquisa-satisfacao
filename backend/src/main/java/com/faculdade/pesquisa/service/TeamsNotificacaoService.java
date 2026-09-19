package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Avaliacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Envia a pesquisa de satisfacao para o Teams quando um chamado e encerrado.
 *
 * Sem registro de Bot Framework no Azure: usa um webhook do Teams (Conector
 * "Incoming Webhook" ou o app "Workflows", que gera uma URL de POST) e manda
 * um Adaptive Card com 10 botoes (nota de 1 a 10). Cada botao e um link
 * (Action.OpenUrl) que abre o endpoint publico de resposta rapida do backend
 * — nao precisa de bot conversacional, o clique so abre a URL no navegador.
 *
 * Se app.teams.webhook-url nao estiver configurada, o envio e apenas
 * ignorado (log de aviso) para nao quebrar o encerramento do chamado.
 */
@Service
public class TeamsNotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(TeamsNotificacaoService.class);
    private static final int NOTA_MINIMA = 1;
    private static final int NOTA_MAXIMA = 10;

    private final RestTemplate restTemplate;
    private final String webhookUrl;
    private final String backendUrl;

    public TeamsNotificacaoService(
            RestTemplate restTemplate,
            @Value("${app.teams.webhook-url:}") String webhookUrl,
            @Value("${app.backend.url}") String backendUrl) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
        this.backendUrl = backendUrl;
    }

    public void enviarPesquisa(Avaliacao avaliacao) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("app.teams.webhook-url nao configurada; pesquisa do chamado {} nao foi enviada ao Teams.",
                    avaliacao.getChamado().getId());
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requisicao = new HttpEntity<>(montarCartao(avaliacao), headers);
            restTemplate.postForEntity(webhookUrl, requisicao, String.class);
            log.info("Pesquisa de satisfacao do chamado {} enviada ao Teams.", avaliacao.getChamado().getId());
        } catch (RestClientException e) {
            // Falha no envio ao Teams nao pode derrubar o encerramento do chamado.
            log.error("Falha ao enviar pesquisa do chamado {} para o Teams: {}",
                    avaliacao.getChamado().getId(), e.getMessage());
        }
    }

    private Map<String, Object> montarCartao(Avaliacao avaliacao) {
        String titulo = avaliacao.getChamado().getTitulo();
        String cliente = avaliacao.getChamado().getSolicitante().getNome();

        List<Map<String, Object>> botoes = new ArrayList<>();
        for (int nota = NOTA_MINIMA; nota <= NOTA_MAXIMA; nota++) {
            botoes.add(Map.of(
                    "type", "Action.OpenUrl",
                    "title", String.valueOf(nota),
                    "url", backendUrl + "/api/pesquisa/" + avaliacao.getToken() + "/resposta-rapida/" + nota));
        }

        Map<String, Object> conteudoCartao = new LinkedHashMap<>();
        conteudoCartao.put("$schema", "http://adaptivecards.io/schemas/adaptive-card.json");
        conteudoCartao.put("type", "AdaptiveCard");
        conteudoCartao.put("version", "1.4");
        conteudoCartao.put("body", List.of(
                Map.of(
                        "type", "TextBlock",
                        "text", "Pesquisa de satisfação",
                        "weight", "Bolder",
                        "size", "Medium"),
                Map.of(
                        "type", "TextBlock",
                        "text", "Chamado: " + titulo,
                        "wrap", true),
                Map.of(
                        "type", "TextBlock",
                        "text", "Olá " + cliente + ", de 1 a 10, como foi o atendimento?",
                        "wrap", true),
                Map.of(
                        "type", "TextBlock",
                        "text", "1–6 ruim · 7–8 razoável · 9–10 bom",
                        "isSubtle", true,
                        "wrap", true)));
        conteudoCartao.put("actions", botoes);

        Map<String, Object> anexo = new LinkedHashMap<>();
        anexo.put("contentType", "application/vnd.microsoft.card.adaptive");
        anexo.put("content", conteudoCartao);

        Map<String, Object> mensagem = new LinkedHashMap<>();
        mensagem.put("type", "message");
        mensagem.put("attachments", List.of(anexo));
        return mensagem;
    }
}
