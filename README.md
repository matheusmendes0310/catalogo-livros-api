# Catálogo de livros

Matheus Mendes e Gabriel Foscarini.

API REST feita com Java 17, Spring Boot e Spring Web. Os livros ficam em uma lista na memória, sem banco de dados. O ID é atribuído por um contador no controller.

## Rotas

| Método | Rota | Função |
| --- | --- | --- |
| GET | `/livros` | Lista os livros e aplica filtros |
| GET | `/livros/{id}` | Consulta um livro pelo ID |
| POST | `/livros` | Cadastra um livro |
| PUT | `/livros/{id}` | Atualiza um livro |
| DELETE | `/livros/{id}` | Exclui um livro |

O modelo tem os campos `id`, `titulo`, `autor`, `genero` e `anoPublicacao`. A listagem aceita quatro filtros por `@RequestParam`: `titulo`, `autor`, `genero` e `anoPublicacao`. Eles podem ser combinados na mesma requisição.

## Execução

Com JDK 17 ou superior, execute `iniciar.cmd` no Windows. A API fica em `http://localhost:8080/livros`.

## Postman

A coleção para importação está em [postman/Catalogo-Livros.postman_collection.json](postman/Catalogo-Livros.postman_collection.json). Com a aplicação recém-iniciada, execute as requisições numeradas em ordem. A coleção salva os IDs para as consultas seguintes. O arquivo `testar-postman.cmd` executa a coleção completa automaticamente.
