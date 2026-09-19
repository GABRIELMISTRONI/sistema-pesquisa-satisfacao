package com.faculdade.pesquisa.repository;

import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.domain.StatusChamado;
import com.faculdade.pesquisa.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findAllByOrderByCriadoEmDesc();

    List<Chamado> findBySolicitanteOrderByCriadoEmDesc(Usuario solicitante);

    long countByStatus(StatusChamado status);
}
