# Serviço de Revisão

Esse é um serviço utilizado para revisar se um estudante realizou um exercício no Github e, no caso de sucesso, permite registrar o resultado em um sistema Moodle.

## Sequência de revisão

Atualmente o serviço de revisão implementa uma cadeia de verificações (Checkers) antes de submeter uma avaliação no Moodle:

* Verifica se o nome e sobrenome do usuário do Github é o mesmo do Moodle (MoodleChecker)
* Confere se o repositório informado é o mesmo contigo na atividade e se esse repositório é um fork do original (RepositoryChecker)
* Analisa se a última execução dos testes passou com sucesso (RepositoryChecker)
* Verifica em todos os commits do usuário se em algum os testes foram alterados (TestChangeChecker)

## Configuração do Moodle

Para a integração será necessário criar um web service dentro do Moodle. Sugerimos que sejam seguidos os passos da página Site Administration > Server > Web services > Overview (dispponível geralmente em http://endereco-do-moodle/admin/settings.php?section=webservicesoverview). Os passos são:

1. Enable web services - habilitar o web service
2. Enable protocols - habilitar o *REST protocol*
3. Create a Specific User - criar um usuário apenas para acessar o webservice. Preencher os campos obrigatórios (nome, e-mail) e selecionar como método de autenticação a opção *Web services authentication*
4. Check user capabilities - neste trecho é preciso interromper os passos da configuração do web service e ir na área de administração de usuários e vincular o web service a um perfil de usuário. É possível criar um perfil apenas para o webservice. **Importante:** o serviço precisa ter a permissão Use *REST Protocol* habilitada

5. Select a service - neste momento criamos o web service Clique em Add, defina o nome do serviço e marque as opções Enabled e Authorised users only
6. Add functions - aque iremos selecionar as funções do Moodle que o serviço terá acesso. Libere, uma a uma, as seguintes funções:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* core_webservice_get_site_info
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* core_course_get_course_module
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* core_user_get_users
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* mod_assign_get_assignments
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* mod_assign_save_grade

7. Select a specific user - No serviço que você criou você deverá vincular o usuário do web service criado anteriormente. Clique em Authorized Users e adicione apenas o usuário do web service na lista de usuário com acesso permitido.
8. Create a token for a user - Selecione o usuário do web service, o nome do serviço criado e clique em *Save Changes*. Se desejar ampliar a segurança do web service é possível nesta tela fazer a restrição do IP e definir um período de validade do token.
9. Enable developer documentation - Habilite a documentação do web service do Moodle.

## Configuração de uma atividade no Moodle

Para que o web service consiga acessar as tarefas e lista de alunos de um determinado curso, o usuário do web service criado nos passos acima precisará estar inscrito no curso.

Feito isto é possível configurar uma atividade (*Assign*) para que possa ser utilizada com o serviço de revisão. Existem três informações importantes que necessitam serem informadas pra o serviço, são elas: repositório (repo), workflow (Github Actions) e o arquivo que implementa os testes (nota: atualmente todos os testes devem ficar num único arquivo). Um exemplo dessa configuração pode ser observada abaixo:

```html
<!--
    repo: "cpw2-web-storage"
    workflow: "npm-test.yml"
    test-file: "correcao.js"
-->
```

Note que a configuração deve ser informada em uma atividade no Moodle como um comentário de HTML e que o conteúdo segue o formato YAML.