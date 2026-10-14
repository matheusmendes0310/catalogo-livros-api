package br.unipar.backend.catalogolivros.controller;

import br.unipar.backend.catalogolivros.model.Livro;
import br.unipar.backend.catalogolivros.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LivroControllerTest {

    private LivroRepository repository;
    private MockMvc mockMvc;

    private static final String LIVRO = """
            {"titulo":"Dom Casmurro","autor":"Machado de Assis",
             "genero":"Romance","anoPublicacao":1899}
            """;

    @BeforeEach
    void preparar() {
        repository = mock(LivroRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LivroController(repository)).build();
    }

    @Test
    void cadastraSemPrecisarEnviarId() throws Exception {
        when(repository.save(any(Livro.class))).thenAnswer(chamada -> {
            Livro livro = chamada.getArgument(0);
            livro.setId(7L); // Simula o ID devolvido pelo banco.
            return livro;
        });

        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(LIVRO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/livros/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void listaComFiltrosCombinadosConsultaRepository() throws Exception {
        when(repository.buscarComFiltros("dom", "MACHADO", "romance", 1899))
                .thenReturn(List.of(livro(7L)));

        mockMvc.perform(get("/livros").param("titulo", " dom ").param("autor", "MACHADO")
                        .param("genero", "romance").param("anoPublicacao", "1899"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));

        verify(repository).buscarComFiltros("dom", "MACHADO", "romance", 1899);
    }

    @Test
    void listaSemFiltrosUsaFindAll() throws Exception {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(livro(7L)));

        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));

        verify(repository).findAll(any(Sort.class));
    }

    @Test
    void buscaPorIdEInexistente() throws Exception {
        when(repository.findById(7L)).thenReturn(Optional.of(livro(7L)));

        mockMvc.perform(get("/livros/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"));
        mockMvc.perform(get("/livros/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Livro não encontrado."));
    }

    @Test
    void atualizaMantendoIdDaUrl() throws Exception {
        when(repository.findById(7L)).thenReturn(Optional.of(livro(7L)));
        when(repository.save(any(Livro.class))).thenAnswer(chamada -> chamada.getArgument(0));
        String alterado = LIVRO.replace("Dom Casmurro", "Dom Casmurro - bolso");

        mockMvc.perform(put("/livros/7").contentType(MediaType.APPLICATION_JSON).content(alterado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro - bolso"));
    }

    @Test
    void excluiRegistroExistente() throws Exception {
        Livro livro = livro(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(livro));

        mockMvc.perform(delete("/livros/7"))
                .andExpect(status().isNoContent());
        verify(repository).delete(livro);
        mockMvc.perform(delete("/livros/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaDadosInvalidosSemSalvar() throws Exception {
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").exists());
        mockMvc.perform(get("/livros").param("anoPublicacao", "abc"))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    private Livro livro(Long id) {
        Livro livro = new Livro();
        livro.setId(id);
        livro.setTitulo("Dom Casmurro");
        livro.setAutor("Machado de Assis");
        livro.setGenero("Romance");
        livro.setAnoPublicacao(1899);
        return livro;
    }
}
