# Catálogo de livros

Matheus Mendes e Gabriel Foscarini.

API REST em Java 17 e Spring Boot. Os livros são salvos no PostgreSQL com Spring Data JPA. O banco gera o `id`; o cadastro recebe apenas `titulo`, `autor`, `genero` e `anoPublicacao`.

## Rotas

| Método | Rota | Função |
| --- | --- | --- |
| GET | `/livros` | Lista os livros e aplica filtros |
| GET | `/livros/{id}` | Consulta pelo ID |
| POST | `/livros` | Cadastra |
| PUT | `/livros/{id}` | Atualiza |
| DELETE | `/livros/{id}` | Exclui |

Os filtros opcionais são `titulo`, `autor`, `genero` e `anoPublicacao`. Podem ser usados juntos, por exemplo: `/livros?autor=Clarice&genero=Romance&anoPublicacao=1977`.

## Banco e execução

Crie um banco PostgreSQL chamado `catalogo_livros`. As configurações estão em `src/main/resources/application.properties`: porta 5432, usuário `postgres` e senha `postgres` por padrão. Se a sua instalação usa outros dados, defina as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD` antes de iniciar. Com o PostgreSQL em execução, rode `iniciar.cmd` no Windows. O JPA cria a tabela `livros` na primeira execução.

A API fica em `http://localhost:8080/livros`. Para ver diretamente no banco:

```sql
SELECT id, titulo, autor, genero, ano_publicacao FROM livros ORDER BY id;
```

O arquivo [consultas.sql](consultas.sql) também contém a consulta por ID. Execute-o conectado ao banco `catalogo_livros`.

## Postman

Importe [postman/Catalogo-Livros.postman_collection.json](postman/Catalogo-Livros.postman_collection.json). Execute as requisições 01 e 02; reinicie a aplicação, sem apagar o banco; depois execute a 03 para consultar o mesmo ID. As demais requisições cobrem filtros, atualização, exclusão e validação. O arquivo `testar-postman.cmd` executa a coleção completa automaticamente, mas a pausa para reiniciar precisa ser feita manualmente na demonstração.
