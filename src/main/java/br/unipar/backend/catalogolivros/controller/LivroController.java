package br.unipar.backend.catalogolivros.controller;

import br.unipar.backend.catalogolivros.model.Livro;
import br.unipar.backend.catalogolivros.repository.LivroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroRepository repository;

    public LivroController(LivroRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Integer anoPublicacao) {

        String filtroTitulo = filtro(titulo);
        String filtroAutor = filtro(autor);
        String filtroGenero = filtro(genero);
        List<Livro> livros;
        if (filtroTitulo.isEmpty() && filtroAutor.isEmpty() && filtroGenero.isEmpty() && anoPublicacao == null) {
            livros = repository.findAll(Sort.by("id"));
        } else {
            livros = repository.buscarComFiltros(filtroTitulo, filtroAutor, filtroGenero, anoPublicacao);
        }
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Livro> encontrado = repository.findById(id);
        if (encontrado.isEmpty()) {
            return naoEncontrado();
        }

        return ResponseEntity.ok(encontrado.get());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Livro livro) {
        String erro = validarLivro(livro);
        if (erro != null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", erro));
        }

        livro.setId(null);
        Livro salvo = repository.save(livro);
        return ResponseEntity.created(URI.create("/livros/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Livro livro) {
        Optional<Livro> encontrado = repository.findById(id);
        if (encontrado.isEmpty()) {
            return naoEncontrado();
        }

        String erro = validarLivro(livro);
        if (erro != null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", erro));
        }

        Livro atual = encontrado.get();
        atual.setTitulo(livro.getTitulo());
        atual.setAutor(livro.getAutor());
        atual.setGenero(livro.getGenero());
        atual.setAnoPublicacao(livro.getAnoPublicacao());
        return ResponseEntity.ok(repository.save(atual));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Optional<Livro> encontrado = repository.findById(id);
        if (encontrado.isEmpty()) {
            return naoEncontrado();
        }

        repository.delete(encontrado.get());
        return ResponseEntity.noContent().build();
    }

    private String filtro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private ResponseEntity<?> naoEncontrado() {
        return ResponseEntity.status(404).body(Map.of("mensagem", "Livro não encontrado."));
    }

    private String validarLivro(Livro livro) {
        if (livro == null) {
            return "Informe os dados do livro.";
        }
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            return "Informe o título do livro.";
        }
        if (livro.getAutor() == null || livro.getAutor().isBlank()) {
            return "Informe o autor do livro.";
        }
        if (livro.getGenero() == null || livro.getGenero().isBlank()) {
            return "Informe o gênero do livro.";
        }
        if (livro.getAnoPublicacao() == null || livro.getAnoPublicacao() <= 0) {
            return "Informe um ano de publicação maior que zero.";
        }
        return null;
    }
}
