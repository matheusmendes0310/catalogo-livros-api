package br.unipar.backend.catalogolivros.controller;

import br.unipar.backend.catalogolivros.model.Livro;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final List<Livro> livros = new ArrayList<>();
    private long proximoId = 1;

    // O mesmo controller atende várias requisições; synchronized protege a lista.
    @GetMapping
    public synchronized ResponseEntity<List<Livro>> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) Integer anoPublicacao) {

        List<Livro> resultado = new ArrayList<>();

        for (Livro livro : livros) {
            boolean combinaTitulo = corresponde(livro.getTitulo(), titulo);
            boolean combinaAutor = corresponde(livro.getAutor(), autor);
            boolean combinaGenero = corresponde(livro.getGenero(), genero);
            boolean combinaAno = anoPublicacao == null
                    || anoPublicacao.equals(livro.getAnoPublicacao());

            if (combinaTitulo && combinaAutor && combinaGenero && combinaAno) {
                resultado.add(livro);
            }
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public synchronized ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Livro livro = encontrarLivro(id);

        if (livro == null) {
            return ResponseEntity.status(404).body(Map.of("mensagem", "Livro não encontrado."));
        }

        return ResponseEntity.ok(livro);
    }

    @PostMapping
    public synchronized ResponseEntity<?> cadastrar(@RequestBody Livro livro) {
        String erro = validarLivro(livro);

        if (erro != null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", erro));
        }

        // O ID é gerado pelo contador, independentemente do ID enviado no JSON.
        livro.setId(proximoId++);
        livros.add(livro);

        return ResponseEntity.created(URI.create("/livros/" + livro.getId())).body(livro);
    }

    @PutMapping("/{id}")
    public synchronized ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Livro livro) {
        Livro livroAtual = encontrarLivro(id);

        if (livroAtual == null) {
            return ResponseEntity.status(404).body(Map.of("mensagem", "Livro não encontrado."));
        }

        String erro = validarLivro(livro);
        if (erro != null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", erro));
        }

        // Substitui os dados e mantém o ID indicado na URL.
        livro.setId(id);
        int posicao = livros.indexOf(livroAtual);
        livros.set(posicao, livro);

        return ResponseEntity.ok(livro);
    }

    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<?> excluir(@PathVariable Long id) {
        Livro livro = encontrarLivro(id);

        if (livro == null) {
            return ResponseEntity.status(404).body(Map.of("mensagem", "Livro não encontrado."));
        }

        livros.remove(livro);
        return ResponseEntity.noContent().build();
    }

    private Livro encontrarLivro(Long id) {
        for (Livro livro : livros) {
            if (livro.getId().equals(id)) {
                return livro;
            }
        }
        return null;
    }

    private boolean corresponde(String valor, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }
        return valor.toLowerCase(Locale.ROOT).contains(filtro.trim().toLowerCase(Locale.ROOT));
    }

    private String validarLivro(Livro livro) {
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
