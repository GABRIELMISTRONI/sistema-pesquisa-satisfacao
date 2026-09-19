package com.faculdade.pesquisa.repository;

import com.faculdade.pesquisa.domain.Avaliacao;
import com.faculdade.pesquisa.domain.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByToken(String token);

    Optional<Avaliacao> findByChamado(Chamado chamado);

    List<Avaliacao> findByRespondidaEmIsNotNullOrderByRespondidaEmDesc();
}
