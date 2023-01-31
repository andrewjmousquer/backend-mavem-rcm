# Portal Carbon CRM

## Documentação da API

http://localhost:8443/swagger-ui.html

Em construção...

## Banco de Dados

Para a versão 2.0.0 ainda preciso fazer o DIFF e gerar os scripts de alterações...

É possível subir um banco do zero com o container

Acesso:

User: root
Pass: sbmroot

Em construção

## Acesso do Portal

User: root
Pass: 

## Ambiente Docker

Para subir o ambiente docker é necessário ter instalado

	- Docker
	- Docker-Compose

### Iniciando o Ambiente

1 - Acessar a pasta do projeto;
2 - Para garantir a subida do BD primeiro, vamos executar `docker-compose up -d carbon-db`
	2.1 - Deve demorar um pouco caso seja a primeira vez subindo ele.
3 - Para subir o restante executar `docker-compose up -d`

### Parando o Ambiente

1 - Acessar a pasta do projeto;
2 - Executar o comando `docker-compose stop`

**IMPORTANTE**

So executar `docker-compose down` no caso de querer distruir completamente os containers, isso vai fazer com que todos os dados de volumes nao persistentes sejam perdidos


## Testes

O projeto conta com testes unitários, onde validamos as principais classes, Services e DAOs

**DAO**

Para o teste de DAO usamos o TestContiners, ou seja, é o único teste que não é unitário, ele vira um teste de integração, pois realmente criamos um banco temporário e manipulamos os dados de fato os dados

### Detalhes da API RESTful
A API RESTful Web Portal Base contém as seguintes características:  
* Projeto criado com Spring Boot e Java 8
* Banco de dados MySQL
* Autenticação e autorização com Spring Security e tokens JWT (JSON Web Token)
* Testes unitários e de integração com JUnit e Mockito
* Caching com EhCache
