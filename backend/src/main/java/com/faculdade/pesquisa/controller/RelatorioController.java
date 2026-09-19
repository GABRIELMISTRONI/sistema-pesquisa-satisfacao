package com.faculdade.pesquisa.controller;

import com.faculdade.pesquisa.dto.AvaliacaoResponse;
import com.faculdade.pesquisa.dto.ResumoResponse;
import com.faculdade.pesquisa.service.PesquisaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consolidacao usada no painel. Restrito a ADMIN pela SecurityConfig (atendente cuida so dos chamados). */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final PesquisaService pesquisaService;

    public RelatorioController(PesquisaService pesquisaService) {
        this.pesquisaService = pesquisaService;
    }

    @GetMapping("/resumo")
    public ResumoResponse resumo() {
        return pesquisaService.resumo();
    }

    @GetMapping("/avaliacoes")
    public List<AvaliacaoResponse> avaliacoes() {
        return pesquisaService.listarRespondidas();
    }
}
