package com.faculdade.pesquisa.service;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.ClassificacaoNota;
import com.faculdade.pesquisa.domain.StatusChamado;
import com.faculdade.pesquisa.dto.AvaliacaoRequest;
import com.faculdade.pesquisa.dto.AvaliacaoResponse;
import com.faculdade.pesquisa.dto.ResumoResponse;
import com.faculdade.pesquisa.repository.AvaliacaoRepository;
import com.faculdade.pesquisa.repository.ChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Regras da pesquisa de satisfacao: a pesquisa e criada automaticamente no
 * encerramento do chamado, enviada ao Teams e respondida pelo cliente
 * (link da pesquisa web ou clique direto no cartao do Teams).
 *
 * Escala da nota: 1 a 10 (estilo NPS) - ver {@link ClassificacaoNota}.
 */
@Service
public class PesquisaService {

    private static final Logger log = LoggerFactory.getLogger(PesquisaService.class);
    private static final int NOTA_MINIMA = 1;
    private static final int NOTA_MAXIMA = 10;

    private final AvaliacaoRepository avaliacaoRepository;
    private final ChamadoRepository chamadoRepository;
    private final String urlBaseFrontend;
    private final long atrasoEnvioMinutos;

    public PesquisaService(
            AvaliacaoRepository avaliacaoRepository,
            ChamadoRepository chamadoRepository,
            @Value("${app.frontend.url}") String urlBaseFrontend,
            @Value("${app.email.atraso-minutos}") long atrasoEnvioMinutos) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.chamadoRepository = chamadoRepository;
        this.urlBaseFrontend = urlBaseFrontend;
        this.atrasoEnvioMinutos = atrasoEnvioMinutos;
    }

    /**
     * Cria a pesquisa do chamado encerrado e agenda o e-mail para
     * app.email.atraso-minutos depois - quem realmente envia e o
     * {@link EnvioPesquisaScheduler}. Se ja existir (chamado encerrado de novo
     * por engano), so devolve a que existe, sem reagendar.
     */
    @Transactional
    public Avaliacao gerarParaChamado(Chamado chamado) {
        return avaliacaoRepository.findByChamado(chamado).orElseGet(() -> {
            OffsetDateTime agora = OffsetDateTime.now();

            Avaliacao avaliacao = new Avaliacao();
            avaliacao.setChamado(chamado);
            avaliacao.setToken(UUID.randomUUID().toString());
            avaliacao.setCriadaEm(agora);
            avaliacao.setEnviarEm(agora.plusMinutes(atrasoEnvioMinutos));

            Avaliacao salva = avaliacaoRepository.save(avaliacao);
            log.info("Pesquisa de satisfacao gerada para o chamado {}: {} (e-mail agendado para {})",
                    chamado.getId(), montarLink(salva.getToken()), salva.getEnviarEm());

            return salva;
        });
    }

    /** Link da pagina web de resposta (usado em logs e no botao "copiar link" do front). */
    public String montarLink(String token) {
        return urlBaseFrontend + "/pesquisa?token=" + token;
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPorToken(String token) {
        return AvaliacaoResponse.de(carregar(token));
    }

    /** Resposta pela pagina web: nota + comentario opcional. */
    @Transactional
    public AvaliacaoResponse responder(String token, AvaliacaoRequest request) {
        Avaliacao avaliacao = carregar(token);
        if (avaliacao.isRespondida()) {
            throw RegraDeNegocioException.conflito("Esta pesquisa ja foi respondida");
        }

        avaliacao.setNota(request.nota());
        avaliacao.setComentario(request.comentario() == null ? null : request.comentario().trim());
        avaliacao.setRespondidaEm(OffsetDateTime.now());

        return AvaliacaoResponse.de(avaliacao);
    }

    /**
     * Resposta por clique direto no cartao do Teams: so a nota, sem comentario.
     * Clique repetido no mesmo numero (ou em outro, por engano) e ignorado sem
     * erro - a pesquisa so registra a primeira resposta e sempre redireciona
     * para a pagina de agradecimento.
     */
    @Transactional
    public AvaliacaoResponse responderRapido(String token, int nota) {
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw RegraDeNegocioException.invalido("Nota deve ser de " + NOTA_MINIMA + " a " + NOTA_MAXIMA);
        }

        Avaliacao avaliacao = carregar(token);
        if (!avaliacao.isRespondida()) {
            avaliacao.setNota(nota);
            avaliacao.setRespondidaEm(OffsetDateTime.now());
        }

        return AvaliacaoResponse.de(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarRespondidas() {
        return avaliacaoRepository.findByRespondidaEmIsNotNullOrderByRespondidaEmDesc()
                .stream()
                .map(AvaliacaoResponse::de)
                .toList();
    }

    /** Numeros consolidados usados no painel: e a "analise da satisfacao" do projeto. */
    @Transactional(readOnly = true)
    public ResumoResponse resumo() {
        List<Avaliacao> todas = avaliacaoRepository.findAll();
        List<Avaliacao> respondidas = todas.stream().filter(Avaliacao::isRespondida).toList();

        Map<Integer, Long> distribuicaoNotas = new LinkedHashMap<>();
        for (int nota = NOTA_MINIMA; nota <= NOTA_MAXIMA; nota++) {
            final int valor = nota;
            distribuicaoNotas.put(nota, respondidas.stream().filter(a -> a.getNota() == valor).count());
        }

        Map<String, Long> distribuicaoClassificacao = new LinkedHashMap<>();
        for (ClassificacaoNota classificacao : ClassificacaoNota.values()) {
            distribuicaoClassificacao.put(
                    classificacao.name(),
                    respondidas.stream().filter(a -> ClassificacaoNota.deNota(a.getNota()) == classificacao).count());
        }

        Double media = respondidas.isEmpty()
                ? null
                : arredondar(respondidas.stream().mapToInt(Avaliacao::getNota).average().orElse(0));

        double taxaResposta = todas.isEmpty()
                ? 0
                : arredondar((respondidas.size() * 100.0) / todas.size());

        return new ResumoResponse(
                chamadoRepository.countByStatus(StatusChamado.ABERTO),
                chamadoRepository.countByStatus(StatusChamado.EM_ANDAMENTO),
                chamadoRepository.countByStatus(StatusChamado.ENCERRADO),
                todas.size(),
                respondidas.size(),
                taxaResposta,
                media,
                distribuicaoNotas,
                distribuicaoClassificacao);
    }

    private Avaliacao carregar(String token) {
        return avaliacaoRepository.findByToken(token)
                .orElseThrow(() -> RegraDeNegocioException.naoEncontrado("Pesquisa nao encontrada"));
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
