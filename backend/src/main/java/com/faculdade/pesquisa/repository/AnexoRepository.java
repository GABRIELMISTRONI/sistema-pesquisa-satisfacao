package com.faculdade.pesquisa.repository;

import com.faculdade.pesquisa.domain.Anexo;
import com.faculdade.pesquisa.domain.Chamado;
import com.faculdade.pesquisa.dto.AnexoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    // Projeta so os metadados: evita trazer o conteudo binario pra memoria so pra listar.
    @Query("""
            select new com.faculdade.pesquisa.dto.AnexoResponse(
                a.id, a.chamado.id, a.autor.id, a.autor.nome,
                a.nomeArquivo, a.tipoConteudo, a.tamanhoBytes, a.criadoEm)
            from Anexo a
            where a.chamado = :chamado
            order by a.criadoEm asc
            """)
    List<AnexoResponse> listarMetadados(@Param("chamado") Chamado chamado);

    Optional<Anexo> findByIdAndChamado(Long id, Chamado chamado);
}
