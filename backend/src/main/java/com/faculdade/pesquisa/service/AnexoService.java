package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Anexo;
import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.StatusChamado;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.dto.AnexoResponse;
import com.faculdade.pesquisa.repository.AnexoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Anexos de um chamado (aberto/em andamento). O arquivo fica salvo direto no
 * banco (sem bucket externo) - suficiente para o prototipo, com limite de
 * tamanho configuravel.
 */
@Service
public class AnexoService {

    private final AnexoRepository anexoRepository;
    private final ChamadoService chamadoService;
    private final long tamanhoMaximoBytes;

    public AnexoService(
            AnexoRepository anexoRepository,
            ChamadoService chamadoService,
            @Value("${app.anexos.tamanho-maximo-bytes}") long tamanhoMaximoBytes) {
        this.anexoRepository = anexoRepository;
        this.chamadoService = chamadoService;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
    }

    @Transactional(readOnly = true)
    public List<AnexoResponse> listar(Long chamadoId, Usuario usuarioLogado) {
        Chamado chamado = chamadoService.carregarComAcesso(chamadoId, usuarioLogado);
        return anexoRepository.listarMetadados(chamado);
    }

    @Transactional
    public AnexoResponse enviar(Long chamadoId, MultipartFile arquivo, Usuario autor) {
        Chamado chamado = chamadoService.carregarComAcesso(chamadoId, autor);

        if (chamado.getStatus() == StatusChamado.ENCERRADO) {
            throw RegraDeNegocioException.conflito("Chamado encerrado, nao e mais possivel enviar anexos");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw RegraDeNegocioException.invalido("Selecione um arquivo para anexar");
        }
        if (arquivo.getSize() > tamanhoMaximoBytes) {
            throw RegraDeNegocioException.invalido(
                    "Arquivo maior que o limite permitido (" + (tamanhoMaximoBytes / (1024 * 1024)) + " MB)");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        Anexo anexo = new Anexo();
        anexo.setChamado(chamado);
        anexo.setAutor(autor);
        anexo.setNomeArquivo(nomeOriginal == null || nomeOriginal.isBlank() ? "arquivo" : nomeOriginal);
        anexo.setTipoConteudo(
                arquivo.getContentType() == null ? "application/octet-stream" : arquivo.getContentType());
        anexo.setTamanhoBytes(arquivo.getSize());
        anexo.setCriadoEm(OffsetDateTime.now());

        try {
            anexo.setConteudo(arquivo.getBytes());
        } catch (IOException e) {
            throw RegraDeNegocioException.invalido("Nao foi possivel ler o arquivo enviado");
        }

        Anexo salvo = anexoRepository.save(anexo);
        return new AnexoResponse(
                salvo.getId(), chamado.getId(), autor.getId(), autor.getNome(),
                salvo.getNomeArquivo(), salvo.getTipoConteudo(), salvo.getTamanhoBytes(), salvo.getCriadoEm());
    }

    /** Carrega o anexo (com conteudo binario) ja validando o acesso ao chamado. */
    @Transactional(readOnly = true)
    public Anexo baixar(Long chamadoId, Long anexoId, Usuario usuarioLogado) {
        Chamado chamado = chamadoService.carregarComAcesso(chamadoId, usuarioLogado);
        return anexoRepository.findByIdAndChamado(anexoId, chamado)
                .orElseThrow(() -> RegraDeNegocioException.naoEncontrado("Anexo nao encontrado"));
    }
}
