package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.Perfil;
import com.faculdade.pesquisa.domain.Prioridade;
import com.faculdade.pesquisa.domain.StatusChamado;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.dto.ChamadoRequest;
import com.faculdade.pesquisa.dto.ChamadoResponse;
import com.faculdade.pesquisa.repository.AvaliacaoRepository;
import com.faculdade.pesquisa.repository.ChamadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PesquisaService pesquisaService;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            AvaliacaoRepository avaliacaoRepository,
            PesquisaService pesquisaService) {
        this.chamadoRepository = chamadoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.pesquisaService = pesquisaService;
    }

    /** Clientes enxergam apenas os proprios chamados; atendentes e admins enxergam todos. */
    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar(Usuario usuarioLogado) {
        List<Chamado> chamados = usuarioLogado.getPerfil() == Perfil.CLIENTE
                ? chamadoRepository.findBySolicitanteOrderByCriadoEmDesc(usuarioLogado)
                : chamadoRepository.findAllByOrderByCriadoEmDesc();

        return chamados.stream().map(this::montarResposta).toList();
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscar(Long id, Usuario usuarioLogado) {
        return montarResposta(carregarComAcesso(id, usuarioLogado));
    }

    /**
     * Carrega o chamado garantindo que o usuario logado pode ve-lo (mesma regra
     * de {@link #buscar}). Usado tambem pelo chat, que e um sub-recurso do chamado.
     */
    @Transactional(readOnly = true)
    public Chamado carregarComAcesso(Long id, Usuario usuarioLogado) {
        Chamado chamado = carregar(id);
        garantirAcesso(chamado, usuarioLogado);
        return chamado;
    }

    @Transactional
    public ChamadoResponse abrir(ChamadoRequest request, Usuario solicitante) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo().trim());
        chamado.setDescricao(request.descricao().trim());
        chamado.setPrioridade(converterPrioridade(request.prioridade()));
        chamado.setSolicitante(solicitante);
        chamado.setStatus(StatusChamado.ABERTO);

        return montarResposta(chamadoRepository.save(chamado));
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, String novoStatus, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() == Perfil.CLIENTE) {
            throw RegraDeNegocioException.semPermissao("Apenas atendentes podem alterar o status do chamado");
        }

        StatusChamado status = converterStatus(novoStatus);
        if (status == StatusChamado.ENCERRADO) {
            return encerrar(id, usuarioLogado);
        }

        Chamado chamado = carregar(id);
        if (chamado.getStatus() == StatusChamado.ENCERRADO) {
            throw RegraDeNegocioException.conflito("Chamado ja esta encerrado");
        }

        chamado.setStatus(status);
        chamado.setResponsavel(usuarioLogado);
        chamado.setAtualizadoEm(OffsetDateTime.now());

        return montarResposta(chamado);
    }

    /**
     * Encerra o chamado e dispara automaticamente a pesquisa de satisfacao,
     * que e o objetivo principal do sistema.
     */
    @Transactional
    public ChamadoResponse encerrar(Long id, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() == Perfil.CLIENTE) {
            throw RegraDeNegocioException.semPermissao("Apenas atendentes podem encerrar um chamado");
        }

        Chamado chamado = carregar(id);
        if (chamado.getStatus() == StatusChamado.ENCERRADO) {
            throw RegraDeNegocioException.conflito("Chamado ja esta encerrado");
        }

        chamado.setStatus(StatusChamado.ENCERRADO);
        chamado.setResponsavel(usuarioLogado);
        chamado.setEncerradoEm(OffsetDateTime.now());
        chamado.setAtualizadoEm(OffsetDateTime.now());

        pesquisaService.gerarParaChamado(chamado);

        return montarResposta(chamado);
    }

    private Chamado carregar(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> RegraDeNegocioException.naoEncontrado("Chamado nao encontrado"));
    }

    private void garantirAcesso(Chamado chamado, Usuario usuarioLogado) {
        boolean dono = chamado.getSolicitante().getId().equals(usuarioLogado.getId());
        if (usuarioLogado.getPerfil() == Perfil.CLIENTE && !dono) {
            throw RegraDeNegocioException.semPermissao("Este chamado pertence a outro usuario");
        }
    }

    private ChamadoResponse montarResposta(Chamado chamado) {
        Optional<Avaliacao> avaliacao = avaliacaoRepository.findByChamado(chamado);
        return ChamadoResponse.de(
                chamado,
                avaliacao.map(Avaliacao::getToken).orElse(null),
                avaliacao.map(Avaliacao::getNota).orElse(null));
    }

    private Prioridade converterPrioridade(String valor) {
        if (valor == null || valor.isBlank()) {
            return Prioridade.MEDIA;
        }
        try {
            return Prioridade.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw RegraDeNegocioException.invalido("Prioridade invalida. Use BAIXA, MEDIA ou ALTA");
        }
    }

    private StatusChamado converterStatus(String valor) {
        try {
            return StatusChamado.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw RegraDeNegocioException.invalido("Status invalido. Use ABERTO, EM_ANDAMENTO ou ENCERRADO");
        }
    }
}
