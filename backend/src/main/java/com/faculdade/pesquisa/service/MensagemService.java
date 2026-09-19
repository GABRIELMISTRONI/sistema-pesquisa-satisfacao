package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.Mensagem;
import com.faculdade.pesquisa.domain.StatusChamado;
import com.faculdade.pesquisa.domain.Usuario;
import com.faculdade.pesquisa.dto.MensagemResponse;
import com.faculdade.pesquisa.repository.MensagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Chat simples dentro do chamado (REST + polling, sem WebSocket). Fica
 * disponivel enquanto o chamado esta ABERTO ou EM_ANDAMENTO; ao encerrar,
 * vira somente leitura.
 */
@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final ChamadoService chamadoService;

    public MensagemService(MensagemRepository mensagemRepository, ChamadoService chamadoService) {
        this.mensagemRepository = mensagemRepository;
        this.chamadoService = chamadoService;
    }

    @Transactional(readOnly = true)
    public List<MensagemResponse> listar(Long chamadoId, Long apos, Usuario usuarioLogado) {
        Chamado chamado = chamadoService.carregarComAcesso(chamadoId, usuarioLogado);

        List<Mensagem> mensagens = apos == null
                ? mensagemRepository.findByChamadoOrderByCriadaEmAsc(chamado)
                : mensagemRepository.findByChamadoAndIdGreaterThanOrderByCriadaEmAsc(chamado, apos);

        return mensagens.stream().map(MensagemResponse::de).toList();
    }

    @Transactional
    public MensagemResponse enviar(Long chamadoId, String texto, Usuario autor) {
        Chamado chamado = chamadoService.carregarComAcesso(chamadoId, autor);

        if (chamado.getStatus() == StatusChamado.ENCERRADO) {
            throw RegraDeNegocioException.conflito("Chamado encerrado, nao e mais possivel enviar mensagens");
        }

        Mensagem mensagem = new Mensagem();
        mensagem.setChamado(chamado);
        mensagem.setAutor(autor);
        mensagem.setTexto(texto.trim());
        mensagem.setCriadaEm(OffsetDateTime.now());

        return MensagemResponse.de(mensagemRepository.save(mensagem));
    }
}
