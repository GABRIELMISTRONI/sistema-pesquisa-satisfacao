package com.faculdade.pesquisa.repository;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.domain.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByToken(String token);

    Optional<Avaliacao> findByChamado(Chamado chamado);

    List<Avaliacao> findByRespondidaEmIsNotNullOrderByRespondidaEmDesc();

    /** Usado pelo job de envio: pesquisas cujo prazo de atraso ja passou e o e-mail ainda nao saiu. */
    List<Avaliacao> findByEmailEnviadoEmIsNullAndEnviarEmLessThanEqual(OffsetDateTime agora);
}
