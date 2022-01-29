---
layout: default
title: home
nav_order: 1
---

# Serviço de Revisão

Esse é um serviço utilizado para revisar se um estudante realizou um exercício no Github e, no caso de sucesso, permite registrar o resultado em um sistema Moodle.

## Sequência de revisão

Atualmente o serviço de revisão implementa uma cadeia de verificações (Checkers) antes de submeter uma avaliação no Moodle:

* Verifica se o nome e sobrenome do usuário do Github é o mesmo do Moodle (MoodleChecker)
* Confere se o repositório informado é o mesmo contigo na atividade e se esse repositório é um fork do original (RepositoryChecker)
* Analisa se a última execução dos testes passou com sucesso (RepositoryChecker)
* Verifica em todos os commits do usuário se em algum os testes foram alterados (TestChangeChecker)

## Configuração de uma atividade no Moodle

Uma atividade no Moodle necessita ser configurada para que possa ser utilizada com o serviço de revisão. Existem três informações importantes que necessitam serem informadas pra o serviço, são elas: repositório (repo), workflow (Github Actions) e o arquivo que implementa os testes (nota: atualmente todos os testes devem ficar num único arquivo). Um exemplo dessa configuração pode ser observada abaixo:

```html
<!--
    repo: "cpw2-web-storage"
    workflow: "npm-test.yml"
    test-file: "correcao.js"
-->
```

Note que a configuração deve ser informada em uma atividade no Moodle como um comentário de HTML e que o conteúdo segue o formato YAML.