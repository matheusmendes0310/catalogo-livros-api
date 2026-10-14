package br.unipar.backend.catalogolivros.repository;

import br.unipar.backend.catalogolivros.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    @Query("""
            SELECT l FROM Livro l
            WHERE (:titulo = '' OR LOCATE(LOWER(:titulo), LOWER(l.titulo)) > 0)
              AND (:autor = '' OR LOCATE(LOWER(:autor), LOWER(l.autor)) > 0)
              AND (:genero = '' OR LOCATE(LOWER(:genero), LOWER(l.genero)) > 0)
              AND l.anoPublicacao = COALESCE(:ano, l.anoPublicacao)
            ORDER BY l.id
            """)
    List<Livro> buscarComFiltros(@Param("titulo") String titulo,
                                 @Param("autor") String autor,
                                 @Param("genero") String genero,
                                 @Param("ano") Integer ano);
}
