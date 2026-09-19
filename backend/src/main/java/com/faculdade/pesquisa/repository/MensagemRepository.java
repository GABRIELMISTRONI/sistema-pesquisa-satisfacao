package com.faculdade.pesquisa.repository;

import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByChamadoOrderByCriadaEmAsc(Chamado chamado);

    List<Mensagem> findByChamadoAndIdGreaterThanOrderByCriadaEmAsc(Chamado chamado, Long id);
}
