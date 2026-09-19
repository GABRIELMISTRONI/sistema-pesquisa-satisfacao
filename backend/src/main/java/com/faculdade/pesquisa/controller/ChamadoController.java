package com.faculdade.pesquisa.controller;

import com.faculdade.pesquisa.domain.Anexo;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.dto.AnexoResponse;
import com.faculdade.pesquisa.dto.ChamadoRequest;
import com.faculdade.pesquisa.dto.ChamadoResponse;
import com.faculdade.pesquisa.dto.MensagemRequest;
import com.faculdade.pesquisa.dto.MensagemResponse;
import com.faculdade.pesquisa.dto.StatusRequest;
import com.faculdade.pesquisa.security.UsuarioAutenticado;
import com.faculdade.pesquisa.service.AnexoService;
import com.faculdade.pesquisa.service.ChamadoService;
import com.faculdade.pesquisa.service.MensagemService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final MensagemService mensagemService;
    private final AnexoService anexoService;

    public ChamadoController(
            ChamadoService chamadoService,
            MensagemService mensagemService,
            AnexoService anexoService) {
        this.chamadoService = chamadoService;
        this.mensagemService = mensagemService;
        this.anexoService = anexoService;
    }

    @GetMapping
    public List<ChamadoResponse> listar(Authentication autenticacao) {
        return chamadoService.listar(logado(autenticacao));
    }

    @GetMapping("/{id}")
    public ChamadoResponse buscar(@PathVariable Long id, Authentication autenticacao) {
        return chamadoService.buscar(id, logado(autenticacao));
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> abrir(
            @RequestBody @Valid ChamadoRequest request,
            Authentication autenticacao) {
        ChamadoResponse resposta = chamadoService.abrir(request, logado(autenticacao));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PatchMapping("/{id}/status")
    public ChamadoResponse alterarStatus(
            @PathVariable Long id,
            @RequestBody @Valid StatusRequest request,
            Authentication autenticacao) {
        return chamadoService.alterarStatus(id, request.status(), logado(autenticacao));
    }

    @PostMapping("/{id}/encerrar")
    public ChamadoResponse encerrar(@PathVariable Long id, Authentication autenticacao) {
        return chamadoService.encerrar(id, logado(autenticacao));
    }

    @GetMapping("/{id}/mensagens")
    public List<MensagemResponse> listarMensagens(
            @PathVariable Long id,
            @RequestParam(required = false) Long apos,
            Authentication autenticacao) {
        return mensagemService.listar(id, apos, logado(autenticacao));
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<MensagemResponse> enviarMensagem(
            @PathVariable Long id,
            @RequestBody @Valid MensagemRequest request,
            Authentication autenticacao) {
        MensagemResponse resposta = mensagemService.enviar(id, request.texto(), logado(autenticacao));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{id}/anexos")
    public List<AnexoResponse> listarAnexos(@PathVariable Long id, Authentication autenticacao) {
        return anexoService.listar(id, logado(autenticacao));
    }

    @PostMapping(value = "/{id}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponse> enviarAnexo(
            @PathVariable Long id,
            @RequestPart("arquivo") MultipartFile arquivo,
            Authentication autenticacao) {
        AnexoResponse resposta = anexoService.enviar(id, arquivo, logado(autenticacao));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{id}/anexos/{anexoId}")
    public ResponseEntity<byte[]> baixarAnexo(
            @PathVariable Long id,
            @PathVariable Long anexoId,
            Authentication autenticacao) {
        Anexo anexo = anexoService.baixar(id, anexoId, logado(autenticacao));
        ContentDisposition disposicao = ContentDisposition.attachment()
                .filename(anexo.getNomeArquivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getTipoConteudo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .body(anexo.getConteudo());
    }

    private Usuario logado(Authentication autenticacao) {
        return ((UsuarioAutenticado) autenticacao.getPrincipal()).getUsuario();
    }
}
