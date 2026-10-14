SELECT id, titulo, autor, genero, ano_publicacao
FROM livros
ORDER BY id;

-- Substitua 1 pelo ID retornado no cadastro do Postman.
SELECT id, titulo, autor, genero, ano_publicacao
FROM livros
WHERE id = 1;
