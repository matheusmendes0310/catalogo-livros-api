# Catálogo de livros

API REST para cadastrar e consultar livros. Trabalho de Java com Spring Boot, seguindo a estrutura de model e controller do exemplo de aula.

Os livros ficam em uma `List<Livro>` no controller. A aplicação começa vazia e os dados são perdidos quando ela é encerrada. O ID é gerado por um contador: 1, 2, 3... Excluir um registro não altera os IDs dos outros nem reaproveita o ID excluído durante a mesma execução.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC (Spring Web na versão usada no exemplo da professora)
- Maven Wrapper

Não há banco de dados, camada service ou repository. Getters e setters foram escritos no modelo.

## Executar

É necessário um **JDK 17 ou superior**. Apenas o Java 8 ou um JRE não são suficientes. Configure `JAVA_HOME` para a pasta do JDK. O Maven Wrapper baixa o Maven na primeira execução, então não é necessário instalar o Maven separadamente. A primeira execução exige internet.

No Windows, abra o terminal nesta pasta e execute:

```powershell
.\iniciar.cmd
```

O script usa o JDK portátil em `.tools/jdk17`, se estiver disponível nesta cópia local. Em uma cópia obtida pelo Git, usa o JDK configurado no computador. As ferramentas e dependências locais são ignoradas pelo Git.

Também é possível abrir o `pom.xml` na IDE, selecionar um JDK compatível e executar `CatalogoLivrosApplication`.

Para executar diretamente pelo Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, com o JDK configurado:

```sh
sh mvnw spring-boot:run
```

Base da API: `http://localhost:8080/livros`. Para parar no terminal, pressione `Ctrl+C`. A rota `/` não foi criada; acessar somente `http://localhost:8080` retorna 404.

## Modelo

| Campo | Tipo | Regra |
| --- | --- | --- |
| id | Long | Gerado pela API |
| titulo | String | Obrigatório, não pode estar em branco |
| autor | String | Obrigatório, não pode estar em branco |
| genero | String | Obrigatório, não pode estar em branco |
| anoPublicacao | Integer | Obrigatório, maior que zero |

## Rotas

| Método | Rota | Resultado |
| --- | --- | --- |
| GET | `/livros` | Lista todos ou aplica os filtros; 200 |
| GET | `/livros/{id}` | Busca pelo ID; 200 ou 404 |
| POST | `/livros` | Cadastra; 201 e cabeçalho Location |
| PUT | `/livros/{id}` | Atualiza todos os campos; 200 ou 404 |
| DELETE | `/livros/{id}` | Exclui; 204 sem corpo ou 404 |

POST e PUT recebem JSON. Envie `Content-Type: application/json`:

```json
{
  "titulo": "Dom Casmurro",
  "autor": "Machado de Assis",
  "genero": "Romance",
  "anoPublicacao": 1899
}
```

No POST, o `id` enviado no JSON é ignorado. No PUT, o ID vem da URL e é mantido. O PUT precisa dos quatro campos; não faz atualização parcial. Dados inválidos retornam 400. Um ID numérico que não existe retorna 404; um ID como `abc` retorna 400.

## Filtros

Todos são opcionais e usam `@RequestParam`:

| Parâmetro | Exemplo | Comportamento |
| --- | --- | --- |
| titulo | `?titulo=dom` | Contém o trecho informado |
| autor | `?autor=machado` | Contém o trecho informado |
| genero | `?genero=romance` | Contém o trecho informado |
| anoPublicacao | `?anoPublicacao=1899` | Ano exato |

Os filtros de texto ignoram diferenças entre maiúsculas e minúsculas e espaços nas pontas do filtro. Acentos continuam sendo considerados. Filtro de texto vazio não restringe a busca. Sem resultado, a resposta é `200` com `[]`.

Filtros combinados usam **E**: o livro precisa atender a todos os parâmetros enviados.

```text
http://localhost:8080/livros?autor=machado&genero=romance
http://localhost:8080/livros?titulo=dom&autor=machado&genero=romance&anoPublicacao=1899
```

## Postman

Na conta `matheusmendes0310`, a coleção já está configurada no workspace **Matheus Mendes's Workspace**:

[Abrir Catálogo de Livros no Postman](https://go.postman.co/workspace/9387eabc-5698-4afe-8bf1-96380dcc913a/collection/58534606-50d66ef9-8d9d-49bd-94e1-543078a7800b)

Ela tem as 29 requisições, as verificações automáticas e exemplos de respostas obtidas nos testes. O ambiente **Catálogo de Livros - Local** contém o endereço da API. A coleção também funciona sem selecionar um ambiente, pois já possui a variável `baseUrl`.

Para usar o Postman no navegador, mantenha o Desktop Agent aberto e selecione **Desktop Agent** no Postman. A API precisa estar iniciada. O agente da nuvem não consegue acessar `localhost`.

Se preferir importar o arquivo em outra conta ou no aplicativo de desktop:

1. Inicie a aplicação.
2. No Postman, clique em **Import** e selecione `postman/Catalogo-Livros.postman_collection.json`.
3. Abra a coleção. A variável `baseUrl` já contém `http://localhost:8080`.
4. Com a aplicação recém-iniciada e a lista vazia, execute as requisições na ordem de 01 a 29. Também pode usar o **Runner** com uma iteração.
5. Confira o corpo, o status HTTP e a aba de resultados dos testes de cada requisição.

Os cadastros salvam os IDs nas variáveis da coleção, usados nas consultas, atualizações e exclusões. Não é necessário preencher essas variáveis manualmente. Na execução completa, os livros de teste são excluídos ao final.

A coleção cobre CRUD, os quatro filtros isolados, filtros combinados, busca sem resultado, validação, IDs inválidos, registros inexistentes e a preservação dos IDs após excluir um livro.

### Executar a coleção automaticamente no Windows

Com a API iniciada e a lista vazia, dê dois cliques em `testar-postman.cmd` ou execute:

```powershell
.\testar-postman.cmd
```

O script baixa o executor oficial do Postman na primeira execução e roda todas as requisições e verificações. Não precisa de Node.js nem de login no Postman. Ao terminar, abra `postman/relatorios/resultado.html` no navegador para conferir os resultados. O relatório detalhado também fica em `postman/relatorios/resultado.json`. Os relatórios e a ferramenta baixada ficam somente no computador.

Se a lista já tiver livros, o script pede para reiniciar a API. Isso evita alterar cadastros feitos fora dos testes. A execução completa exclui somente os livros criados pela coleção.

O teste automático usa o Postman CLI. Avisos sobre login e publicação na nuvem não impedem os testes locais. Para mostrar a interface do Postman no vídeo, abra a coleção configurada na sua conta ou importe o arquivo no aplicativo e execute as requisições na ordem.

## Testes automatizados

No Windows:

```powershell
.\testar.cmd
```

Ou, com o JDK configurado:

```powershell
.\mvnw.cmd test
```

Os testes verificam requisições e respostas JSON, códigos HTTP, geração do ID, filtros e alterações na lista. Eles ficam em `src/test` e não acrescentam camadas à aplicação.

Na verificação do projeto, os 10 testes Java passaram. A coleção também foi executada contra a API pelo Newman e pelo Postman CLI: 29 requisições e 54 verificações, sem falhas.

## Estrutura principal

```text
src/main/java/br/unipar/backend/catalogolivros/
├── CatalogoLivrosApplication.java
├── model/
│   └── Livro.java
└── controller/
    └── LivroController.java
```

`@PathVariable` recebe o ID da URL. `@RequestParam` recebe os filtros. `@RequestBody` recebe o JSON e o transforma em um Livro. `ResponseEntity` define o status, o corpo e, no cadastro, o cabeçalho Location.

## Conferência dos requisitos

- Modelo com ID e quatro campos adicionais.
- GET de listagem e GET por ID.
- POST, PUT por ID e DELETE por ID.
- Quatro filtros com `@RequestParam`, combináveis.
- Uso de `@PathVariable`, `@RequestBody` e `ResponseEntity`.
- Armazenamento em lista e geração manual do ID.
- Apenas model e controller como camadas.
- Coleção para demonstração e testes no Postman.

Referência de estrutura: projeto `minhaapi-toledo-master` disponibilizado pela professora. O Maven Wrapper foi aproveitado desse exemplo.
