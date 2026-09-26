package br.unipar.backend.catalogolivros.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LivroControllerTest {

    private MockMvc mockMvc;

    private static final String DOM_CASMURRO = """
            {"titulo":"Dom Casmurro","autor":"Machado de Assis",
             "genero":"Romance","anoPublicacao":1899}
            """;

    private static final String HOBBIT = """
            {"titulo":"O Hobbit","autor":"J. R. R. Tolkien",
             "genero":"Fantasia","anoPublicacao":1937}
            """;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LivroController()).build();
    }

    @Test
    void listaComecaVazia() throws Exception {
        mockMvc.perform(get("/livros"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void cadastraGeraIdERetornaLocalizacao() throws Exception {
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON)
                        .content(DOM_CASMURRO.replace("{", "{\"id\":999,")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/livros/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"));

        mockMvc.perform(get("/livros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("Machado de Assis"));
    }

    @Test
    void combinaTodosOsFiltrosEIgnoraMaiusculas() throws Exception {
        cadastrar(DOM_CASMURRO);
        cadastrar(HOBBIT);

        mockMvc.perform(get("/livros").param("titulo", " DOM ")
                        .param("autor", "MACHADO").param("genero", "romance")
                        .param("anoPublicacao", "1899"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/livros").param("autor", "Machado").param("genero", "Fantasia"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void filtrosFuncionamSeparadosEFiltroVazioNaoRestringe() throws Exception {
        cadastrar(DOM_CASMURRO);
        cadastrar(HOBBIT);

        mockMvc.perform(get("/livros").param("titulo", "hobbit"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        mockMvc.perform(get("/livros").param("autor", "machado"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/livros").param("genero", "fantasia"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        mockMvc.perform(get("/livros").param("anoPublicacao", "1899"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/livros").param("titulo", " "))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void atualizaTodosOsCamposSemAlterarId() throws Exception {
        cadastrar(DOM_CASMURRO);

        mockMvc.perform(put("/livros/1").contentType(MediaType.APPLICATION_JSON)
                        .content(HOBBIT.replace("{", "{\"id\":999,")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/livros/1"))
                .andExpect(jsonPath("$.titulo").value("O Hobbit"))
                .andExpect(jsonPath("$.autor").value("J. R. R. Tolkien"))
                .andExpect(jsonPath("$.genero").value("Fantasia"))
                .andExpect(jsonPath("$.anoPublicacao").value(1937));
    }

    @Test
    void excluirNaoMudaIdsENaoReutilizaId() throws Exception {
        cadastrar(DOM_CASMURRO);
        cadastrar(HOBBIT);

        mockMvc.perform(delete("/livros/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(get("/livros/1")).andExpect(status().isNotFound());
        mockMvc.perform(get("/livros/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("O Hobbit"));
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(DOM_CASMURRO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void registroInexistenteRetorna404NosTresMetodos() throws Exception {
        mockMvc.perform(get("/livros/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Livro não encontrado."));
        mockMvc.perform(put("/livros/999").contentType(MediaType.APPLICATION_JSON).content(HOBBIT))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/livros/999")).andExpect(status().isNotFound());
    }

    @Test
    void camposInvalidosNaoEntramNaLista() throws Exception {
        String[] invalidos = {
                "{}",
                DOM_CASMURRO.replace("Dom Casmurro", "  "),
                DOM_CASMURRO.replace("Machado de Assis", ""),
                DOM_CASMURRO.replace("Romance", ""),
                DOM_CASMURRO.replace("1899", "0"),
                DOM_CASMURRO.replace("1899", "-1"),
                DOM_CASMURRO.replace("1899", "null")
        };
        for (String json : invalidos) {
            mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").exists());
        }
        mockMvc.perform(get("/livros")).andExpect(content().json("[]"));
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(HOBBIT))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void atualizacaoInvalidaPreservaDados() throws Exception {
        cadastrar(DOM_CASMURRO);
        mockMvc.perform(put("/livros/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/livros/1"))
                .andExpect(jsonPath("$.titulo").value("Dom Casmurro"));
    }

    @Test
    void jsonInvalidoCorpoAusenteEParametrosInvalidosRetornam400() throws Exception {
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content("null"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/livros/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/livros").param("anoPublicacao", "abc"))
                .andExpect(status().isBadRequest());
    }

    private void cadastrar(String json) throws Exception {
        mockMvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }
}
