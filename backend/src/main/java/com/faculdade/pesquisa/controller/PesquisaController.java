package com.faculdade.pesquisa.controller;

import com.faculdade.pesquisa.dto.AvaliacaoRequest;
import com.faculdade.pesquisa.dto.AvaliacaoResponse;
import com.faculdade.pesquisa.service.PesquisaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Endpoints publicos: o cliente responde a pesquisa pelo link, sem fazer login. */
@RestController
@RequestMapping("/api/pesquisa")
public class PesquisaController {

    private final PesquisaService pesquisaService;
    private final String urlBaseFrontend;

    public PesquisaController(
            PesquisaService pesquisaService,
            @Value("${app.frontend.url}") String urlBaseFrontend) {
        this.pesquisaService = pesquisaService;
        this.urlBaseFrontend = urlBaseFrontend;
    }

    @GetMapping("/{token}")
    public AvaliacaoResponse buscar(@PathVariable String token) {
        return pesquisaService.buscarPorToken(token);
    }

    @PostMapping("/{token}")
    public AvaliacaoResponse responder(
            @PathVariable String token,
            @RequestBody @Valid AvaliacaoRequest request) {
        return pesquisaService.responder(token, request);
    }

    /**
     * Link clicado direto no cartao do Teams (botao 1 a 10). Nao devolve JSON:
     * grava a nota e redireciona para a pagina web da pesquisa, que ja mostra
     * o estado "respondida" com a nota registrada.
     */
    @GetMapping("/{token}/resposta-rapida/{nota}")
    public ResponseEntity<Void> responderRapido(@PathVariable String token, @PathVariable int nota) {
        pesquisaService.responderRapido(token, nota);
        URI destino = URI.create(urlBaseFrontend + "/pesquisa?token=" + token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(destino)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }
}
